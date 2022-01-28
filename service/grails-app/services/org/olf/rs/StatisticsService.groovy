package org.olf.rs

import org.olf.rs.shared.TenantSymbolMapping
import grails.gorm.multitenancy.*
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.olf.rs.Counter
import org.olf.okapi.modules.directory.Symbol;
// import java.util.concurrent.ThreadLocalRandom;
import groovy.json.JsonSlurper
import org.olf.okapi.modules.directory.ServiceAccount


/**
 * This service takes responsibility for assembling/aggregating all the data needed
 * to build a ranked rota entry.
 */
public class StatisticsService {

  def grailsApplication
  def dataSource
  private static final long MAX_CACHE_AGE = 60 * 5 * 1000; // 5mins

  private Map<String, Map> stats_cache = [:]

  public incrementCounter(String context) {
    Counter c = Counter.findByContext(context) ?: new Counter(context:context, value:0)
    c.value++
    c.save(flush:true, failOnError:true);
  }

  public decrementCounter(String context) {
    Counter c = Counter.findByContext(context) ?: new Counter(context:context, value:0)
    c.value--
    c.save(flush:true, failOnError:true);
  }

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
          def ratio_custprop = symbol.owner.customProperties.value.find { it.definition?.name == 'policy.ill.InstitutionalLoanToBorrowRatio' }
          String ratio = ratio_custprop?.value

          if ( ratio == null ) {
            log.warn('Unable to find policy.ill.InstitutionalLoanToBorrowRatio in custom properties, using 1:1 as a default');
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
    if ( current_stats.requestsByTag != null )
      return processDynamicRatioInfo(current_stats,ratio);
    else
      return processCounterBasedRatioInfo(current_stats,ratio);
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
      long current_loans = current_stats.requestsByTag.ACTIVE_LOAN
      long current_borrowing = current_stats.requestsByTag.BORROW
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

  public Map processCounterBasedRatioInfo(Map current_stats, String ratio) {

    log.debug("Loan to borrow ratio is : ${ratio}");
    log.debug("Stats output is : ${current_stats}");

    Map result = null;

    String[] parsed_ratio = ratio.split(':')
    long ratio_loan = Long.parseLong(parsed_ratio[0])
    long ratio_borrow = Long.parseLong(parsed_ratio[1])

    if ( current_stats ) {
      long current_loans = current_stats.current.find { it.context=='/activeLoans' } ?.value
      long current_borrowing = current_stats.current.find { it.context=='/activeBorrowing' } ?.value
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
}

