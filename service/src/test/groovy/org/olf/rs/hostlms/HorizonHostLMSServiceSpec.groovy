package org.olf.rs.hostlms

import org.olf.rs.logging.DoNothingHoldingLogDetails;

import grails.testing.services.ServiceUnitTest;
import groovy.json.JsonOutput;
import spock.lang.Specification;

class HorizonHostLMSServiceSpec extends Specification implements ServiceUnitTest<HorizonHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File("src/test/resources/zresponsexml/${zResponseFile}").text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample, null, new DoNothingHoldingLogDetails());

        then:
        def resultJson = JsonOutput.toJson(result);
        resultJson == validResultJson;

        where:
        zResponseFile              | validResultJson
        'horizon-jhu.xml'          | '[{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":null,"callNumber":"SH 349 .M34 1992","reason":null,"shelvingPreference":null,"preference":null,"location":"Eisenhower","itemLoanPolicy":"MSEL Books"}]'
        'horizon-hennepin.xml'     | '[{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":"Adult Fiction Book (Stacks)","callNumber":"YEVTUSH","reason":null,"shelvingPreference":null,"preference":null,"location":"Minneapolis Central (1st floor General)","itemLoanPolicy":"Checked In"}]'
    }
}

