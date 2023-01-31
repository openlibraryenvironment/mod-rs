package org.olf

import grails.testing.mixin.integration.Integration;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class ExternalApiSpec extends TestBase {

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

    void "Test the status endpoint for tenant #tenantId"(String tenantId, String ignore) {
        when:"We fetch the status report"
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);
            def resp = doGet("${baseUrl}/rs/externalApi/statusReport".toString());
            log.debug("Got status report: ${resp}");

        then:"Correct counts"
            assert(resp != null);

        where:
            tenantId    | ignore
            'RSInstOne' | ""
    }

    void "Test the statistics endpoint for tenant #tenantId"(String tenantId, String ignore) {
        when:"We fetch the status report"
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);
            def response = doGet("${baseUrl}/rs/externalApi/statistics".toString());
            log.debug("Got statistics: ${response}");

        then:"check expected structure"
            assert(response != null);
            assert(response.asAt != null);
            assert(response.current != null);
            assert(response.requestsByState != null);

        where:
            tenantId    | ignore
            'RSInstOne' | ""
    }
}
