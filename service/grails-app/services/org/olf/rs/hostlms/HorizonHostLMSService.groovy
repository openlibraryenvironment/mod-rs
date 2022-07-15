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
  //We need to also eliminate any holdings of type "Internet"
  protected Map<String, ItemLocation> extractAvailableItemsFrom(z_response, String reason=null) {
    Map<String, ItemLocation> availability_summary = null;
    if ( z_response?.records?.record?.recordData?.opacRecord != null ) {
      def withHoldings = z_response.records.record.findAll { it?.recordData?.opacRecord?.holdings?.holding?.size() > 0 &&
       it?.recordData?.opacRecord?.holdings?.holding?.localLocation.text() != "Internet" };
      if (withHoldings.size() < 1) {
        log.warn("BaseHostLMSService failed to find an OPAC record with holdings");
        return null;
      } else if (withHoldings.size() > 1) {
        log.warn("BaseHostLMSService found multiple OPAC records with holdings");
        return null;
      }
      log.debug("[BaseHostLMSService] Extract available items from OPAC record ${z_response}, reason: ${reason}");
      availability_summary = extractAvailableItemsFromOpacRecord(withHoldings?.first()?.recordData?.opacRecord, reason);
    }
    else {
      log.warn("BaseHostLMSService expected the response to contain an OPAC record, but none was found");
    }
    return availability_summary;
  }

  @Override
  public Map<String, ItemLocation> extractAvailableItemsFromOpacRecord(opacRecord, String reason=null) {

    log.debug("extractAvailableItemsFromOpacRecord (HorizonHostLMSService)");
    Map<String,ItemLocation> availability_summary = [:]

    opacRecord?.holdings?.holding?.each { hld ->
      log.debug("Holding record: ${hld}");
      hld.circulations?.circulation?.each { circ ->
        if (hld?.localLocation && circ?.availableNow?.@value == '1') {
          ItemLocation il = new ItemLocation(
                  reason: reason,
                  location: hld?.localLocation?.text(),
                  shelvingLocation: hld?.shelvingLocation?.text() ?: null,
                  itemLoanPolicy: hld?.shelvingData?.text()?.trim() ?: null,
                  callNumber: hld.callNumber);
          availability_summary[hld.localLocation] = il;
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