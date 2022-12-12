package org.olf.rs.hostlms

import grails.testing.services.ServiceUnitTest
import groovy.json.JsonOutput
import org.olf.rs.hostlms.KohaHostLMSService
import spock.lang.Specification

class KohaHostLMSServiceSpec extends Specification implements ServiceUnitTest<KohaHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File('src/test/resources/zresponsexml/koha-chatham.xml').text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample);

        then:
        def resultJson = JsonOutput.toJson(result.first());
        result.size() == 1;
        resultJson == '{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":"CIRC2","callNumber":"580.1 G733v","reason":null,"shelvingPreference":null,"preference":null,"location":"CHATHAM","itemLoanPolicy":null}';
    }
}

