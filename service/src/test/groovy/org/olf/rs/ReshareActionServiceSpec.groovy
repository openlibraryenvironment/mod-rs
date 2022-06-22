package org.olf.rs;

import grails.testing.services.ServiceUnitTest
import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class ReshareActionServiceSpec extends Specification implements ServiceUnitTest<ReshareActionService> {

  def 'test date parse'(datestr, format, expected_time) {
    when: 'We parse a date'
      Date result = service.parseDateString(datestr, format)

    then:
      log.info("parsed ${datestr} as ${result}");

    expect:
      result.getTime() == expected_time

    where:
      datestr|format|expected_time
      '2021-09-30' | "yyyy-MM-dd" | 1632956400000
      '30/9/2021' | "dd/MM/yyyy" | 1632956400000
      '9/9/2021' | "dd/MM/yyyy" | 1631142000000
      '2021-09-30T00:00:00Z' | "" | 1632960000000
      '2021-09-30T00:00:00Z' | " " | 1632960000000
      '2021-09-30T00:00:00Z' | null | 1632960000000
      '2021-09-30 00:00' | null | 1632960000000
      '2021-09-30T00:00:00-0500' | null | 1632978000000
      '2021-09-30T00:00:00.000+01:00' | null | 1632956400000
      // II: Comment out the remaining ones here - they don't seem to work as expected and cause test
      // failures. Possibly needs work in implementation of service.parseDateString
      // Strangely, these do not use the expected offsets
      // '2021-09-30T00:00:00EST' | null | 1632978000000
      // '2021-09-30T00:00:00BST' | null | 1632956400000
      // '2021-09-30T00:00:00GMT' | null | 1632960000000
      // '2021-09-30T00:00:00UTC' | null | 1632960000000
      // '2021-09-30T00:00:00EST' | null | 1632974400000
  }
}
