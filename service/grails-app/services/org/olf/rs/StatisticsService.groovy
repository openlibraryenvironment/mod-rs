package org.olf.rs

import org.olf.okapi.modules.directory.ServiceAccount;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.constants.Directory;

import groovy.json.JsonSlurper;

/**
 * This service takes responsibility for assembling/aggregating all the data needed
 * to build a ranked rota entry.
 */
public class StatisticsService {

  def grailsApplication
  def dataSource
  private static final long MAX_CACHE_AGE = 60 * 5 * 1000; // 5mins

  private Map<String, Map> stats_cache = [:]


  /**
   * Given a symbol, try to retrieve the stats for a symbol - if needed, refresh the cache
   */
  public Map<String,Object> getStatsFor(Symbol symbol) {

    log.debug("StatisticsService::getStatsFor(${symbol})");

    Map result = null;
    try {
      result = refreshStatsFor(symbol);
    }
    catch ( Exception e ) {
      log.error("problem fetching stats for ${symbol}", e);
    }

    log.debug("getStatsFor(${symbol}) returns ${result}");
    return result;
  }

  public Map<String, Object> refreshStatsFor(Symbol symbol) {

    log.debug("StatisticsService::refreshStatsForrefreshStatsFor(${symbol})");
    Map result = null;

    if ( symbol != null ) {
      String symbol_str = "${symbol.authority.symbol}:${symbol.symbol}".toString()
      result = stats_cache[symbol_str]

      if ( ( result == null ) ||
           ( System.currentTimeMillis() - result.timestamp > MAX_CACHE_AGE ) ) {

        // symbol.owner.customProperties.value.each { it ->
        //   log.debug("refreshStatsFor cp ${it}");
        // }

        // symbol.owner.services.each { it ->
        //   log.debug("refreshStatsFor service ${it}");
        // }

        // symbol.owner.customProperties is a CustomPropertyContainer which means it's value is a list of custom properties
        try {
          def ratio_custprop = symbol.owner.customProperties.value.find { it.definition?.name == Directory.KEY_ILL_POLICY_BORROW_RATIO };
          String ratio = ratio_custprop?.value

          if ( ratio == null ) {
            log.warn('Unable to find ${Directory.KEY_ILL_POLICY_BORROW_RATIO} in custom properties, using 1:1 as a default');
            ratio = "1:1"
            symbol.owner.customProperties.value.each { cp ->
              log.info("    custom properties that are present: ${cp.definition?.name} -> ${cp.value}");
            }
          }

          ServiceAccount sa = symbol.owner.services.find { it.service.businessFunction?.value?.toUpperCase() == 'RS_STATS' }
          String stats_url = sa?.service.address

          log.debug("URL for stats is : ${stats_url}, ratio is ${ratio}");

          if ( ( ratio != null ) && ( stats_url != null ) ) {
            String stats_json = new java.net.URL(stats_url).text
            result = processRatioInfo(stats_json, ratio);
            stats_cache[symbol_str] = result;
          }
          else {
            log.warn("No stats service available for ${symbol}. Found the following services");
            symbol.owner.services.each {
              log.warn("    -> declared service: ${it.service.businessFunction}/${it.service.businessFunction?.value} != RS_STATS");
            }

            // No stats available so return data which will place this symbol at parity
            result = [
              timestamp: System.currentTimeMillis(),
              lbr_loan:1,
              lbr_borrow:1,
              current_loan_level:1,
              current_borrowing_level:1,
              reason:'No stats service available'
            ]

          }
        }
        catch ( Exception e ) {
          log.error("Exception processing stats",e);
        }
      }
      log.debug("Result of refreshStatsFor : ${result}");
    }
    return result;
  }

  public Map processRatioInfo(String stats_json, String ratio) {
    def current_stats = new JsonSlurper().parseText(stats_json)
    return processDynamicRatioInfo(current_stats,ratio);
  }

  // Extract into more testable lump
  public Map processDynamicRatioInfo(Map current_stats, String ratio) {
    log.debug("Loan to borrow ratio is : ${ratio}");
    log.debug("Stats output is : ${current_stats}");

    Map result = null;

    String[] parsed_ratio = ratio.split(':')
    long ratio_loan = Long.parseLong(parsed_ratio[0])
    long ratio_borrow = Long.parseLong(parsed_ratio[1])

    if ( current_stats ) {
      long current_loans = current_stats.requestsByTag.ACTIVE_LOAN ?: 0
      long current_borrowing = current_stats.requestsByTag.ACTIVE_BORROW ?: 0
      result = [
        timestamp: System.currentTimeMillis(),
        lbr_loan:ratio_loan,
        lbr_borrow:ratio_borrow,
        current_loan_level:current_loans,
        current_borrowing_level:current_borrowing,
        reason:'Statistics collected from stats service'
      ]
    }
    return result;
  }


  public Map generateRequestsByState() {
    Map result = [:]
    PatronRequest.executeQuery('select pr.stateModel.shortcode, pr.state.code, count(pr.id) from PatronRequest as pr group by pr.stateModel.shortcode, pr.state.code').each { sl ->
      result[sl[0]+':'+sl[1]] = sl[2]
    }
    return result;
  }

  public Map generateRequestsByStateTag() {
    Map result = [:]
    PatronRequest.executeQuery('select tag.value, count(pr.id) from PatronRequest as pr join pr.state.tags as tag group by tag.value').each { sl ->
      result[sl[0]] = sl[1]
    }
    return result;
  }
}

