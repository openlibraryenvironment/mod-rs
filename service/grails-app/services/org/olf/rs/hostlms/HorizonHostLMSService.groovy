package org.olf.rs.hostlms


import org.olf.rs.circ.client.CirculationClient
import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.lms.ItemLocation
import org.olf.rs.logging.IHoldingLogDetails
import org.olf.rs.settings.ISettings

public class HorizonHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    // This wrapper creates the circulationClient we need
    return new NCIPClientWrapper(address, [protocol: "NCIP1"]).circulationClient;
  }

  @Override
  public boolean isManualCancelRequestItem() {
    return true;
  }

  @Override
  //We need to also eliminate any holdings of type "Internet"
  protected List<ItemLocation> extractAvailableItemsFrom(z_response, String reason, IHoldingLogDetails holdingLogDetails) {
    List<ItemLocation> availability_summary = [];
    if ( z_response?.records?.record?.recordData?.opacRecord != null ) {
      def withHoldings = z_response.records.record.findAll { it?.recordData?.opacRecord?.holdings?.holding?.size() > 0 &&
       it?.recordData?.opacRecord?.holdings?.holding?.localLocation.text() != "Internet" };

      // Log the holdings
      logOpacHoldings(withHoldings, holdingLogDetails);

      if (withHoldings.size() < 1) {
        log.warn("HorizonHostLMSService failed to find an OPAC record with holdings");
      } else if (withHoldings.size() > 1) {
        log.warn("HorizonHostLMSService found multiple OPAC records with holdings");
      } else {
        log.debug("[HorizonHostLMSService] Extract available items from OPAC record ${z_response}, reason: ${reason}");
        availability_summary = extractAvailableItemsFromOpacRecord(withHoldings?.first()?.recordData?.opacRecord, reason);
      }
    }
    else {
      log.warn("HorizonHostLMSService expected the response to contain an OPAC record, but none was found");
    }
    return availability_summary;
  }

  @Override
  public List<ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    List<ItemLocation> availability_summary = [];

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("HorizonHostLMSService holdings record:: ${hld}");
      hld.circulations?.circulation?.each { circ ->
        def loc = hld?.localLocation?.text()?.trim();
        if (loc && circ?.availableNow?.@value == '1') {
          log.debug("Horizon extractAvailableItemsFromOpacRecord Available now");

          if (hld?.shelvingData && hld.shelvingData?.text()?.trim().length()) {
            ItemLocation il = new ItemLocation(
                    reason: reason,
                    location: loc,
                    shelvingLocation: hld?.shelvingLocation?.text()?.trim() ?: null,
                    itemLoanPolicy: hld.shelvingData.text().trim(),
                    itemId: circ?.itemId?.text()?.trim() ?: null,
                    callNumber: hld?.callNumber?.text()?.trim() ?: null)
            availability_summary << il;
          }

          if (circ?.temporaryLocation && circ.temporaryLocation.text()?.trim().length()) {
            ItemLocation il = new ItemLocation(
                    reason: reason,
                    location: loc,
                    shelvingLocation: hld?.shelvingLocation?.text()?.trim() ?: null,
                    itemLoanPolicy: circ.temporaryLocation.text().trim(),
                    itemId: circ?.itemId?.text()?.trim() ?: null,
                    callNumber: hld?.callNumber?.text()?.trim() ?: null)
            availability_summary << il;
          }
        }
      }
    }

    return availability_summary;
  }
}
