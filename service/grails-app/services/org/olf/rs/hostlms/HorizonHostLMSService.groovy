package org.olf.rs.hostlms;

import org.olf.rs.circ.client.NCIPClientWrapper;

import org.olf.rs.lms.ItemLocation;
import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.PatronRequest;

public class HorizonHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(String address) {
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [protocol: "NCIP1"]).circulationClient;
  }

  @Override
  public Map<String, ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    log.debug("extractAvailableItemsFromOpacRecord (HorizonHostLMSService)");
    Map<String,ItemLocation> availability_summary = [:]

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("Holding record: ${hld}");
      log.debug("${hld.circulations?.circulation?.availableNow}");
      log.debug("${hld.circulations?.circulation?.availableNow?.@value}");
      if ( hld.circulations?.circulation?.availableNow?.@value=='1' ) {
        log.debug("Available now");
        def shelvingLocation = hld?.shelvingLocation?.text();
        def location = hld?.localLocation?.text();
        if(!shelvingLocation) {
          shelvingLocation = null; //No blank strings
        }
        /*
        def locParts = splitLocation(hld.localLocation?.text());
        log.debug("splitLocation returned ${locParts}");
        if(locParts) {
          location = locParts[0];
          shelvingLocation = locParts[1];
        }
        */
        log.debug("Creating new ItemLocation with fields location: ${location}, shelvingLocation: ${shelvingLocation}, callNumber: ${hld.callNumber}");
        ItemLocation il = new ItemLocation( reason: reason, location: location, shelvingLocation: shelvingLocation, callNumber:hld.callNumber )
        availability_summary[hld.localLocation] = il;
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

  @Override
  public List<ItemLocation> z3950ItemsByIdentifier(PatronRequest pr) {

    List<ItemLocation> result = [];

    String z3950_proxy = 'http://reshare-mp.folio-dev.indexdata.com:9000';
    String z3950_server = getZ3950Server();

    if ( z3950_server != null ) {
      // log.debug("Sending system id query ${z3950_proxy}?x-target=http://temple-psb.alma.exlibrisgroup.com:1921/01TULI_INST&x-pquery=@attr 1=12 ${pr.supplierUniqueRecordId}");
      log.debug("Sending system id query ${z3950_proxy}?x-target=${z3950_server}&x-pquery=@attr 1=100 ${pr.supplierUniqueRecordId}");

      def z_response = HttpBuilder.configure {
        request.uri = z3950_proxy
      }.get {
          request.uri.path = '/'
          // request.uri.query = ['x-target': 'http://aleph.library.nyu.edu:9992/TNSEZB',
          request.uri.query = ['x-target': z3950_server,
                               'x-pquery': '@attr 1=100 '+pr.supplierUniqueRecordId,
                               'maximumRecords':'1' ]

          if ( getHoldingsQueryRecsyn() ) {
            request.uri.query['recordSchema'] = getHoldingsQueryRecsyn();
          }

          log.debug("Querying z server with URL ${request.uri?.toURI().toString()}")
      }

      log.debug("Got Z3950 response: ${z_response}");

      if ( z_response?.numberOfRecords == 1 ) {
        // Got exactly 1 record
        Map<String, ItemLocation> availability_summary = extractAvailableItemsFrom(z_response,"Match by @attr 1=12 ${pr.supplierUniqueRecordId}")
        if ( availability_summary.size() > 0 ) {
          availability_summary.values().each { v ->
            result.add(v);
          }
        }

        log.debug("At end, availability summary: ${availability_summary}");
      }
    }

    return result;
  }



}