package org.olf.rs.hostlms

import org.olf.rs.logging.DoNothingHoldingLogDetails;

import grails.testing.services.ServiceUnitTest;
import groovy.json.JsonOutput;
import spock.lang.Specification;

class SierraHostLMSServiceSpec extends Specification implements ServiceUnitTest<SierraHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File("src/test/resources/zresponsexml/${zResponseFile}").text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample, "", new DoNothingHoldingLogDetails());

        then:
        def resultJson = JsonOutput.toJson(result);
        resultJson == validResult;

        where:
        zResponseFile | validResult
        'sierra-laroche.xml' | '[{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":"Circulation","callNumber":"813.54 P648my 2003 ","reason":null,"shelvingPreference":null,"preference":null,"location":"Circulation","itemLoanPolicy":null}]'
        'sierra-laroche-unavail.xml' | '[]'
        'sierra-widener-check-shelves.xml' | '[{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":"M CIRCULATING","callNumber":"TX809.M17 R59 1996 ","reason":null,"shelvingPreference":null,"preference":null,"location":"M CIRCULATING","itemLoanPolicy":null}]'
    }
}

