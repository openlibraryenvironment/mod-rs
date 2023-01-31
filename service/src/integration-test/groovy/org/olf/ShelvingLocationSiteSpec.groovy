package org.olf

import grails.testing.mixin.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class ShelvingLocationSiteSpec extends TestBase {

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

    void "Create a new ShelvingLocationSite"(String tenantId, long supplyPreference) {
        when:"Create a new ShelvingLocationSite"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the ShelvingLocationSite
            Map shelvingLocationSite = [
                shelvingLocation : createHostLMSShelvingLocation(tenantId),
                location : createHostLMSLocation(tenantId),
                supplyPreference : supplyPreference
            ];
            String json = (new JsonBuilder(shelvingLocationSite)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/shelvingLocationSites".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post ShelvingLocationSite: " + response.toString());

            // Store that ShelvingLocationSite
            testctx.shelvingLocationSite = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | supplyPreference
            TENANT_ONE | 102
    }

    void "Fetch a specific ShelvingLocationSite"(String tenantId, String ignore) {
        when:"Fetch the ShelvingLocationSite"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the ShelvingLocationSite
            def response = doGet("${baseUrl}/rs/shelvingLocationSites/" + testctx.shelvingLocationSite.id.toString());
            log.debug("Response from Get shelvingLocations: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.shelvingLocationSite.id);
            assert(response.shelvingLocation.id == testctx.shelvingLocationSite.shelvingLocation.id);
            assert(response.location.id == testctx.shelvingLocationSite.location.id);
            assert(response.supplyPreference == testctx.shelvingLocationSite.supplyPreference);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for ShelvingLocationSites"(String tenantId, String ignore) {
        when:"Search for ShelvingLocationSites"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/shelvingLocationSites", [ filters : "id==" + testctx.shelvingLocationSite.id ]);
            log.debug("Response from searching for shelvingLocations: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.shelvingLocationSite.id);
            assert(response[0].shelvingLocation.id == testctx.shelvingLocationSite.shelvingLocation.id);
            assert(response[0].location.id == testctx.shelvingLocationSite.location.id);
            assert(response[0].supplyPreference == testctx.shelvingLocationSite.supplyPreference);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update ShelvingLocationSite supply preference"(String tenantId, long supplyPreference) {
        when:"Update supply preference for ShelvingLocationSite"

            Map shelvingLocationSite = [
                supplyPreference : supplyPreference
            ];
            String json = (new JsonBuilder(shelvingLocationSite)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/shelvingLocationSites/" + testctx.shelvingLocationSite.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating ShelvingLocationSite: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.supplyPreference == supplyPreference);

        where:
            tenantId   | supplyPreference
            TENANT_ONE | 69
    }

    void "Delete a ShelvingLocationSite"(String tenantId, String ignore) {
        when:"Delete a ShelvingLocationSite"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/shelvingLocationSites/" + testctx.shelvingLocationSite.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a ShelvingLocationSite: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
