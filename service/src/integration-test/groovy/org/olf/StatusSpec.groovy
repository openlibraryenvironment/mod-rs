package org.olf

import org.olf.rs.statemodel.Status;

import grails.testing.mixin.integration.Integration;
import groovy.util.logging.Slf4j
import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class StatusSpec extends TestBase {

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

    void "Search for Status"(String tenantId, String code) {
        when:"Search for Status"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/status", [ filters : "code==" + code ]);
            log.debug("Response from searching for status: " + response.toString());

            // Save the response
            testctx.status = response[0];

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].code == code);

        where:
            tenantId   | code
            TENANT_ONE | Status.RESPONDER_IDLE
    }

    void "Fetch a specific Status"(String tenantId, String ignore) {
        when:"Fetch the Status"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the Status
            def response = doGet("${baseUrl}/rs/status/" + testctx.status.id.toString());
            log.debug("Response from Get status: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.status.id);
            assert(response.code == testctx.status.code);
            assert(response.presSeq == testctx.status.presSeq);
            assert(response.visible == testctx.status.visible);
            assert(response.needsAttention == testctx.status.needsAttention);
            assert(response.terminal == testctx.status.terminal);
            assert(response.stage == testctx.status.stage);
            assert(response.terminalSequence == testctx.status.terminalSequence);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
