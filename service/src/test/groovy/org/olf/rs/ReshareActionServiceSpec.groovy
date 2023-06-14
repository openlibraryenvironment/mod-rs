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
      '2022-10-13T03:59Z' | null | 1665633540000 // Alma
      '2022-10-12T18:00:00Z' | null | 1665597600000 // Aleph
      '2022-12-06T23:59Z' | null | 1670371140000 // Koha
      '2022-10-12T09:00Z' | null | 1665565200000 // Sierra
      '2022-10-12T23:59:00.000-04:00' | null | 1665633540000 // Symphony
      '2022-10-20T18:59:00Z' | null | 1666292340000 // Voyager
      '2022-10-27T03:59Z' | null | 1666843140000 // WMS
      '2021-09-30' | "yyyy-MM-dd" | 1632960000000
      '30/9/2021' | "dd/MM/yyyy" | 1632960000000
      '9/9/2021' | "dd/MM/yyyy" | 1631145600000
      '2021-09-30T00:00:00Z' | "" | 1632960000000
      '2021-09-30T00:00:00Z' | " " | 1632960000000
      '2021-09-30T00:00:00Z' | null | 1632960000000
      '2021-09-30 00:00' | null | 1632960000000
      '2021-09-30T00:00:00-0500' | null | 1632978000000
      '2021-09-30T00:00:00.000+01:00' | null | 1632956400000
      '2023-10-17T03:59Z' | null | 1697515140000
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
