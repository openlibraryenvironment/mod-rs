package org.olf.rs.hostlms;

import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.settings.ISettings;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class SierraHostLMSService extends BaseHostLMSService {

  List<String> NOTES_CONSIDERED_AVAILABLE = ['AVAILABLE', 'CHECK SHELVES'];

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    // This wrapper creates the circulationClient we need
    return new NCIPClientWrapper(address, [protocol: "NCIP2"]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
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
  public List<ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    List<ItemLocation> availability_summary = []

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("Process sierra OPAC holdings record:: ${hld}");
      def note = hld?.publicNote?.toString();
      if ( note && NOTES_CONSIDERED_AVAILABLE.contains(note) ) {
        log.debug("SIERRA OPAC Record: Item Available now");
        ItemLocation il = new ItemLocation(
                                            reason: reason,
                                            location: hld.localLocation?.toString(),
                                            shelvingLocation:hld.localLocation?.toString(),
                                            callNumber:hld?.callNumber?.toString() )
        availability_summary << il;
      }
    }

    return availability_summary;
  }
}
