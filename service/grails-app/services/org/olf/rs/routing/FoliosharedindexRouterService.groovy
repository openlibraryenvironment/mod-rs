package org.olf.rs.routing;

import org.olf.rs.routing.RequestRouter;
import org.olf.rs.routing.RankedSupplier;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.okapi.modules.directory.DirectoryEntry;
import org.olf.rs.AvailabilityStatement;
import org.olf.rs.SharedIndexService;

public class FoliosharedindexRouterService implements RequestRouter {

  SharedIndexService sharedIndexService

  public List<RankedSupplier> findMoreSuppliers(Map description, List<String> already_tried_symbols) {
    List<AvailabilityStatement> sia = sharedIndexService.getSharedIndexActions().findAppropriateCopies(description);
    return createRankedRota(sia);
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
      Symbol s = ( av_stmt.symbol != null ) ? resolveCombinedSymbol(av_stmt.symbol) : null;

      if ( s != null ) {
        log.debug("Refine availability statement ${av_stmt} for symbol ${s}");

        // 2. See if the entry has policy.ill.loan_policy set to "Not Lending" - if so - skip
        // s.owner.customProperties is a container :: com.k_int.web.toolkit.custprops.types.CustomPropertyContainer
        def entry_loan_policy = s.owner.customProperties?.value?.find { it.definition.name=='ill.loan_policy' }
        log.debug("Symbols.owner.custprops['ill.loan_policy] : ${entry_loan_policy}");

        if ( ( entry_loan_policy == null ) ||
             ( entry_loan_policy.value?.value == 'lending_all_types' ) ) {

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
          log.debug("Directory entry says not currently lending - ${av_stmt.symbol}/policy=${entry_loan_policy.value?.value}");
        }
      }
      else {
        log.debug("Unable to locate symbol ${av_stmt.symbol}");
      } 
    }
    
    def sorted_result = result.toSorted { a,b -> b.loadBalancingScore <=> a.loadBalancingScore }
    log.debug("createRankedRota returns ${sorted_result}");
    return sorted_result;
  }

  public Symbol resolveCombinedSymbol(String combinedString) {
    Symbol result = null;
    if ( combinedString != null ) {
      String[] name_components = combinedString.split(':');
      if ( name_components.length == 2 ) {
        result = resolveSymbol(name_components[0], name_components[1]);
      }
    }
    return result;
  }

  public Symbol resolveSymbol(String authorty, String symbol) {
    Symbol result = null;
    List<Symbol> symbol_list = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol = :authority and s.symbol = :symbol',
                                                   [authority:authorty?.toUpperCase(), symbol:symbol?.toUpperCase()]);
    if ( symbol_list.size() == 1 ) {
      result = symbol_list.get(0);
    }

    return result;
  }

}
