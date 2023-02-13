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
 */
public class FolioHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(String address) {
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [protocol: "NCIP2"]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
  }

  /*

  Use temporaryLocation field in circulation record

  <holding>
   <typeOfRecord>c</typeOfRecord>
   <encodingLevel>3</encodingLevel>
   <receiptAcqStatus>1</receiptAcqStatus>
   <generalRetention />
   <completeness>n</completeness>
   <dateOfReport>00</dateOfReport>
   <nucCode>Cornell University</nucCode>
   <localLocation>Olin Library</localLocation>
   <shelvingLocation>Olin</shelvingLocation>
   <callNumber>CB19 .G69 2021</callNumber>
   <copyNumber>2</copyNumber>
   <circulations>
      <circulation>
         <availableNow value="1" />
         <itemId>31924128478918</itemId>
         <renewable value="0" />
         <onHold value="0" />
         <temporaryLocation>Olin Reserve</temporaryLocation>
      </circulation>
   </circulations>
  </holding>
  */

  @Override
  public List<ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    List<ItemLocation> availability_summary = [];

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("BaseHostLMSService holdings record:: ${hld}");
      hld.circulations?.circulation?.each { circ ->
        def loc = hld?.localLocation?.text()?.trim();
        if (loc && circ?.availableNow?.@value == '1') {
          log.debug("Folio extractAvailableItemsFromOpacRecord Available now");
          ItemLocation il = new ItemLocation(
                  reason: reason,
                  location: loc,
                  shelvingLocation: hld?.shelvingLocation?.text()?.trim() ?: null,
                  temporaryLocation: circ?.temporaryLocation?.text()?.trim() ?: null,
                  itemLoanPolicy: circ?.availableThru?.text()?.trim() ?: null,
                  itemId: circ?.itemId?.text()?.trim() ?: null,
                  callNumber: hld?.callNumber?.text()?.trim() ?: null)
          availability_summary << il;
        }
      }
    }

    return availability_summary;
  }

}
