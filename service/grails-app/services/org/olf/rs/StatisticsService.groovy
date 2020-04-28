package org.olf.rs

import org.olf.rs.shared.TenantSymbolMapping
import grails.gorm.multitenancy.*
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.olf.rs.Counter

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
  public Map<String,Object> getStatsFor(String symbol) {
    Map result = refreshStatsFor(symbol);
    log.debug("getStatsFor(${symbol}) returns ${result}");
    return result;
  }

  /**
   * Dummy implementation
   */
  public Map<String, Object> refreshStatsFor(String symbol) {
    return [
      lbr_loan:1,
      lbr_borrow:1,
      current_loan_level:ThreadLocalRandom.current().nextInt(0, 1000 + 1),
      current_borrowing_level:ThreadLocalRandom.current().nextInt(0, 1000 + 1)
    ]
  }
}

