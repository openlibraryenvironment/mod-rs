package org.olf.rs.hostlms;

import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.settings.ISettings;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class MillenniumHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    // This wrapper creates the circulationClient we need
    return new NCIPClientWrapper(address, [protocol: "NCIP2"]).circulationClient;
  }

  /**
   * Copied from Sierra
   */
  @Override
  public List<ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    List<ItemLocation> availability_summary = []

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("${hld}");
      if ( hld.publicNote?.toString() == 'AVAILABLE' ) {
        log.debug("Available now");
        ItemLocation il = new ItemLocation(
                                            reason: reason,
                                            location: hld.localLocation?.toString(),
                                            shelvingLocation:hld.localLocation?.toString(),
                                            callNumber:hld.callNumber?.toString() )
        availability_summary << il;
      }
    }

    return availability_summary;
  }
}
