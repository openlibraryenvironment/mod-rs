package org.olf.rs.hostlms

import org.olf.rs.logging.DoNothingHoldingLogDetails;

import grails.testing.services.ServiceUnitTest;
import groovy.json.JsonOutput;
import spock.lang.Specification;

class FolioHostLMSServiceSpec extends Specification implements ServiceUnitTest<FolioHostLMSService> {


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
        'folio-not-requestable.xml' | '[{"temporaryShelvingLocation":"Olin Reserve","itemId":"31924128478918","temporaryLocation":null,"shelvingLocation":"Olin","callNumber":"CB19 .G69 2021","reason":null,"shelvingPreference":null,"preference":null,"location":"Olin Library","itemLoanPolicy":null}]'

    }
}
