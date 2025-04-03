package org.olf

import grails.testing.mixin.integration.Integration;
import groovy.util.logging.Slf4j
import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ApplicationSpec extends TestBase {

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

    void "Fetch the application home page"(String tenantId, String ignore) {
        when:"Fetch the Applicarion home page"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the AppSetting
            def response = doGet("${baseUrl}/");
            log.debug("Response from Get application: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.message != null);
            assert(response.grailsversion != null);
            assert(response.groovyversion != null);
            assert(response.jvmversion != null);
            assert(response.appversion != null);
            assert(response.artefacts != null);
            assert(response.controllers != null);
            assert(response.plugins != null);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
