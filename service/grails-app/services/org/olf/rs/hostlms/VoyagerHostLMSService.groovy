package org.olf.rs.hostlms;

import static groovyx.net.http.HttpBuilder.configure

import java.text.Normalizer;

import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.ISettings;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class VoyagerHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    // This wrapper creates the circulationClient we need
    return new NCIPClientWrapper(address, [protocol: "NCIP1_SOCKET", strictSocket:true]).circulationClient;
  }

  @Override
  public List<ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    List<ItemLocation> availability_summary = [];

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("VoyagerHostLMSService holdings record :: ${hld}");
      hld.circulations?.circulation?.each { circ ->
        def loc = hld?.localLocation?.text()?.trim();
        if (loc && circ?.availableNow?.@value == '1') {
          log.debug("Available now");
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


  //Use the Voyager API to get the barcode for the ItemLocation's itemId and replace
  //the field value with the barcode instead
  @Override
  public ItemLocation enrichItemLocation(ISettings settings, ItemLocation location) {
    log.debug("Calling VoyagerHostLMSService::enrichItemLocation()");
    if ( location == null ) {
      return null;
    }
    String voyager_item_api_address = settings.getSettingValue(SettingsData.SETTING_VOYAGER_ITEM_API_ADDRESS);
    String ncip_server_address = settings.getSettingValue(SettingsData.SETTING_NCIP_SERVER_ADDRESS);
    String barcode = null;
    try {
      URI barcodeLookupURI = null;
      String barcodeLookupPath = "/vxws/item/" + location.itemId;
      String query = "view=brief";
      if (voyager_item_api_address == null || voyager_item_api_address.isEmpty()) {
        URI ncipURI = new URI(ncip_server_address);
        int barcodeLookupPort = 7014;
        String scheme = ncipURI.getScheme();
        if (scheme != "http" || scheme != "https") {
          scheme = "http";
        }
        barcodeLookupURI = new URI(
          scheme, //scheme
          null, //userInfo
          ncipURI.getHost(), //host
          barcodeLookupPort, //port
          barcodeLookupPath, //path
          query, //query
          null //fragment
        );
      } else {
        URI voyager_lookup_uri = new URI(voyager_item_api_address);
        barcodeLookupURI = new URI(
          voyager_lookup_uri.getScheme(), //scheme
          null, //userInfo
          voyager_lookup_uri.getHost(), //host
          voyager_lookup_uri.getPort(), //port
          barcodeLookupPath, //path
          query, //query
          null //fragment
        );
      }
      log.debug("Voyager barcode lookup url is " + barcodeLookupURI.toString());
      barcode = lookupBarcode(barcodeLookupURI.toString());
    } catch ( Exception e ) {
      log.error("Unable to lookup barcode for item id " + location?.itemId + ":" + e.getLocalizedMessage());
    }

    if (barcode != null) {
      log.debug("Setting ItemLocation itemId to ${barcode}");
      location.itemId = barcode;
    }

    return location;

  }

  private String lookupBarcode(String lookupURL) {
    def httpBuilder = configure {
      request.uri = lookupURL;
    };
    log.debug("Contacting Voyager API at ${lookupURL}");
    def voyagerResponse = httpBuilder.get();
    log.debug("Got response from Voyager API: ${voyagerResponse}");
    String barcode = voyagerResponse?.item?.itemData?.find{ it.@name=="itemBarcode" }?.text();
    return barcode;
  }

  public Map acceptItem(ISettings settings,
                        String item_id,
                        String request_id,
                        String user_id,
                        String author,
                        String title,
                        String isbn,
                        String call_number,
                        String pickup_location,
                        String requested_action) {
    log.debug("Calling VoyagerHostLMSService::acceptItem");
    return super.acceptItem(settings, item_id, request_id, user_id, stripDiacritics(author),
     stripDiacritics(title), isbn, call_number, pickup_location, requested_action);

  }

  private String stripDiacritics(String input) {
    if ( input == null ) {
      return null;
    }
    input = Normalizer.normalize(input, Normalizer.Form.NFD);
    input = input.replaceAll("[^\\p{ASCII}]", ""); //Remove all non ASCII chars
    return input;
  }

}
