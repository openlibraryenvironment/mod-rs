package org.olf.rs.hostlms;

import org.olf.rs.PatronRequest
import groovyx.net.http.HttpBuilder
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.statemodel.Status;
import com.k_int.web.toolkit.settings.AppSetting
import groovy.xml.StreamingMarkupBuilder
import static groovyx.net.http.HttpBuilder.configure
import groovyx.net.http.FromServer;
import com.k_int.web.toolkit.refdata.RefdataValue
import static groovyx.net.http.ContentTypes.XML
import org.olf.rs.lms.HostLMSActions;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.AcceptItem;

import org.olf.rs.circ.client.NCIPClientWrapper

import org.json.JSONObject;
import org.json.JSONArray;
import org.olf.rs.circ.client.CirculationClient;



/**
 * The interface between mod-rs and any host Library Management Systems
 *
 * Sirsi Z3950 behaves a little differently when looking for available copies.
 * The format of the URL for metaproxy needs to be 
 * http://mpserver:9000/?x-target=http://unicornserver:2200/UNICORN&x-pquery=@attr 1=1016 @attr 3=3 water&maximumRecords=1&recordSchema=marcxml
 *
 */
public class SymphonyHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(String address) {
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [protocol: "NCIP1"]).circulationClient;
  }

  @Override
  protected String getHoldingsQueryRecsyn() {
    return 'marcxml';
  }

  //Override to search on attribute 1016, and prepend '^C' to search string
  @Override
  public List<ItemLocation> z3950ItemsByIdentifier(PatronRequest pr) {

    List<ItemLocation> result = [];

    String id_prefix = "^C";
    String search_id = id_prefix + pr.supplierUniqueRecordId;
    String prefix_query_string = "@attr 1=1016 ${search_id}";
    def z_response = z3950Service.query(prefix_query_string, 1, getHoldingsQueryRecsyn());
    log.debug("Got Z3950 response: ${z_response}");

    if ( z_response?.numberOfRecords == 1 ) {
      // Got exactly 1 record
      Map<String, ItemLocation> availability_summary = extractAvailableItemsFrom(z_response,"Match by @attr 1=1016 ${search_id}")
      if ( availability_summary.size() > 0 ) {
        availability_summary.values().each { v ->
          result.add(v);
        }
      }

      log.debug("At end, availability summary: ${availability_summary}");
    }

    return result;
  }

  // Given the record syntax above, process response records as Opac recsyn. If you change the recsyn string above
  // you need to change the handler here. SIRSI for example needs to return us marcxml with a different location for the holdings
  @Override
  protected Map<String, ItemLocation> extractAvailableItemsFrom(z_response, String reason=null) {
    log.debug("Extract holdings from Symphony marcxml record ${z_response}");
    if ( (z_response?.numberOfRecords?.text() as int) != 1 ) {
      log.warn("Multiple records seen in response from Symphony Z39.50 server, unable to extract available items. Record: ${z_response}");
      return null;
    }

    Map<String, ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.record != null ) {
      availability_summary = extractAvailableItemsFromMARCXMLRecord(z_response?.records?.record?.recordData?.record, reason);
    }
    return availability_summary;

  }

  @Override
  public Map<String, ItemLocation> extractAvailableItemsFromMARCXMLRecord(record, String reason=null) {
    // <zs:searchRetrieveResponse>
    //   <zs:numberOfRecords>9421</zs:numberOfRecords>
    //   <zs:records>
    //     <zs:record>
    //       <zs:recordSchema>marcxml</zs:recordSchema>
    //       <zs:recordXMLEscaping>xml</zs:recordXMLEscaping>
    //       <zs:recordData>
    //         <record>
    //           <leader>02370cam a2200541Ii 4500</leader>
    //           <controlfield tag="008">140408r20141991nyua j 001 0 eng d</controlfield>
    //           <datafield tag="040" ind1=" " ind2=" ">
    //           </datafield>
    //           <datafield tag="926" ind1=" " ind2=" ">
    //             <subfield code="a">WEST</subfield>
    //             <subfield code="b">RESERVES</subfield>
    //             <subfield code="c">QL737 .C23 C58 2014</subfield>
    //             <subfield code="d">BOOK</subfield>
    //             <subfield code="f">2</subfield>
    //           </datafield>
    log.debug("extractAvailableItemsFromMARCXMLRecord (SymphonyHostLMSService)");
    Map<String,ItemLocation> availability_summary = [:]
    record.datafield.each { df ->
      if ( df.'@tag' == "926" ) {
        Map<String,String> tag_data = [:]
        df.subfield.each { sf ->
          if ( sf.@code != null ) {
            tag_data[ sf.'@code'.toString() ] = sf.toString()
          }
        }
        log.debug("Found holdings tag : ${df} ${tag_data}");
        try {
          if ( tag_data['b'] != null ){
            if ( [ 'RESERVES', 'CHECKEDOUT', 'MISSING', 'DISCARD'].contains(tag_data['b']) ) {
              // $b contains a string we think implies non-availability
            }
            else {
              log.debug("Assuming ${tag_data['b']} implies available - update extractAvailableItemsFromMARCXMLRecord if not the case");
              availability_summary[tag_data['a']] = new ItemLocation( location: tag_data['a'], shelvingLocation: tag_data['b'], callNumber: tag_data['c'], itemLoanPolicy: tag_data?.d )
            }
          }
          else {
            log.debug("No subfield b present - unable to determine number of copies available");
          }
        }
        catch ( Exception e ) {
          // All kind of odd strings like 'NONE' that mean there aren't any holdings available
          log.debug("Unable to parse number of copies: ${e.message}");
        }
      }
    }
    log.debug("MARCXML availability: ${availability_summary}");
    return availability_summary;
  }

}
