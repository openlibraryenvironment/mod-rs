package org.olf.rs.hostlms

import org.olf.rs.logging.DoNothingHoldingLogDetails;

import grails.testing.services.ServiceUnitTest;
import groovy.json.JsonOutput;
import spock.lang.Specification;

class HorizonHostLMSServiceSpec extends Specification implements ServiceUnitTest<HorizonHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File('src/test/resources/zresponsexml/horizon-jhu.xml').text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample, null, new DoNothingHoldingLogDetails());

        then:
        def resultJson = JsonOutput.toJson(result.first());
        result.size() == 1;
        resultJson == '{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":null,"callNumber":"SH 349 .M34 1992","reason":null,"shelvingPreference":null,"preference":null,"location":"Eisenhower","itemLoanPolicy":"Available"}';
    }
}

