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
        result.userProfile == profileName;

        where:
        profileName             | responseJson
        'Alma, WMS, etc.'       | '{"firstName":"TEST ACCOUNT","lastName":"OCLC","privileges":[{"key":"STATUS","value":"OK"}, {"key":"PROFILE","value":"Alma, WMS, etc."}],"electronicAddresses":[{"value":"some@email.tld","key":"emailAddress"}],"physicalAddresses":[{"value":{"postalCode":"01545","locality":"Shrewsbury","lineOne":"123 Avenue Rd.","region":"MA"},"key":"unknown-type"}],"userId":"12345"}'
        'Evergreen'             | '{"firstName":"Firstly","Lastly":"","privileges":[{"key":"Evergreen","value":"Active"}],"electronicAddresses":[{"value":"some@email.tld","key":"emailAddress"}],"physicalAddresses":[{"value":{"postalCode":"01545","locality":"Shrewsbury","lineOne":"123 Avenue Rd.","region":"MA"},"key":"unknown-type"}],"userId":"12345"}'
        'Polaris'               | '{"firstName":"Firstly","Lastly":"","privileges":[{"key":"Polaris","value":"01/01/01"}, {"key":"Polaris","value":"OK"}],"electronicAddresses":[{"value":"some@email.tld","key":"emailAddress"}],"physicalAddresses":[{"value":{"postalCode":"01545","locality":"Shrewsbury","lineOne":"123 Avenue Rd.","region":"MA"},"key":"unknown-type"}],"userId":"12345"}'
        'Symphony, sometimes'   | '{"firstName":"Firstly","Lastly":"","privileges":[{"key":"Symphony, sometimes","value":""}],"electronicAddresses":[{"value":"some@email.tld","key":"emailAddress"}],"physicalAddresses":[{"value":{"postalCode":"01545","locality":"Shrewsbury","lineOne":"123 Avenue Rd.","region":"MA"},"key":"unknown-type"}],"userId":"12345"}'
    }
}
