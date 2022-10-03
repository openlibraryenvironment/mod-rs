package org.olf.rs.hostlms

import grails.testing.services.ServiceUnitTest
import groovy.json.JsonOutput
import org.olf.rs.hostlms.VoyagerHostLMSService
import spock.lang.Specification

class VoyagerHostLMSServiceSpec extends Specification implements ServiceUnitTest<VoyagerHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File('src/test/resources/zresponsexml/voyager-yale.xml').text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample);

        then:
        def resultJson = JsonOutput.toJson(result.first());
        result.size() == 1;
        resultJson == '{"itemId":"211398","shelvingLocation":null,"callNumber":"PS3561 O646 W5 1979 (LC)","reason":null,"shelvingPreference":null,"preference":null,"location":"Library Shelving Facility (LSF)","itemLoanPolicy":null}';
    }
}

