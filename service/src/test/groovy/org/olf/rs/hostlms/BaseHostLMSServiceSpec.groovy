package org.olf.rs.hostlms

import org.olf.rs.logging.DoNothingNcipLogDetails

import grails.testing.services.ServiceUnitTest;
import groovy.json.JsonOutput;
import spock.lang.Specification;
import org.json.JSONObject;

class BaseHostLMSServiceSpec extends Specification implements ServiceUnitTest<DefaultHostLMSService> {
    def 'testProcessLookupUserResponse'() {
        when:
        def result = [ status : 'FAIL'];

        then:
        def responseJsonObject = new JSONObject(responseJson);
        service.processLookupUserResponse(result, responseJsonObject, new DoNothingNcipLogDetails());

        expect:
        result.result == true;
        result.userProfile;

        where:
        adapter       | responseJson
        'WMS'         | '{"firstName":"TEST ACCOUNT","lastName":"OCLC","privileges":[{"key":"STATUS","value":"OK"}, {"key":"PROFILE","value":"A Profile"}],"electronicAddresses":[{"value":"rebecca.gerber1@umassmed.edu","key":"emailAddress"}],"physicalAddresses":[{"value":{"postalCode":"01545","locality":"Shrewsbury","lineOne":"222 Maple Ave","region":"MA"},"key":"unknown-type"}],"userId":"D760463647"}'
        'Evergreen'   | '{"firstName":"Firstly","Lastly":"","privileges":[{"key":"Some Profile","value":"Active"}],"electronicAddresses":[{"value":"rebecca.gerber1@umassmed.edu","key":"emailAddress"}],"physicalAddresses":[{"value":{"postalCode":"01545","locality":"Shrewsbury","lineOne":"222 Maple Ave","region":"MA"},"key":"unknown-type"}],"userId":"D760463647"}'
    }
}
