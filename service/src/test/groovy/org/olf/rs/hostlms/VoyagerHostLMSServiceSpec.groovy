package org.olf.rs.hostlms

import grails.testing.services.ServiceUnitTest
import groovy.json.JsonOutput
import org.olf.rs.hostlms.VoyagerHostLMSService
import spock.lang.Specification

class VoyagerHostLMSServiceSpec extends Specification implements ServiceUnitTest<VoyagerHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File("src/test/resources/zresponsexml/${zResponseFile}").text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample);

        then:
        def resultJson = JsonOutput.toJson(result);
        resultJson == validResult;

        where:
        zResponseFile | validResult
        'voyager-yale.xml' | '[{"temporaryShelvingLocation":null,"itemId":"211398","temporaryLocation":null,"shelvingLocation":null,"callNumber":"PS3561 O646 W5 1979 (LC)","reason":null,"shelvingPreference":null,"preference":null,"location":"Library Shelving Facility (LSF)","itemLoanPolicy":null}]'
        'voyager-temp.xml' | '[{"temporaryShelvingLocation":null,"itemId":"11529458","temporaryLocation":"BASS, Lower Level, 24-Hour Reserve","shelvingLocation":null,"callNumber":"PN56.C612 G48X 2016 (LC)","reason":null,"shelvingPreference":null,"preference":null,"location":"BASS, Lower Level","itemLoanPolicy":null}]'
    }
}

