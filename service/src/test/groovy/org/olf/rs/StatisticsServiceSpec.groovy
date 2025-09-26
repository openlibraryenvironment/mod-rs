package org.olf.rs;

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import groovy.util.logging.Slf4j

/**
 * A mock email service that allows the integration tests to complete without sending any actual emails
 *
 */
@Slf4j
class StatisticsServiceSpec extends Specification implements ServiceUnitTest<StatisticsService> {

  def 'test ratio processing'() {
    when: 'We process an example set of ratio data'
      def result = service.processRatioInfo('{"asAt":"2021-09-27T18:54:50Z","requestsByTag":{"ACTIVE_LOAN":29,"ACTIVE_BORROW":0}}','1:3')

    then:
      log.info("got result: ${result}");

    expect:
      result.timestamp <= System.currentTimeMillis();
      result.lbr_loan == 1
      result.lbr_borrow == 3
      result.current_loan_level == 29
      result.current_borrowing_level == 0
  }
}
