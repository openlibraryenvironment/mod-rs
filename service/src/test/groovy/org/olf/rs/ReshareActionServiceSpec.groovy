package org.olf.rs;

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import groovy.util.logging.Slf4j
import java.util.Date;

/**
 * A mock email service that allows the integration tests to complete without sending any actual emails
 *
 */
@Slf4j
class ReshareActionServiceSpec extends Specification implements ServiceUnitTest<ReshareActionService> {

  def 'test date parse'(datestr, expected_time) {
    when: 'We parse a date'
      Date result = service.parseDateString(datestr)

    then:
      log.info("parsed ${datestr} as ${result}");

    expect:
      result.getTime() == expected_time
      
    where:
      datestr|expected_time
      '2021-09-30T00:00:00GMT' | 1632960000000
      '2021-09-30T00:00:00UTC' | 1632960000000
  }
}
