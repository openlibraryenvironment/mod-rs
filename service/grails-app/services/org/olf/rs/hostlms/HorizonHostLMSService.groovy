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
}
