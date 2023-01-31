package org.olf

import grails.testing.mixin.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class HostLMSShelvingLocationSpec extends TestBase {

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

    void "Create a new HostLMSShelvingLocation"(String tenantId, String code, String name, long supplyPreference, boolean hidden) {
        when:"Create a new HostLMSShelvingLocation"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the HostLMSShelvingLocation
            Map hostLMSShelvingLocation = [
                code : code,
                name : name,
                supplyPreference: supplyPreference,
                hidden: hidden
            ];
            String json = (new JsonBuilder(hostLMSShelvingLocation)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/shelvingLocations".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post HostLMSShelvingLocation: " + response.toString());

            // Store that HostLMSShelvingLocation
            testctx.hostLMSShelvingLocation = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | code   | name                  | supplyPreference | hidden
            TENANT_ONE | 'test' | "A shelving location" | 105              | false
    }

    void "Fetch a specific HostLMSShelvingLocation"(String tenantId, String ignore) {
        when:"Fetch the HostLMSShelvingLocation"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the HostLMSShelvingLocation
            def response = doGet("${baseUrl}/rs/shelvingLocations/" + testctx.hostLMSShelvingLocation.id.toString());
            log.debug("Response from Get shelvingLocations: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.hostLMSShelvingLocation.id);
            assert(response.code == testctx.hostLMSShelvingLocation.code);
            assert(response.name == testctx.hostLMSShelvingLocation.name);
            assert(response.supplyPreference == testctx.hostLMSShelvingLocation.supplyPreference);
            assert(response.hidden == testctx.hostLMSShelvingLocation.hidden);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for HostLMSShelvingLocations"(String tenantId, String ignore) {
        when:"Search for HostLMSShelvingLocations"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/shelvingLocations", [ filters : "code==" + testctx.hostLMSShelvingLocation.code ]);
            log.debug("Response from searching for shelvingLocations: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.hostLMSShelvingLocation.id);
            assert(response[0].code == testctx.hostLMSShelvingLocation.code);
            assert(response[0].name == testctx.hostLMSShelvingLocation.name);
            assert(response[0].supplyPreference == testctx.hostLMSShelvingLocation.supplyPreference);
            assert(response[0].hidden == testctx.hostLMSShelvingLocation.hidden);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update HostLMSShelvingLocation name"(String tenantId, String name) {
        when:"Update name for HostLMSShelvingLocation"

            Map hostLMSShelvingLocation = [
                name : name
            ];
            String json = (new JsonBuilder(hostLMSShelvingLocation)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/shelvingLocations/" + testctx.hostLMSShelvingLocation.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating HostLMSShelvingLocation: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.name == name);

        where:
            tenantId   | name
            TENANT_ONE | "Name has been changed"
    }

    void "Delete a HostLMSShelvingLocation"(String tenantId, String ignore) {
        when:"Delete a HostLMSShelvingLocation"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/shelvingLocations/" + testctx.hostLMSShelvingLocation.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a HostLMSShelvingLocation: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
