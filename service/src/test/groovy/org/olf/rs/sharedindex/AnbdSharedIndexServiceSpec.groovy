package org.olf.rs.sharedindex

import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import org.olf.rs.AvailabilityStatement
import spock.lang.Specification
import grails.testing.services.ServiceUnitTest

@Slf4j
class AnbdSharedIndexServiceSpec extends Specification implements ServiceUnitTest<AnbdSharedIndexService> {
    def 'test query generation'() {
        setup:
        def values = [
                '000066653580',
                '000066413758',
                '000059268678',
                '000066047974',
                '000063992938'
        ];

        when:
        String query = service.makeQuery(["systemInstanceIdentifier" : values]);

        then:
        log.debug("Got result ${query}");
        assert(query == "@or @or @attr 1=12 000066653580 @attr 1=12 000066413758 @or @attr 1=12 000063992938 @or @attr 1=12 000059268678 @attr 1=12 000066047974");
    }

    def 'test SI record results'() {
        setup:
        String resultXML = new File("src/test/resources/sharedindex/anbd_sample.xml").text;

        when:
        def z_response = new XmlSlurper().parseText(resultXML);
        def mapList = service.getHoldingsMaps(z_response);
        def recordList = service.getRecords(z_response);

        then:
        assert(z_response != null);
        assert(!z_response.isEmpty());
        assert(mapList.size() == 13);
        assert(recordList.size() == 5);
    }

    def 'test retrieval methods'() {
        setup:
        String resultXML = new File("src/test/resources/sharedindex/anbd_sample.xml").text;

        when:
        service.metaClass.retrieveRecordsFromZ3950 = { String query -> new XmlSlurper().parseText(resultXML)} ;
        List<AvailabilityStatement> copies = service.findAppropriateCopies([systemInstanceIdentifier: ["12345", "56789"]]);
        List<String> records = service.fetchSharedIndexRecords([systemInstanceIdentifier: ["12345", "56789"]]);

        then:
        assert(copies.size() == 13);
        assert(records.size() == 5);
    }
}
