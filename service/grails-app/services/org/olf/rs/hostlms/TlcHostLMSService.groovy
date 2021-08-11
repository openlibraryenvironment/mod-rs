package org.olf.rs.hostlms;

import org.olf.rs.PatronRequest
import groovyx.net.http.HttpBuilder
import org.olf.rs.ItemLocation;
import org.olf.rs.statemodel.Status;
import com.k_int.web.toolkit.settings.AppSetting
import groovy.xml.StreamingMarkupBuilder
import static groovyx.net.http.HttpBuilder.configure
import groovyx.net.http.FromServer;
import com.k_int.web.toolkit.refdata.RefdataValue
import static groovyx.net.http.ContentTypes.XML
import org.olf.rs.lms.ItemLocation;
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

public class TlcHostLMSService extends BaseHostLMSService {


  @Override
  protected String getHoldingsQueryRecsyn() {
    return 'marcxml';
  }

  public CirculationClient getCirculationClient(String address) {
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [protocol: "NCIP2"]).circulationClient;
  }

  @Override
  protected Map<String, ItemLocation> extractAvailableItemsFrom(z_response, String reason=null) {
    log.debug("Extract available items from TLC marcxml record ${z_response}, reason ${reason}");

    Map<String, ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.record != null ) {
      availability_summary = extractAvailableItemsFromMARCXMLRecord(z_response?.records?.record?.recordData?.record, reason);
    }
    return availability_summary;

  }



  
  @Override
  public Map<String, ItemLocation> extractAvailableItemsFromMARCXMLRecord(record, String reason=null) {
    //<zs:searchRetrieveResponse xmlns:zs="http://docs.oasis-open.org/ns/search-ws/sruResponse">
    //  <zs:numberOfRecords>1359</zs:numberOfRecords>
    //  <zs:records>
    //    <zs:record>
    //      <zs:recordSchema>marcxml</zs:recordSchema>
    //      <zs:recordXMLEscaping>xml</zs:recordXMLEscaping>
    //      <zs:recordData>
    //        <record xmlns="http://www.loc.gov/MARC21/slim">
    //        <leader>02304nam a22005051i 4500</leader>
    //        <controlfield tag="001">ebc1823214</controlfield>
    //        <controlfield tag="003">NhCcYBP</controlfield>
    //        <controlfield tag="005">20170628225136.0</controlfield>
    //        <controlfield tag="006">m |o d | </controlfield>
    //        <controlfield tag="007">cr |n|||||||||</controlfield>
    //        <controlfield tag="008">170604s2014 ilua ob 001 0 eng d</controlfield>
    //        <datafield tag="982" ind1=" " ind2=" ">
    //          <subfield code="1">y</subfield>
    //          <subfield code="a">PDA</subfield>
    //          <subfield code="b">Snowden Library</subfield>
    //          <subfield code="c">E-BOOK (DDA)</subfield>
    //          <subfield code="i">ebc1823214</subfield>
    //          <subfield code="m"/>
    //          <subfield code="s">A</subfield>
    //        </datafield>
    log.debug("extractAvailableItemsFromMARCXMLRecord (TlcHostLMSService)");
    Map<String, ItemLocation> availability_summary = [:];
    record.datafield.each { df ->
      if( df.'@tag' == "982") {
        Map<String,String> tag_data = [:];
        df.subfield.each { sf ->
          if( sf.@code != null ) {
            tag_data[ sf.'@code'.toString() ] = sf.toString();
          }
        }
        log.debug("Found holdings (982) tag: ${df} ${tag_data}");
        try {
          if( tag_data['1'] == 'y') {
            log.debug("Available now");
            def location = tag_data['b'];
            def shelvingLocation = tag_data['c'];
            ItemLocation il = new ItemLocation( location: location, shelvingLocation: shelvingLocation );
            availability_summary[location] = il;
          }
        } catch(Exception e) {
          log.debug("Unable to parse holdings (982): ${e.message}");
        }
        
      }
    }
    log.debug("Tlc Host availability: ${availability_summary}")
    return availability_summary;

  }

}