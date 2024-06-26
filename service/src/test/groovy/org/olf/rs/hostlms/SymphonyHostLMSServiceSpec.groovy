package org.olf.rs.hostlms

import org.olf.rs.logging.DoNothingHoldingLogDetails;

import grails.testing.services.ServiceUnitTest;
import groovy.json.JsonOutput;
import spock.lang.Specification;

class SymphonyHostLMSServiceSpec extends Specification implements ServiceUnitTest<SymphonyHostLMSService> {
    def 'extractAvailableItemsFrom'() {
        setup:
        def parsedSample = new XmlSlurper().parseText(new File('src/test/resources/zresponsexml/symphony-stanford.xml').text);

        when: 'We extract holdings'
        def result = service.extractAvailableItemsFrom(parsedSample, null, new DoNothingHoldingLogDetails());

        then:
        def resultJson = JsonOutput.toJson(result.first());
        result.size() == 1;
        resultJson == '{"temporaryShelvingLocation":null,"itemId":null,"temporaryLocation":null,"shelvingLocation":"STACKS","callNumber":"HV6534 .V55 J36 2017","reason":null,"shelvingPreference":null,"preference":null,"location":"SAL3","itemLoanPolicy":"STKS-MONO"}';
    }

    def 'modifyIdentifier'() {
        setup:
        def identifier = 'a983475243';
        def identifier2 = '983475243';
        def identifier3 = 'abcdefgHIJKL';

        when: 'We modify the identifier'
        def result = service.modifyIdentifier(identifier);
        def result2 = service.modifyIdentifier(identifier2);
        def result3 = service.modifyIdentifier(identifier3);

        then:
        '^C983475243' == result;
        '^C983475243' == result2;
        '^CabcdefgHIJKL' == result3;


    }

    def 'checkFilterRequestItemItemId'() {
        when:
        def result = service.filterRequestItemItemId(rawId);

        then:
        result == filteredId;

        where:
        rawId | filteredId
        '12345' | '12345'
        'boogaboogabooga' | 'boogaboogabooga'
        '.b13088889' | '13088889'
        'cowcow4949'  | '4949'
        'cow4949cow'  | '4949cow'
    }
}

