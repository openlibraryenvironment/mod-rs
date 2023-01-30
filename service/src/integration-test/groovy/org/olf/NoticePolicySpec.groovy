package org.olf

import grails.testing.mixin.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class NoticePolicySpec extends TestBase {

    // This method is declared in the HttpSpec
    def setupSpecWithSpring() {
        super.setupSpecWithSpring();
    }

    def setupSpec() {
    }

    def setup() {
    }

    def cleanup() {
    }

    void "Set up test tenants"(String tenantId, String name) {
        when:"We post a new tenant request to the OKAPI controller"
            boolean response = setupTenant(tenantId, name);

        then:"The response is correct"
            assert(response);

        where:
            tenantId   | name
            TENANT_ONE | TENANT_ONE
    }

    void "Create a new NoticePolicy"(String tenantId, String name, String description, boolean active) {
        when:"Create a new NoticePolicy"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the NoticePolicy
            Map noticePolicy = [
                name : name,
                description: description,
                active: active
            ];
            String json = (new JsonBuilder(noticePolicy)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/noticePolicies".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post NoticePolicy: " + response.toString());

            // Store that NoticePolicy
            testctx.noticePolicy = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | name   | description       | active
            TENANT_ONE | 'test' | "A notice policy" | true
    }

    void "Fetch a specific NoticePolicy"(String tenantId, String ignore) {
        when:"Fetch the NoticePolicy"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the NoticePolicy
            def response = doGet("${baseUrl}/rs/noticePolicies/" + testctx.noticePolicy.id.toString());
            log.debug("Response from Get noticePolicies: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.noticePolicy.id);
            assert(response.name == testctx.noticePolicy.name);
            assert(response.description == testctx.noticePolicy.description);
            assert(response.active == testctx.noticePolicy.active);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for NoticePolicys"(String tenantId, String ignore) {
        when:"Search for NoticePolicys"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/noticePolicies", [ filters : "name==" + testctx.noticePolicy.name ]);
            log.debug("Response from searching for noticePolicies: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.noticePolicy.id);
            assert(response[0].name == testctx.noticePolicy.name);
            assert(response[0].description == testctx.noticePolicy.description);
            assert(response[0].active == testctx.noticePolicy.active);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update NoticePolicy name"(String tenantId, String name) {
        when:"Update name for NoticePolicy"

            Map noticePolicy = [
                name : name
            ];
            String json = (new JsonBuilder(noticePolicy)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/noticePolicies/" + testctx.noticePolicy.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating NoticePolicy: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.name == name);

        where:
            tenantId   | name
            TENANT_ONE | "Name has been changed"
    }

    void "Delete a NoticePolicy"(String tenantId, String ignore) {
        when:"Delete a NoticePolicy"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/noticePolicies/" + testctx.noticePolicy.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a NoticePolicy: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
