package org.olf.rs.hostlms

import grails.testing.services.ServiceUnitTest
import groovy.json.JsonOutput
import spock.lang.Specification
import org.olf.rs.hostlms.WmsHostLMSService

class WmsHostLMSServiceSpec extends Specification implements ServiceUnitTest<WmsHostLMSService> {
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
        'wms-lfmm.xml' | '[{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":"General Collection","callNumber":"PS3557.R48935 S67 2018,","reason":null,"shelvingPreference":null,"preference":null,"location":"LFMM","itemLoanPolicy":null}]'
    }
}

