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
class DirectoryEntrySpec extends TestBase {

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

    void "Create a new DirectoryEntry"(
        String tenantId,
        String id,
        String name,
        String slug,
        String description,
        String status,
        String foafUrl,
        String entryUrl,
        String brandingUrl,
        long foafTimestamp,
        String lmsLocationCode,
        String phoneNumber,
        String emailAddress,
        String contactName,
        String type,
        long pubLastUpdate
    ) {
        when:"Create a new DirectoryEntry"
            // Lookup the reference data values
            RefdataValue statusValue = null;
            RefdataValue typeValue = null;
            Tenants.withId(tenantId.toLowerCase()+'_mod_rs') {
                statusValue =  RefdataValue.lookupOrCreate("DirectoryEntry.Status", status);
                typeValue = RefdataValue.lookupOrCreate("DirectoryEntry.Type", type);
            }

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the DirectoryEntry
            Map directoryEntry = [
                id : id,
                name : name,
                slug : slug,
                description : description,
                status : statusValue.id,
                foafUrl : foafUrl,
                entryUrl : entryUrl,
                brandingUrl : brandingUrl,
                foafTimestamp : foafTimestamp,
                lmsLocationCode : lmsLocationCode,
                phoneNumber : phoneNumber,
                emailAddress : emailAddress,
                contactName : contactName,
                type : typeValue.id,
                pubLastUpdate : pubLastUpdate
            ];
            String json = (new JsonBuilder(directoryEntry)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/directoryEntry".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post DirectoryEntry: " + response.toString());

            // Store that DirectoryEntry
            testctx.directoryEntry = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(response.id == id);
            assert(statusCode == 201);

        where:
            tenantId   | id                                     | name                | slug  | description     | status    | foafUrl           | entryUrl           | brandingUrl           | foafTimestamp | lmsLocationCode | phoneNumber | emailAddress | contactName | type          | pubLastUpdate
            TENANT_ONE | "a9e9c6cf-01f2-4d6b-ba12-c5e436a75e53" | 'A directory entry' | "abc" | "A test record" | "Managed" | "http://foaf.com" | "http://entry.com" | "http://branding.com" | 2             | "def"           | "phoneHome" | "email@here" | "contact"   | "Institution" | 0
    }

    void "Fetch a specific DirectoryEntry"(String tenantId, String ignore) {
        when:"Fetch the DirectoryEntry"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the DirectoryEntry
            def response = doGet("${baseUrl}/rs/directoryEntry/" + testctx.directoryEntry.id.toString());
            log.debug("Response from Get directoryEntry: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response.id == testctx.directoryEntry.id);
            assert(response.name == testctx.directoryEntry.name);
            assert(response.slug == testctx.directoryEntry.slug);
            assert(response.description == testctx.directoryEntry.description);
            assert(response.status == testctx.directoryEntry.status);
            assert(response.foafUrl == testctx.directoryEntry.foafUrl);
            assert(response.entryUrl == testctx.directoryEntry.entryUrl);
            assert(response.brandingUrl == testctx.directoryEntry.brandingUrl);
            assert(response.foafTimestamp == testctx.directoryEntry.foafTimestamp);
            assert(response.lmsLocationCode == testctx.directoryEntry.lmsLocationCode);
            assert(response.phoneNumber == testctx.directoryEntry.phoneNumber);
            assert(response.emailAddress == testctx.directoryEntry.emailAddress);
            assert(response.contactName == testctx.directoryEntry.contactName);
            assert(response.type == testctx.directoryEntry.type);
            assert(response.pubLastUpdate == testctx.directoryEntry.pubLastUpdate);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Search for DirectoryEntryies"(String tenantId, String ignore) {
        when:"Search for DirectoryEntries"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/directoryEntry", [ filters : "slug==" + testctx.directoryEntry.slug ]);
            log.debug("Response from searching for directoryEntry: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.directoryEntry.id);
            assert(response[0].name == testctx.directoryEntry.name);
            assert(response[0].slug == testctx.directoryEntry.slug);
            assert(response[0].description == testctx.directoryEntry.description);
            assert(response[0].status == testctx.directoryEntry.status);
            assert(response[0].foafUrl == testctx.directoryEntry.foafUrl);
            assert(response[0].entryUrl == testctx.directoryEntry.entryUrl);
            assert(response[0].brandingUrl == testctx.directoryEntry.brandingUrl);
            assert(response[0].foafTimestamp == testctx.directoryEntry.foafTimestamp);
            assert(response[0].lmsLocationCode == testctx.directoryEntry.lmsLocationCode);
            assert(response[0].phoneNumber == testctx.directoryEntry.phoneNumber);
            assert(response[0].emailAddress == testctx.directoryEntry.emailAddress);
            assert(response[0].contactName == testctx.directoryEntry.contactName);
            assert(response[0].type == testctx.directoryEntry.type);
            assert(response[0].pubLastUpdate == testctx.directoryEntry.pubLastUpdate);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update DirectoryEntry name"(String tenantId, String name) {
        when:"Update name for DirectoryEntry"

            Map directoryEntry = [
                name : name
            ];
            String json = (new JsonBuilder(directoryEntry)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/directoryEntry/" + testctx.directoryEntry.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating DirectoryEntry: " + response.toString());

        then:"Check we have a valid response"
            // Check the name
            assert(response != null);
            assert(response.name == name);

        where:
            tenantId   | name
            TENANT_ONE | "Name has been changed"
    }

    void "Delete a DirectoryEntry"(String tenantId, String ignore) {
        when:"Delete a DirectoryEntry"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/directoryEntry/" + testctx.directoryEntry.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a DirectoryEntry: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
