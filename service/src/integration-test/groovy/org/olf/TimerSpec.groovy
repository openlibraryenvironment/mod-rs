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
class TimerSpec extends TestBase {

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

    void "Create a new Timer"(
        String tenantId,
        String code,
        String description,
        String rrule,
        long lastExecution,
        long nextExecution,
        String taskCode,
        String taskConfig,
        boolean enabled,
        boolean executeAtDayStart
    ) {
        when:"Create a new Timer"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the Timer
            Map timer = [
                code : code,
                description : description,
                rrule : rrule,
                lastExecution : lastExecution,
                nextExecution : nextExecution,
                taskCode : taskCode,
                taskConfig : taskConfig,
                enabled : enabled,
                executeAtDayStart : executeAtDayStart
            ];
            String json = (new JsonBuilder(timer)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/timers".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post Timer: " + response.toString());

            // Store that Timer
            testctx.timer = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | code   | description | rrule   | lastExecution | nextExecution | taskCode | taskConfig | enabled | executeAtDayStart
            TENANT_ONE | 'test' | "A timer"   | "rrule" | 0             | 1             | "Timer"  | "{}"       | false   | true
    }

    void "Fetch a specific Timer"(String tenantId, String ignore) {
        when:"Fetch the Timer"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the Timer
            def response = doGet("${baseUrl}/rs/timers/" + testctx.timer.id.toString());
            log.debug("Response from Get timers: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.timer.id);
            assert(response.code == testctx.timer.code);
            assert(response.description == testctx.timer.description);
            assert(response.rrule == testctx.timer.rrule);
            assert(response.lastExecution == testctx.timer.lastExecution);
            assert(response.nextExecution == testctx.timer.nextExecution);
            assert(response.taskCode == testctx.timer.taskCode);
            assert(response.taskConfig == testctx.timer.taskConfig);
            assert(response.enabled == testctx.timer.enabled);
            assert(response.executeAtDayStart == testctx.timer.executeAtDayStart);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for Timers"(String tenantId, String ignore) {
        when:"Search for Timers"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/timers", [ filters : "code==" + testctx.timer.code ]);
            log.debug("Response from searching for timers: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.timer.id);
            assert(response[0].code == testctx.timer.code);
            assert(response[0].description == testctx.timer.description);
            assert(response[0].rrule == testctx.timer.rrule);
            assert(response[0].lastExecution == testctx.timer.lastExecution);
            assert(response[0].nextExecution == testctx.timer.nextExecution);
            assert(response[0].taskCode == testctx.timer.taskCode);
            assert(response[0].taskConfig == testctx.timer.taskConfig);
            assert(response[0].enabled == testctx.timer.enabled);
            assert(response[0].executeAtDayStart == testctx.timer.executeAtDayStart);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update Timer description"(String tenantId, description) {
        when:"Update description for Timer"

            Map timer = [
                description : description
            ];
            String json = (new JsonBuilder(timer)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/timers/" + testctx.timer.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating Timer: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.description == description);

        where:
            tenantId   | description
            TENANT_ONE | "Timer description has been changed"
    }

    void "Delete a Timer"(String tenantId, String ignore) {
        when:"Delete a Timer"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/timers/" + testctx.timer.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a Timer: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
