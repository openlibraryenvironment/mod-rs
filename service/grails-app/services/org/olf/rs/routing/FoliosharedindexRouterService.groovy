package org.olf.rs.routing;

import org.olf.rs.routing.RequestRouter;
import org.olf.rs.routing.RankedSupplier;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.okapi.modules.directory.DirectoryEntry;
import org.olf.rs.AvailabilityStatement;
import org.olf.rs.SharedIndexService;
import org.olf.rs.StatisticsService
import org.olf.rs.DirectoryEntryService


public class FoliosharedindexRouterService implements RequestRouter {

  SharedIndexService sharedIndexService
  StatisticsService statisticsService
  DirectoryEntryService directoryEntryService

  public List<RankedSupplier> findMoreSuppliers(Map description, List<String> already_tried_symbols) {
    log.debug("FoliosharedindexRouterService::findMoreSuppliers");
    List<AvailabilityStatement> sia = sharedIndexService.getSharedIndexActions().findAppropriateCopies(description);
    return createRankedRota(sia);
  }

  public Map getRouterInfo() {
    return [
      'name':'FoliosharedindexRouterService',
      'description': 'Shared Index Routing Service'
    ]
  }



  /**
   * Take a list of availability statements and turn it into a ranked rota
   * @param sia - List of AvailabilityStatement
   * @return [
   *   [
   *     symbol:
   *   ]
   * ]
   */
  private List<RankedSupplier> createRankedRota(List<AvailabilityStatement> sia) {
    log.debug("createRankedRota(${sia})");
    List<RankedSupplier> result = new ArrayList<RankedSupplier>()

    sia.each { av_stmt ->
      log.debug("Considering rota entry: ${av_stmt}");

      // 1. look up the directory entry for the symbol
      Symbol s = ( av_stmt.symbol != null ) ? directoryEntryService.resolveCombinedSymbol(av_stmt.symbol) : null;

      if ( s != null ) {
        log.debug("Refine availability statement ${av_stmt} for symbol ${s}");

        // 2. See if the entry has policy.ill.loan_policy set to "Not Lending" - if so - skip
        // s.owner.customProperties is a container :: com.k_int.web.toolkit.custprops.types.CustomPropertyContainer
        def isLending = directoryEntryService.directoryEntryIsLending(s.owner);

        if ( isLending ) {
          Map peer_stats = statisticsService.getStatsFor(s);

          def loadBalancingScore = null;
          def loadBalancingReason = null;
          def ownerStatus = s.owner?.status?.value;
          log.debug("Found status of ${ownerStatus} for symbol ${s}");

          if ( ownerStatus == null ) {
            log.debug("Unable to get owner status for ${s}");
          } 

          if ( ownerStatus != null && ( ownerStatus == "Managed" || ownerStatus == "managed" )) {
            loadBalancingScore = 10000;
            loadBalancingReason = "Local lending sources prioritized";
          } else if ( peer_stats != null ) {
            // 3. See if we can locate load balancing informaiton for the entry - if so, calculate a score, if not, set to 0
            double lbr = peer_stats.lbr_loan/peer_stats.lbr_borrow
            long target_lending = peer_stats.current_borrowing_level*lbr
            loadBalancingScore = target_lending - peer_stats.current_loan_level
            loadBalancingReason = "LB Ratio ${peer_stats.lbr_loan}:${peer_stats.lbr_borrow}=${lbr}. Actual Borrowing=${peer_stats.current_borrowing_level}. Target loans=${target_lending} Actual loans=${peer_stats.current_loan_level} Distance/Score=${loadBalancingScore}";
          } else {
            loadBalancingScore = 0;
            loadBalancingReason = 'No load balancing information available for peer'
          }

          RankedSupplier rota_entry = new RankedSupplier( 
                                               supplier_symbol: av_stmt.symbol,
                                               instance_identifier: av_stmt.instanceIdentifier,
                                               copy_identifier: av_stmt.copyIdentifier,
                                               ill_policy: av_stmt.illPolicy,
                                               rank: loadBalancingScore,
                                               rankReason: loadBalancingReason )
          result.add(rota_entry)
        }
        else {
          def entry_loan_policy = directoryEntryService.parseCustomPropertyValue(s.owner, directoryEntryService.ill_policy_custprop_key)
          log.debug("Directory entry says not currently lending - ${av_stmt.symbol}/policy=${entry_loan_policy}");
        }
      }
      else {
        log.debug("Unable to locate symbol ${av_stmt.symbol}");
      } 
    }
    
    def sorted_result = result.toSorted { a,b -> b.rank <=> a.rank }
    log.debug("createRankedRota returns ${sorted_result}");
    return sorted_result;
  }
}
