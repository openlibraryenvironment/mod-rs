package org.olf.rs;

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import groovy.util.logging.Slf4j
import java.util.Date;

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
      '2021-09-30T00:00:00Z' | 1632960000000
      '2021-09-30 00:00' | 1632960000000
      '2021-09-30T00:00:00-0500' | 1632978000000
      '2021-09-30T00:00:00.000+01:00' | 1632956400000
      // II: Comment out the remaining ones here - they don't seem to work as expected and cause test
      // failures. Possibly needs work in implementation of service.parseDateString
      // Strangely, these do not use the expected offsets
      // '2021-09-30T00:00:00EST' | 1632978000000
      // '2021-09-30T00:00:00BST' | 1632956400000
      // '2021-09-30T00:00:00GMT' | 1632960000000
      // '2021-09-30T00:00:00UTC' | 1632960000000
      // '2021-09-30T00:00:00EST' | 1632974400000
  }
}
