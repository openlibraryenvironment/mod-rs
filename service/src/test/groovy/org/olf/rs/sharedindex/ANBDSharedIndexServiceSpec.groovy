package org.olf.rs.sharedindex

import groovy.util.logging.Slf4j
import spock.lang.Specification
import grails.testing.services.ServiceUnitTest

@Slf4j
class ANBDSharedIndexServiceSpec extends Specification implements ServiceUnitTest<ANBDSharedIndexService> {
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
}
