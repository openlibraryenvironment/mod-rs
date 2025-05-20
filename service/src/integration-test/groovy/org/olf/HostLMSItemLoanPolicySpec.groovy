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
class HostLMSItemLoanPolicySpec extends TestBase {

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

    void "Create a new HostLMSItemLoanPolicy"(String tenantId, String code, String name, boolean lendable, boolean hidden) {
        when:"Create a new HostLMSItemLoanPolicy"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the HostLMSItemLoanPolicy
            Map hostLMSItemLoanPolicy = [
                code : code,
                name : name,
                lendable: lendable,
                hidden: hidden
            ];
            String json = (new JsonBuilder(hostLMSItemLoanPolicy)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/hostLMSItemLoanPolicies".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post HostLMSItemLoanPolicy: " + response.toString());

            // Store that HostLMSItemLoanPolicy
            testctx.hostLMSItemLoanPolicy = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | code   | name                 | lendable | hidden
            TENANT_ONE | 'test' | "A item loan policy" | true     | false
    }

    void "Fetch a specific HostLMSItemLoanPolicy"(String tenantId, String ignore) {
        when:"Fetch the HostLMSItemLoanPolicy"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the HostLMSItemLoanPolicy
            def response = doGet("${baseUrl}/rs/hostLMSItemLoanPolicies/" + testctx.hostLMSItemLoanPolicy.id.toString());
            log.debug("Response from Get hostLMSItemLoanPolicies: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.hostLMSItemLoanPolicy.id);
            assert(response.code == testctx.hostLMSItemLoanPolicy.code);
            assert(response.name == testctx.hostLMSItemLoanPolicy.name);
            assert(response.lendable == testctx.hostLMSItemLoanPolicy.lendable);
            assert(response.hidden == testctx.hostLMSItemLoanPolicy.hidden);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for hostLMSItemLoanPolicies"(String tenantId, String ignore) {
        when:"Search for hostLMSItemLoanPolicies"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/hostLMSItemLoanPolicies", [ filters : "code==" + testctx.hostLMSItemLoanPolicy.code ]);
            log.debug("Response from searching for hostLMSItemLoanPolicies: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.hostLMSItemLoanPolicy.id);
            assert(response[0].code == testctx.hostLMSItemLoanPolicy.code);
            assert(response[0].name == testctx.hostLMSItemLoanPolicy.name);
            assert(response[0].lendable == testctx.hostLMSItemLoanPolicy.lendable);
            assert(response[0].hidden == testctx.hostLMSItemLoanPolicy.hidden);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update HostLMSItemLoanPolicy name"(String tenantId, String name) {
        when:"Update name for HostLMSItemLoanPolicy"

            Map hostLMSItemLoanPolicy = [
                name : name
            ];
            String json = (new JsonBuilder(hostLMSItemLoanPolicy)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/hostLMSItemLoanPolicies/" + testctx.hostLMSItemLoanPolicy.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating HostLMSItemLoanPolicy: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.name == name);

        where:
            tenantId   | name
            TENANT_ONE | "Name has been changed"
    }

    void "Delete a HostLMSItemLoanPolicy"(String tenantId, String ignore) {
        when:"Delete a HostLMSItemLoanPolicy"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/hostLMSItemLoanPolicies/" + testctx.hostLMSItemLoanPolicy.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a HostLMSItemLoanPolicy: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
