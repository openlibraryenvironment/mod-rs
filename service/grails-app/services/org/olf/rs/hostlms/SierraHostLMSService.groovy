package org.olf.rs.hostlms

import org.olf.rs.SettingsService;
import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper;
import org.olf.rs.lms.ItemLocation
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.ISettings;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class SierraHostLMSService extends BaseHostLMSService {

  SettingsService settingsService;
  List<String> NOTES_CONSIDERED_AVAILABLE = ['AVAILABLE', 'CHECK SHELVES', 'CHECK SHELF'];

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

  @Override
  public String getRequestItemRequestScopeType() {
    return "Title";
  }

  @Override
  public String getRequestItemPickupLocation() {
    String pickupLocation = settingsService.getSettingValue(SettingsData.SETTING_NCIP_REQUEST_ITEM_PICKUP_LOCATION);
    if (pickupLocation != null && !pickupLocation.isEmpty()) {
      return pickupLocation;
    }
    return null;
  }

  @Override
  public String getRequestItemRequestType() {
    return "Hold";
  }

  @Override
  public String filterRequestItemItemId(String itemId) {
    if (itemId?.startsWith(".b")) {
      itemId = itemId.split(".b", 2)[1];
    }
    if (itemId.length() > 1) {
      String lastChar = itemId.substring(itemId.length() - 1);
      if (lastChar.matches("\\d")) {
        itemId = itemId.substring(0, itemId.length() - 1);
      }
    }
    return itemId;
  }
}
