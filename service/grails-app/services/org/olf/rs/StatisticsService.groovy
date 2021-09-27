package org.olf.rs

import org.olf.rs.shared.TenantSymbolMapping
import grails.gorm.multitenancy.*
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.olf.rs.Counter
import org.olf.okapi.modules.directory.Symbol;
// import java.util.concurrent.ThreadLocalRandom;
import groovy.json.JsonSlurper


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
  
        // symbol.owner.customProperties is a CustomPropertyContainer which means it's a list of custom properties
        try {
          def ratio = symbol.owner.customProperties.value.find { it.definition?.name == 'policy.ill.InstitutionalLoanToBorrowRatio' }
          def stats_url = symbol.owner.services.find { it.service.businessFunction?.value == 'RS_STATS' }
          log.debug("URL for stats is : ${stats_url}, ratio is ${ratio}");

          if ( stats_url ) {
            String stats_json = new java.net.URL(stats_url).text
            result = processRatioInfo(stats_json, ratio);
            stats_cache[symbol_str] = result;
          }
        }
        catch ( Exception e ) {
          e.printStackTrace();
        }
      }

      log.debug("Result of refreshStatsFor : ${result}");
    }
    return result;
  }

  // Extract into more testable lump
  public Map processRatioInfo(String stats_json, String ratio) {
    log.debug("Loan to borrow ratio is : ${ratio}");
    log.debug("Stats output is : ${stats_json}");

    def current_stats = new JsonSlurper().parseText(stats_json)

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
        current_borrowing_level:current_borrowing
      ]
    }
    return result;
  }
}

