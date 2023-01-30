package org.olf

import grails.testing.mixin.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class HostLMSPatronProfileSpec extends TestBase {

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

    void "Create a new HostLMSPatronProfile"(String tenantId, String code, String name, boolean canCreateRequests, boolean hidden) {
        when:"Create a new HostLMSPatronProfile"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the HostLMSPatronProfile
            Map hostLMSPatronProfile = [
                code : code,
                name : name,
                canCreateRequests: canCreateRequests,
                hidden: hidden
            ];
            String json = (new JsonBuilder(hostLMSPatronProfile)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/hostLMSPatronProfiles".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post HostLMSPatronProfile: " + response.toString());

            // Store that HostLMSPatronProfile
            testctx.hostLMSPatronProfile = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | code   | name               | canCreateRequests | hidden
            TENANT_ONE | 'test' | "A patron profile" | true              | false
    }

    void "Fetch a specific HostLMSPatronProfile"(String tenantId, String ignore) {
        when:"Fetch the HostLMSPatronProfile"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the HostLMSPatronProfile
            def response = doGet("${baseUrl}/rs/hostLMSPatronProfiles/" + testctx.hostLMSPatronProfile.id.toString());
            log.debug("Response from Get hostLMSPatronProfiles: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.hostLMSPatronProfile.id);
            assert(response.code == testctx.hostLMSPatronProfile.code);
            assert(response.name == testctx.hostLMSPatronProfile.name);
            assert(response.canCreateRequests == testctx.hostLMSPatronProfile.canCreateRequests);
            assert(response.hidden == testctx.hostLMSPatronProfile.hidden);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for HostLMSPatronProfiles"(String tenantId, String ignore) {
        when:"Search for HostLMSPatronProfiles"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/hostLMSPatronProfiles", [ filters : "code==" + testctx.hostLMSPatronProfile.code ]);
            log.debug("Response from searching for hostLMSPatronProfiles: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.hostLMSPatronProfile.id);
            assert(response[0].code == testctx.hostLMSPatronProfile.code);
            assert(response[0].name == testctx.hostLMSPatronProfile.name);
            assert(response[0].canCreateRequests == testctx.hostLMSPatronProfile.canCreateRequests);
            assert(response[0].hidden == testctx.hostLMSPatronProfile.hidden);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update HostLMSPatronProfile name"(String tenantId, String name) {
        when:"Update name for HostLMSPatronProfile"

            Map hostLMSPatronProfile = [
                name : name
            ];
            String json = (new JsonBuilder(hostLMSPatronProfile)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/hostLMSPatronProfiles/" + testctx.hostLMSPatronProfile.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating HostLMSPatronProfile: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.name == name);

        where:
            tenantId   | name
            TENANT_ONE | "Name has been changed"
    }

    void "Delete a HostLMSPatronProfile"(String tenantId, String ignore) {
        when:"Delete a HostLMSPatronProfile"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/hostLMSPatronProfiles/" + testctx.hostLMSPatronProfile.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a HostLMSPatronProfile: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
