package org.olf.rs;

import grails.testing.services.ServiceUnitTest
import groovy.util.logging.Slf4j
import spock.lang.Specification
import org.olf.rs.statemodel.events.EventMessageRequestIndService

@Slf4j
class EventMessageRequestIndServiceSpec extends Specification implements ServiceUnitTest<EventMessageRequestIndService> {

  def 'test format physical address'(address, expected) {
    when: 'We format an address'
      String result = service.formatPhysicalAddress(address);

    then:
      log.info("formatted address as ${result}");

    expect:
      result == expected;

    where:
      address | expected
      [line1: "123 Main St.", locality: "The City", region: "PA", postalCode: "123456"] | "123 Main St.\nThe City, PA, 123456"
  }

  def 'test address list to map'() {
    when:
    def list = [
                [ address : [ physicalAddress : [ foo : 'bar']]],
                [ address : [ electronicAddress : [ whip : 'lash']]]
    ];
    then:
    def map = service.addressListToMap(list)
    assert( map.address.physicalAddress.foo == 'bar')
    assert( map.address.electronicAddress.whip == 'lash')
  }
}
