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

    def prefix_query_string = "@attr 1=100 ${pr.supplierUniqueRecordId}";
    def z_response = z3950Service.query(prefix_query_string, 1, getHoldingsQueryRecsyn());
    log.debug("Got Z3950 response: ${z_response}");

    if ( z_response?.numberOfRecords == 1 ) {
      // Got exactly 1 record
      Map<String, ItemLocation> availability_summary = extractAvailableItemsFrom(z_response,"Match by @attr 1=100 ${pr.supplierUniqueRecordId}")
      if ( availability_summary?.size() > 0 ) {
        availability_summary.values().each { v ->
          result.add(v);
        }
      }
      else {
        log.debug("CQL lookup(${prefix_query_string}) returned ${z_response?.numberOfRecords} matches. Unable to determine availability");
      }

      log.debug("At end, availability summary: ${availability_summary}");
    }

    return result;
  }



}