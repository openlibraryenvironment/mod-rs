package org.olf.rs.hostlms;

import org.olf.rs.circ.client.NCIPClientWrapper;

import org.olf.rs.lms.ItemLocation;
import org.olf.rs.circ.client.CirculationClient;

public class HorizonHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(String address) {
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [protocol: "NCIP1"]).circulationClient;
  }

  @Override
  protected String getHoldingsQueryRecsyn() {
    return 'marcxml';
  }

  @Override
  protected Map<String, ItemLocation> extractAvailableItemsFrom(z_response, String reason=null) {
    log.debug("Extract holdings from Horizon marcxml record ${z_response}");
    if ( (z_response?.numberOfRecords?.text() as int) != 1 ) {
      log.warn("Multiple records seen in response from Horizon Z39.50 server, unable to extract available items. Record: ${z_response}");
      return null;
    }

    Map<String, ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.record != null ) {
      availability_summary = extractAvailableItemsFromMARCXMLRecord(z_response?.records?.record?.recordData?.record, reason);
    }
    return availability_summary;

  }



}