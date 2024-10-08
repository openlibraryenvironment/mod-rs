package org.olf.rs.hostlms;

import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.lms.ConnectionDetailsNCIP;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.settings.ISettings;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class FolioHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    // This wrapper creates the circulationClient we need
    return new NCIPClientWrapper(address, [protocol: "NCIP2"]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
  }

  @Override
  public String getRequestItemRequestType() {
    return "Page";
  }

  @Override
  public String getRequestItemRequestScopeType(ConnectionDetailsNCIP ncipConnectionDetails) {
    return ncipConnectionDetails.useTitle ? "Title" : "Item"
  }

  @Override
  public String getRequestItemPickupLocation(String pickupLocation) {
    return pickupLocation;
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
                  temporaryShelvingLocation: circ?.temporaryLocation?.text()?.trim() ?: null,
                  itemLoanPolicy: circ?.availableThru?.text()?.trim() ?: null,
                  itemId: circ?.itemId?.text()?.trim() ?: null,
                  callNumber: hld?.callNumber?.text()?.trim() ?: null)
          availability_summary << il;
        }
      }
    }

    return availability_summary;
  }

  @Override
  boolean isManualCancelRequestItem() {
    return true
  }
}
