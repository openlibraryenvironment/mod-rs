package org.olf.rs.hostlms

import org.olf.rs.logging.DoNothingHoldingLogDetails;

import grails.testing.services.ServiceUnitTest;
import groovy.json.JsonOutput;
import spock.lang.Specification;

class TlcHostLMSServiceSpec extends Specification implements ServiceUnitTest<TlcHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File('src/test/resources/zresponsexml/tlc-eastern.xml').text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample, "", new DoNothingHoldingLogDetails());

        then:
        def resultJson = JsonOutput.toJson(result.first());
        result.size() == 1;
        resultJson == '{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":"WARNER STACKS","callNumber":null,"reason":null,"shelvingPreference":null,"preference":null,"location":"Warner Library","itemLoanPolicy":null}';
    }
}

