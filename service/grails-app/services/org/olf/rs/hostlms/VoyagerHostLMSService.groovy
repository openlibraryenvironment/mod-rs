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

import java.text.Normalizer;



/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class VoyagerHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(String address) {
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [protocol: "NCIP1_SOCKET", strictSocket:true]).circulationClient;
  }

  @Override
  /*
    Instead of having using a localLocation and a shelvingLocation field, the Voyager result
    simply stores both in the localLocation field and separates them with a dash. If there is
    no dash-separated name, assume it is only the location and there is no shelving location
    provided. In the event of not finding a shelving location in the localLocation field, we'll
    check for a shelvingLocation field just in case it exists
  */
  public Map<String, ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    Map<String,ItemLocation> availability_summary = [:]

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("VoyagerHostLMSService holdings record :: ${hld}");
      def location = hld.localLocation?.text()?.trim();
      def shelvingLocation = hld.shelvingLocation?.text()?.trim() ?: null;
      def locParts = splitLocation(location);
      log.debug("splitLocation returned ${locParts}");
      if (locParts) {
        location = locParts[0];
        shelvingLocation = locParts[1];
      }

      hld.circulations?.circulation?.each { circ ->
        if (location && circ?.availableNow?.@value == '1') {
          log.debug("Available now");
          ItemLocation il = new ItemLocation(
            reason: reason,
            location: location,
            shelvingLocation: shelvingLocation,
            itemId: circ?.itemId?.text()?.trim() ?: null,
            callNumber: hld?.callNumber?.text()?.trim() ?: null)
          availability_summary[location] = il;
        }
      }
    }

    return availability_summary;
  }

  public static List<String> splitLocation(String loc) {
    def pattern = /(.+)\s+-\s+(.+)/;
    def matcher = loc =~ pattern;
    if(matcher.find()) {
      return [ matcher.group(1), matcher.group(2)];
    }
    return null;
  }

  public Map acceptItem(String item_id,
                        String request_id,
                        String user_id,
                        String author,
                        String title,
                        String isbn,
                        String call_number,
                        String pickup_location,
                        String requested_action) {
    log.debug("Calling VoyagerHostLMSService::acceptItem");
    return super.acceptItem(item_id, request_id, user_id, stripDiacritics(author),
     stripDiacritics(title), isbn, call_number, pickup_location, requested_action);

  }

  private String stripDiacritics(String input) {
    input = Normalizer.normalize(input, Normalizer.Form.NFD);
    input = input.replaceAll("[^\\p{ASCII}]", ""); //Remove all non ASCII chars
    return input;
  }

}
