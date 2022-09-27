package org.olf.rs.hostlms

import grails.testing.services.ServiceUnitTest
import org.olf.rs.hostlms.AlmaHostLMSService
import spock.lang.Specification
import groovy.json.JsonOutput
import groovy.util.XmlSlurper

class AlmaHostLMSServiceSpec extends Specification implements ServiceUnitTest<AlmaHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File('src/test/resources/zresponsexml/alma-pitt-extra-eresource-record.xml').text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample);

        then:
        def resultJson = JsonOutput.toJson(result.first());
        result.size() == 1;
        resultJson == '{"itemId":"31735056082393","shelvingLocation":"stacks","callNumber":"HC79.E5 K685 2007","reason":null,"shelvingPreference":null,"preference":null,"location":"ULS - Thomas Blvd","itemLoanPolicy":null}';
    }
}

