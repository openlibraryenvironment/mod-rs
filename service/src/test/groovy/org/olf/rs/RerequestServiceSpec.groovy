package org.olf.rs

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import groovy.util.logging.Slf4j

class RerequestServiceSpec extends Specification implements ServiceUnitTest<RerequestService> {
    def 'test bib record processing'() {
        when: 'We process a shared index record'
            String xmlText = new File("src/test/resources/sharedindex/bibrecord_01.xml").text
        then:
            def result = service.parseBibRecordXML(xmlText);
        expect:
            result.title == "Case study research : design and methods /";
    }
}


