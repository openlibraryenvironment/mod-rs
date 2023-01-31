package org.olf

import com.k_int.web.toolkit.refdata.RefdataValue;

import grails.gorm.multitenancy.Tenants;
import grails.testing.mixin.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class TemplateContainerSpec extends TestBase {

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

    void "Create a new TemplateContainer"(
        String tenantId,
        String name,
        String templateResolver,
        String description,
        String context
    ) {
        when:"Create a new TemplateContainer"

            // Lookup the reference data values
            RefdataValue templateResolverValue = null;
            Tenants.withId(tenantId.toLowerCase()+'_mod_rs') {
                templateResolverValue =  RefdataValue.lookupOrCreate("TemplateContainer.TemplateResolver", templateResolver);
            }

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the TemplateContainer
            Map templateContainer = [
                name : name,
                templateResolver : templateResolverValue.id,
                description : description,
                context : context
            ];
            String json = (new JsonBuilder(templateContainer)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/template".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post TemplateContainer: " + response.toString());

            // Store that TemplateContainer
            testctx.templateContainer = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | name   | templateResolver | description            | context
            TENANT_ONE | 'test' | "Handlebars"     | "A template container" | "testing"
    }

    void "Fetch a specific TemplateContainer"(String tenantId, String ignore) {
        when:"Fetch the TemplateContainer"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the TemplateContainer
            def response = doGet("${baseUrl}/rs/template/" + testctx.templateContainer.id.toString());
            log.debug("Response from Get templateContainers: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.templateContainer.id);
            assert(response.name == testctx.templateContainer.name);
            assert(response.description == testctx.templateContainer.description);
            assert(response.templateResolver == testctx.templateContainer.templateResolver);
            assert(response.context == testctx.templateContainer.context);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for TemplateContainers"(String tenantId, String ignore) {
        when:"Search for TemplateContainers"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/template", [ filters : "name==" + testctx.templateContainer.name ]);
            log.debug("Response from searching for templateContainers: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.templateContainer.id);
            assert(response[0].name == testctx.templateContainer.name);
            assert(response[0].description == testctx.templateContainer.description);
            assert(response[0].templateResolver == testctx.templateContainer.templateResolver);
            assert(response[0].context == testctx.templateContainer.context);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update TemplateContainer description"(String tenantId, description) {
        when:"Update description for TemplateContainer"

            Map templateContainer = [
                description : description
            ];
            String json = (new JsonBuilder(templateContainer)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/template/" + testctx.templateContainer.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating TemplateContainer: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.description == description);

        where:
            tenantId   | description
            TENANT_ONE | "TemplateContainer description has been changed"
    }

    void "Delete a TemplateContainer"(String tenantId, String ignore) {
        when:"Delete a TemplateContainer"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/template/" + testctx.templateContainer.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a TemplateContainer: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
