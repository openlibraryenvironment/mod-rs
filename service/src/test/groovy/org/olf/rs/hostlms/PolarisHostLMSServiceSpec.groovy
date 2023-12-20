package org.olf.rs.hostlms

import grails.testing.services.ServiceUnitTest
import groovy.json.JsonOutput
import org.olf.rs.logging.DoNothingHoldingLogDetails
import spock.lang.Specification

class PolarisHostLMSServiceSpec extends Specification implements ServiceUnitTest<PolarisHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File("src/test/resources/zresponsexml/${zResponseFile}").text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample, null, new DoNothingHoldingLogDetails());

        then:
        def resultJson = JsonOutput.toJson(result);
        resultJson == validResult;

        where:
        zResponseFile | validResult
        'polaris-with-in-item-status.xml' | '[{"temporaryShelvingLocation":null,"itemId":"31256015853493","temporaryLocation":null,"shelvingLocation":"Main Library","callNumber":"j574.92 Sill","reason":null,"shelvingPreference":null,"preference":null,"location":"Juvenile nonfiction shelves","itemLoanPolicy":null}]'
    }

    def 'failToExtractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File("src/test/resources/zresponsexml/${zResponseFile}").text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample, null, new DoNothingHoldingLogDetails());

        then:
        def resultJson = JsonOutput.toJson(result);
        resultJson == validResult;

        where:
        zResponseFile | validResult
        'polaris-with-out-item-status.xml' | '[]'
        'polaris-with-held-item-status.xml' | '[]'
    }
}

