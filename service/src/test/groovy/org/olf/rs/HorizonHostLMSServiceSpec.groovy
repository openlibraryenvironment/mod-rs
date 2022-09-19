package org.olf.rs

import grails.testing.services.ServiceUnitTest
import io.swagger.util.Json
import org.olf.rs.hostlms.HorizonHostLMSService
import org.olf.rs.lms.ItemLocation
import spock.lang.Specification
import groovy.util.logging.Slf4j
import groovy.json.JsonOutput
import groovy.util.XmlSlurper

@Slf4j
class HorizonHostLMSServiceSpec extends Specification implements ServiceUnitTest<HorizonHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        log.info(new File('.').absolutePath);
        def parsedSample = new XmlSlurper().parseText(new File('src/test/resources/zresponsexml/horizon-jhu.xml').text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample);

        then:
        def resultJson = JsonOutput.toJson(result.values().first());
        result.size() == 1;
        resultJson == '{"itemId":null,"shelvingLocation":null,"callNumber":"SH 349 .M34 1992","reason":null,"shelvingPreference":null,"preference":null,"location":"Eisenhower","itemLoanPolicy":"Available"}';
    }
}

