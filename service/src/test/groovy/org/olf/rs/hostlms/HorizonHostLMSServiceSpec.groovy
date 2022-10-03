package org.olf.rs.hostlms

import grails.testing.services.ServiceUnitTest
import org.olf.rs.hostlms.HorizonHostLMSService
import spock.lang.Specification
import groovy.json.JsonOutput
import groovy.util.XmlSlurper

class HorizonHostLMSServiceSpec extends Specification implements ServiceUnitTest<HorizonHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File('src/test/resources/zresponsexml/horizon-jhu.xml').text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample);

        then:
        def resultJson = JsonOutput.toJson(result.first());
        result.size() == 1;
        resultJson == '{"itemId":null,"shelvingLocation":null,"callNumber":"SH 349 .M34 1992","reason":null,"shelvingPreference":null,"preference":null,"location":"Eisenhower","itemLoanPolicy":"Available"}';
    }
}

