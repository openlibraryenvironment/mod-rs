package org.olf

import com.k_int.web.toolkit.refdata.RefdataValue

import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j
import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ShipmentSpec extends TestBase {

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

    void "Create a new Shipment"(
        String tenantId,
        String shipmentMethod,
        String trackingNumber,
        String status,
        String shipDate,
        String receivedDate
    ) {
        when:"Create a new Shipment"

            // Lookup the reference data values
            RefdataValue shipmentMethodValue = null;
            RefdataValue statusValue = null;
            Tenants.withId(tenantId.toLowerCase()+'_mod_rs') {
                shipmentMethodValue =  RefdataValue.lookupOrCreate("Shipment.Method", shipmentMethod);
                statusValue = RefdataValue.lookupOrCreate("Shipment.Status", status);
            }

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the Shipment
            Map shipment = [
                shipmentMethod : shipmentMethodValue.id,
                trackingNumber : trackingNumber,
                status : statusValue.id,
                shipDate : shipDate,
                receivedDate : receivedDate
            ];
            String json = (new JsonBuilder(shipment)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/shipments".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post Shipment: " + response.toString());

            // Store that Shipment
            testctx.shipment = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | shipmentMethod | trackingNumber | status     | shipDate              | receivedDate
            TENANT_ONE | "Courier"      | "A0001"        | "Received" | "2022-06-01T00:00:00" | "2023-01-31T00:00:00"
    }

    void "Fetch a specific Shipment"(String tenantId, String ignore) {
        when:"Fetch the Shipment"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the Shipment
            def response = doGet("${baseUrl}/rs/shipments/" + testctx.shipment.id.toString());
            log.debug("Response from Get shipments: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.shipment.id);
            assert(response.shipmentMethod == testctx.shipment.shipmentMethod);
            assert(response.trackingNumber == testctx.shipment.trackingNumber);
            assert(response.status == testctx.shipment.status);
            assert(response.shipDate == testctx.shipment.shipDate);
            assert(response.receivedDate == testctx.shipment.receivedDate);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for Shipments"(String tenantId, String ignore) {
        when:"Search for Shipments"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/shipments", [ filters : "trackingNumber==" + testctx.shipment.trackingNumber ]);
            log.debug("Response from searching for shipments: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.shipment.id);
            assert(response[0].shipmentMethod == testctx.shipment.shipmentMethod);
            assert(response[0].trackingNumber == testctx.shipment.trackingNumber);
            assert(response[0].status == testctx.shipment.status);
            assert(response[0].shipDate == testctx.shipment.shipDate);
            assert(response[0].receivedDate == testctx.shipment.receivedDate);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update Shipment trscking number"(String tenantId, trackingNumber) {
        when:"Update tracking number for Shipment"

            Map shipment = [
                trackingNumber : trackingNumber
            ];
            String json = (new JsonBuilder(shipment)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/shipments/" + testctx.shipment.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating Shipment: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.trackingNumber == trackingNumber);

        where:
            tenantId   | trackingNumber
            TENANT_ONE | "B1564"
    }

    void "Delete a Shipment"(String tenantId, String ignore) {
        when:"Delete a Shipment"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/shipments/" + testctx.shipment.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a Shipment: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
