package org.olf.rs.hostlms

import grails.testing.services.ServiceUnitTest
import groovy.json.JsonOutput
import org.olf.rs.logging.DoNothingHoldingLogDetails
import spock.lang.Specification

class EvergreenHostLMSServiceSpec extends Specification implements ServiceUnitTest<EvergreenHostLMSService> {
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
        'evergreen-lake-agassiz-with-available-item-status.xml'      | '[{"temporaryShelvingLocation":null,"itemId":"33500012563250","temporaryLocation":null,"shelvingLocation":"Main","callNumber":"PAR","reason":null,"shelvingPreference":null,"preference":null,"location":"HAWLEY","itemLoanPolicy":null}]'
        'evergreen-north-west-with-available-item-status.xml'        | '[{"temporaryShelvingLocation":null,"itemId":"35500003682026","temporaryLocation":null,"shelvingLocation":"Main","callNumber":"E pb","reason":null,"shelvingPreference":null,"preference":null,"location":"ROSEAU","itemLoanPolicy":null}]'
        'evergreen-traverse-de-sioux-with-available-item-status.xml' | '[{"temporaryShelvingLocation":null,"itemId":"30619003213957","temporaryLocation":null,"shelvingLocation":"Children\'s Literature Area","callNumber":"PZ 7 .S3912 Ho ","reason":null,"shelvingPreference":null,"preference":null,"location":"AM","itemLoanPolicy":null}]'
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
        'evergreen-east-central-with-checked-out-item-status.xml' | '[]'
        'evergreen-east-central-with-in-transit-item-status.xml' | '[]'
    }
}

