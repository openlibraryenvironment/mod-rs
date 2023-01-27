package org.olf

import grails.testing.mixin.integration.Integration
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import spock.lang.*

@Slf4j
@Integration
@Stepwise
class BatchSpec extends TestBase {

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

    void "Create a new batch"(String tenantId, String context, String description) {
        when:"Create a new batch"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the batch
            Map batch = [
                context : context,
                description : description
            ];
            String jsonBatch = (new JsonBuilder(batch)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/batch".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(jsonBatch);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post batch: " + response.toString());

            // Store that batch id
            testctx.batchId = response?.id;
            testctx.batchContext = context;

        then:"Check we have a valid response"
            // The error element should exist
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | context | description
            TENANT_ONE | 'test'  | "A simple batch created for the test"
    }

    void "Fetch a specific batch"(String tenantId, String ignore) {
        when:"Fetch the batch"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the batch
            def response = doGet("${baseUrl}/rs/batch/" + testctx.batchId.toString());
            log.debug("Response from Get batch: " + response.toString());

        then:"Check we have a valid response"
            // Check the context and id
            assert(response != null);
            assert(response.id == testctx.batchId);
            assert(response.context == testctx.batchContext);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for batches"(String tenantId, String ignore) {
        when:"Search for batches"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/batch", [ filters : "context==" + testctx.batchContext ]);
            log.debug("Response from searching for batches: " + response.toString());

        then:"Check we have a valid response"
            // Check the context and id
            assert(response != null);
            assert(response[0].context == testctx.batchContext);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update batch context"(String tenantId, String context) {
        when:"Update context for batch"

            Map batch = [
                context : context
            ];
            String jsonBatch = (new JsonBuilder(batch)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/batch/" + testctx.batchId.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(jsonBatch);
            });
            log.debug("Response from updating batch: " + response.toString());

        then:"Check we have a valid response"
            // Check the context and id
            assert(response != null);
            assert(response.context == context);

        where:
            tenantId   | context
            TENANT_ONE | "changed"
    }

    void "Delete a batch"(String tenantId, String ignore) {
        when:"Delete a batch"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/batch/" + testctx.batchId.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a batch: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the context and id
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
