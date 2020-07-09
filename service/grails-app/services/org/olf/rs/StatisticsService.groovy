package org.olf.rs

import org.olf.rs.shared.TenantSymbolMapping
import grails.gorm.multitenancy.*
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.olf.rs.Counter
import org.olf.okapi.modules.directory.Symbol;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This service takes responsibility for assembling/aggregating all the data needed
 * to build a ranked rota entry.
 */
public class StatisticsService {

  def grailsApplication
  def dataSource

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

  /**
   * Dummy implementation
   */
  public Map<String, Object> refreshStatsFor(Symbol symbol) {

    log.debug("StatisticsService::refreshStatsForrefreshStatsFor(${symbol})");

    // symbol.owner.customProperties.value.each { it ->
    //   log.debug("refreshStatsFor cp ${it}");
    // }

    // symbol.owner.services.each { it ->
    //   log.debug("refreshStatsFor service ${it}");
    // }

    // symbol.owner.customProperties is a CustomPropertyContainer which means it's a list of custom properties
    def ratio = symbol.owner.customProperties.value.find { it.definition?.name == 'policy.ill.InstitutionalLoanToBorrowRatio' }
    def stats_url = symbol.owner.services.find { it.service.businessFunction?.value == 'RS_STATS' }

    log.debug("Loan to borrow ratio is : ${ratio}");
    log.debug("URL for stats is : ${stats_url}");

    Map<String, Object> result = [
      lbr_loan:1,
      lbr_borrow:1,
      current_loan_level:ThreadLocalRandom.current().nextInt(0, 1000 + 1),
      current_borrowing_level:ThreadLocalRandom.current().nextInt(0, 1000 + 1)
    ]

    log.debug("Result of refreshStatsFor : ${result}");
    return result;
  }
}

