package org.olf

import grails.testing.mixin.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j
import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class HostLMSLocationSpec extends TestBase {

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

    void "Create a new HostLMSLocation"(String tenantId, String code, String name, String icalRule, long supplyPreference, boolean hidden) {
        when:"Create a new HostLMSLocation"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the HostLMSLocation
            Map hostLMSLocation = [
                code : code,
                name : name,
                icalRule: icalRule,
                supplyPreference: supplyPreference,
                hidden: hidden
            ];
            String json = (new JsonBuilder(hostLMSLocation)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/hostLMSLocations".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post HostLMSLocation: " + response.toString());

            // Store that HostLMSLocation
            testctx.hostLMSLocation = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | code   | name                 | icalRule   | supplyPreference | hidden
            TENANT_ONE | 'test' | "A item loan policy" | "icalRule" | 55               | false
    }

    void "Fetch a specific HostLMSLocation"(String tenantId, String ignore) {
        when:"Fetch the HostLMSLocation"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the HostLMSLocation
            def response = doGet("${baseUrl}/rs/hostLMSLocations/" + testctx.hostLMSLocation.id.toString());
            log.debug("Response from Get hostLMSLocations: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.hostLMSLocation.id);
            assert(response.code == testctx.hostLMSLocation.code);
            assert(response.name == testctx.hostLMSLocation.name);
            assert(response.icalRule == testctx.hostLMSLocation.icalRule);
            assert(response.supplyPreference == testctx.hostLMSLocation.supplyPreference);
            assert(response.hidden == testctx.hostLMSLocation.hidden);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for HostLMSLocations"(String tenantId, String ignore) {
        when:"Search for HostLMSLocations"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/hostLMSLocations", [ filters : "code==" + testctx.hostLMSLocation.code ]);
            log.debug("Response from searching for hostLMSLocations: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.hostLMSLocation.id);
            assert(response[0].code == testctx.hostLMSLocation.code);
            assert(response[0].name == testctx.hostLMSLocation.name);
            assert(response[0].icalRule == testctx.hostLMSLocation.icalRule);
            assert(response[0].supplyPreference == testctx.hostLMSLocation.supplyPreference);
            assert(response[0].hidden == testctx.hostLMSLocation.hidden);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update HostLMSLocation name"(String tenantId, String name) {
        when:"Update name for HostLMSLocation"

            Map hostLMSLocation = [
                name : name
            ];
            String json = (new JsonBuilder(hostLMSLocation)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/hostLMSLocations/" + testctx.hostLMSLocation.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating HostLMSLocation: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.name == name);

        where:
            tenantId   | name
            TENANT_ONE | "Name has been changed"
    }

    void "Delete a HostLMSLocation"(String tenantId, String ignore) {
        when:"Delete a HostLMSLocation"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/hostLMSLocations/" + testctx.hostLMSLocation.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a HostLMSLocation: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
