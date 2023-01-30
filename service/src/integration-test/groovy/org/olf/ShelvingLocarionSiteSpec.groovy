package org.olf

import grails.testing.mixin.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class ShelvingLocarionSiteSpec extends TestBase {

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

    void "Create a new ShelvingLocarionSite"(String tenantId, long supplyPreference) {
        when:"Create a new ShelvingLocarionSite"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the ShelvingLocarionSite
            Map shelvingLocarionSite = [
                shelvingLocation : createHostLMSShelvingLocation(tenantId),
                location : createHostLMSLocation(tenantId),
                supplyPreference : supplyPreference
            ];
            String json = (new JsonBuilder(shelvingLocarionSite)).toString();

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
            log.debug("Response from post ShelvingLocarionSite: " + response.toString());

            // Store that ShelvingLocarionSite
            testctx.shelvingLocarionSite = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | supplyPreference
            TENANT_ONE | 102
    }

    void "Fetch a specific ShelvingLocarionSite"(String tenantId, String ignore) {
        when:"Fetch the ShelvingLocarionSite"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the ShelvingLocarionSite
            def response = doGet("${baseUrl}/rs/shelvingLocationSites/" + testctx.shelvingLocarionSite.id.toString());
            log.debug("Response from Get shelvingLocations: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.shelvingLocarionSite.id);
            assert(response.shelvingLocation.id == testctx.shelvingLocarionSite.shelvingLocation.id);
            assert(response.location.id == testctx.shelvingLocarionSite.location.id);
            assert(response.supplyPreference == testctx.shelvingLocarionSite.supplyPreference);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for ShelvingLocarionSites"(String tenantId, String ignore) {
        when:"Search for ShelvingLocarionSites"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/shelvingLocationSites", [ filters : "id==" + testctx.shelvingLocarionSite.id ]);
            log.debug("Response from searching for shelvingLocations: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.shelvingLocarionSite.id);
            assert(response[0].shelvingLocation.id == testctx.shelvingLocarionSite.shelvingLocation.id);
            assert(response[0].location.id == testctx.shelvingLocarionSite.location.id);
            assert(response[0].supplyPreference == testctx.shelvingLocarionSite.supplyPreference);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update ShelvingLocarionSite supply preference"(String tenantId, long supplyPreference) {
        when:"Update supply preference for ShelvingLocarionSite"

            Map shelvingLocarionSite = [
                supplyPreference : supplyPreference
            ];
            String json = (new JsonBuilder(shelvingLocarionSite)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/shelvingLocationSites/" + testctx.shelvingLocarionSite.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating ShelvingLocarionSite: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.supplyPreference == supplyPreference);

        where:
            tenantId   | supplyPreference
            TENANT_ONE | 69
    }

    void "Delete a ShelvingLocarionSite"(String tenantId, String ignore) {
        when:"Delete a ShelvingLocarionSite"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/shelvingLocationSites/" + testctx.shelvingLocarionSite.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a ShelvingLocarionSite: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
