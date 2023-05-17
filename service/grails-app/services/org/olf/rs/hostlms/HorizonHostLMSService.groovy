package org.olf.rs.hostlms;

import org.olf.rs.PatronRequest;
import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.logging.IHoldingLogDetails;
import org.olf.rs.settings.ISettings;

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

  public static List<String> splitLocation(String loc) {
    def pattern = /(.+)\s+-\s+(.+)/;
    def matcher = loc =~ pattern;
    if(matcher.find()) {
      return [ matcher.group(1), matcher.group(2)];
    }
    return null;
  }

  @Override
  public List<ItemLocation> z3950ItemsByIdentifier(PatronRequest pr, ISettings settings, IHoldingLogDetails holdingLogDetails) {

    List<ItemLocation> result = [];

    def prefix_query_string = "@attr 1=100 ${pr.supplierUniqueRecordId}";
    def z_response = z3950Service.query(settings, prefix_query_string, 1, getHoldingsQueryRecsyn(), holdingLogDetails);
    log.debug("Got Z3950 response: ${z_response}");

    if ( z_response?.numberOfRecords == 1 ) {
      // Got exactly 1 record
      List<ItemLocation> availability_summary = extractAvailableItemsFrom(z_response,"Match by @attr 1=100 ${pr.supplierUniqueRecordId}", holdingLogDetails)
      if ( availability_summary?.size() > 0 ) {
        result = availability_summary;
      }
      else {
        log.debug("CQL lookup(${prefix_query_string}) returned ${z_response?.numberOfRecords} matches. Unable to determine availability");
      }

      log.debug("At end, availability summary: ${availability_summary}");
    }

    return result;
  }
}
