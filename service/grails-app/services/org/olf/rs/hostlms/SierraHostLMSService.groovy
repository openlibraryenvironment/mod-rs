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



/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class SierraHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(String address) {
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [protocol: "NCIP2"]).circulationClient;
  }

  /**
   * III Sierra doesn't provide an availableNow flag in it's holdings record - instead the XML looks as followS:
   * <holdings>
   *   <holding>
   *     <localLocation>Gumberg Silverman Phen General - 1st Floor</localLocation>
   *     <callNumber>B3279.H94 T756 2021 </callNumber>
   *     <publicNote>AVAILABLE</publicNote>
   *   </holding>
   * </holdings>
   *
   * We are taking publicNote==AVAILABLE as an indication of an available copy
   */
  @Override
  public Map<String, ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    Map<String,ItemLocation> availability_summary = [:]

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("Process sierra OPAC holdings record:: ${hld}");
      if ( hld.publicNote?.toString() == 'AVAILABLE' ) {
        log.debug("SIERRA OPAC Record: Item Available now");
        ItemLocation il = new ItemLocation( 
                                            reason: reason,
                                            location: hld.localLocation?.toString(), 
                                            shelvingLocation:hld.localLocation?.toString(), 
                                            callNumber:hld.callNumber?.toString() )
        availability_summary[hld.localLocation?.toString()] = il;
      }
    }

    return availability_summary;
  }

}
