package org.olf.rs.hostlms

import org.olf.rs.PatronRequest
import org.olf.rs.circ.client.CirculationClient
import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.lms.ItemLocation
import org.olf.rs.logging.IHoldingLogDetails
import org.olf.rs.settings.ISettings

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class EvergreenHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    // This wrapper creates the circulationClient we need
    return new NCIPClientWrapper(address, [protocol: "NCIP2"]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
  }

  @Override
  protected String getHoldingsQueryRecsyn() {
    return 'marcxml';
  }

  // Override to search on attribute 1016 and tweak identifier for search string
  @Override
  public List<ItemLocation> z3950ItemsByIdentifier(PatronRequest pr, ISettings settings, IHoldingLogDetails holdingLogDetails) {

    List<ItemLocation> result = [];

    String search_id = pr.supplierUniqueRecordId;
    String prefix_query_string = "@attr 1=1016 ${search_id}";
    def z_response = z3950Service.query(settings, prefix_query_string, 1, getHoldingsQueryRecsyn(), holdingLogDetails);
    log.debug("Got Z3950 response: ${z_response}");

    if ( z_response?.numberOfRecords == 1 ) {
      // Got exactly 1 record
      List<ItemLocation> availability_summary = extractAvailableItemsFrom(z_response,"Match by @attr 1=1016 ${search_id}", holdingLogDetails)
      if ( availability_summary.size() > 0 ) {
        result = availability_summary;
      }

      log.debug("At end, availability summary: ${availability_summary}");
    }

    return result;
  }

  // Given the record syntax above, process response records as Opac recsyn. If you change the recsyn string above
  // you need to change the handler here. Evergreen for example needs to return us marcxml with a different location for the holdings
  @Override
  protected List<ItemLocation> extractAvailableItemsFrom(z_response, String reason, IHoldingLogDetails holdingLogDetails) {
    log.debug("Extract holdings from Evergreen marcxml record ${z_response}");
    if ( z_response?.numberOfRecords != 1 ) {
      log.warn("Multiple records seen in response from Evergreen Z39.50 server, unable to extract available items. Record: ${z_response}");
      return null;
    }

    List<ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.record != null ) {
      availability_summary = extractAvailableItemsFromMARCXMLRecord(z_response?.records?.record?.recordData?.record, reason, holdingLogDetails);
    }
    return availability_summary;

  }

  @Override
  public List<ItemLocation> extractAvailableItemsFromMARCXMLRecord(record, String reason, IHoldingLogDetails holdingLogDetails) {
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
    //             <datafield tag="852" ind1="4" ind2=" ">
    //              <subfield code="a">Current</subfield>
    //              <subfield code="b">EC-RC</subfield>
    //              <subfield code="c">CAR</subfield>
    //              <subfield code="d">EC-RC</subfield>
    //              <subfield code="g">32050009446276</subfield> <subfield code="n">Checked out</subfield>
    //            </datafield>
    log.debug("extractAvailableItemsFromMARCXMLRecord (EvergreenHostLMSService)");
    List<ItemLocation> availability_summary = [];
    holdingLogDetails.newRecord();
    record.datafield.each { df ->
      if ( df.'@tag' == "852" ) {
        holdingLogDetails.holdings(df);
        Map<String,String> tag_data = [:]
        df.subfield.each { sf ->
          if ( sf.@code != null ) {
            tag_data[ sf.'@code'.toString() ] = sf.toString()
          }
        }
        log.debug("Found holdings tag : ${df} ${tag_data}");
        try {
          if ( ![ 'Available' ].contains(tag_data['n']) ) {
            // $n contains item status which is not requestable
            log.debug("Item status not requestable - ${tag_data['n']}");
          }
          else {
            log.debug("Assuming ${tag_data['n']} is requestable status to update extractAvailableItemsFromMARCXMLRecord if not the case");
            availability_summary << new ItemLocation(
                    location: tag_data['b'],
                    shelvingLocation: tag_data['a'],
                    callNumber: tag_data['c'],
                    itemId: tag_data['g'] )
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
