package org.olf

import grails.testing.mixin.integration.Integration;
import grails.gorm.multitenancy.Tenants;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j
import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Stepwise;

import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.referenceData.RefdataValueData;
import org.olf.rs.ReferenceDataService;

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AppSettingSpec extends TestBase {

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

    void "Create a new AppSetting"(
        String tenantId,
        String section,
        String key,
        String settingType,
        String vocab,
        String defValue,
        String value
    ) {
        when:"Create a new AppSetting"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Create the AppSetting
            Map appSetting = [
                section : section,
                key : key,
                settingType : settingType,
                vocab : vocab,
                defValue : defValue,
                value : value
            ];
            String json = (new JsonBuilder(appSetting)).toString();

            def response = null;
            int statusCode = 201;
            try {
                response = doPost("${baseUrl}/rs/settings/appSettings".toString(), null, null, {
                    // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                    request.setBody(json);
                });
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from post AppSetting: " + response.toString());

            // Store that AppSetting
            testctx.appSetting = response;

        then:"Check we have a valid response"
            assert(response?.id != null);
            assert(statusCode == 201);

        where:
            tenantId   | section        | key           | settingType | vocab        | defValue  | value
            TENANT_ONE | "test section" | "section key" | "number"    | "vocabulary" | "default" | "value"
    }

    void "Fetch a specific AppSetting"(String tenantId, String ignore) {
        when:"Fetch the AppSetting"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Fetch the AppSetting
        def response = doGet("${baseUrl}/rs/settings/appSettings/" + testctx.appSetting.id.toString());
        log.debug("Response from Get appSettings: " + response.toString());

        then:"Check we have a valid response"
        // Check the various fields
        assert(response != null);
        assert(response.id == testctx.appSetting.id);
        assert(response.section == testctx.appSetting.section);
        assert(response.key == testctx.appSetting.key);
        assert(response.settingType == testctx.appSetting.settingType);
        assert(response.vocab == testctx.appSetting.vocab);
        assert(response.defValue == testctx.appSetting.defValue);
        assert(response.value == testctx.appSetting.value);

        where:
        tenantId   | ignore
        TENANT_ONE | ""
    }

    void "Create a new Refdata AppSetting"(
            String tenantId,
            String section,
            String key,
            String settingType,
            String vocab,
            String defValue,
            String category,
            String value
    ) {
        when:"Create a new RefData AppSetting"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Create the AppSetting
        ReferenceDataService referenceDataService = ReferenceDataService.getInstance();
        def refDataValue;
        Tenants.withId((tenantId + '_mod_rs').toLowerCase()) {
            refDataValue = referenceDataService.lookup(category, value).value;
        }
        Map appSetting = [
                section : section,
                key : key,
                settingType : settingType,
                vocab : vocab,
                defValue : defValue,
                value : refDataValue
        ];
        String json = (new JsonBuilder(appSetting)).toString();

        def response = null;
        int statusCode = 201;
        try {
            response = doPost("${baseUrl}/rs/settings/appSettings".toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
        } catch (groovyx.net.http.HttpException e) {
            statusCode = e.getStatusCode();
            response = e.getBody();
        }
        log.debug("Response from post AppSetting: " + response.toString());

        // Store that AppSetting
        testctx.refDataAppSetting = response;

        then:"Check we have a valid response"
        assert(response?.id != null);
        assert(statusCode == 201);

        where:
        tenantId   | section        | key           | settingType | vocab        | defValue  | category | value
        TENANT_ONE | "test section" | "other key"   | SettingsData.SETTING_TYPE_REF_DATA | RefdataValueData.VOCABULARY_YES_NO | null | RefdataValueData.VOCABULARY_YES_NO | RefdataValueData.YES_NO_NO
    }

    void "Fetch the Refdata AppSetting"(String tenantId, String ignore) {
        when:"Fetch the AppSetting"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Fetch the AppSetting
        def response = doGet("${baseUrl}/rs/settings/appSettings/" + testctx.refDataAppSetting.id.toString());
        log.debug("Response from Get refata appSettings: " + response.toString());

        then:"Check we have a valid response"
        // Check the various fields
        assert(response != null);
        assert(response.id == testctx.refDataAppSetting.id);
        assert(response.section == testctx.refDataAppSetting.section);
        assert(response.key == testctx.refDataAppSetting.key);
        assert(response.settingType == testctx.refDataAppSetting.settingType);
        assert(response.vocab == testctx.refDataAppSetting.vocab);
        assert(response.defValue == testctx.refDataAppSetting.defValue);
        assert(response.value == testctx.refDataAppSetting.value);

        where:
        tenantId   | ignore
        TENANT_ONE | ""
    }


    void "Search for AppSettings"(String tenantId, String ignore) {
        when:"Search for AppSettings"

            // Set the headers
            setHeaders([
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Permissions': '["rs.settings.get", "rs.settings.getsection.all"]'
            ]);

            // Perform a search
            def response = doGet("${baseUrl}/rs/settings/appSettings", [ filters : "key==" + testctx.appSetting.key ]);
            log.debug("Response from searching for appSettings: " + response.toString());

        then:"Check we have a valid response"
            // Check the various fields
            assert(response != null);
            assert(response[0].id == testctx.appSetting.id);
            assert(response[0].section == testctx.appSetting.section);
            assert(response[0].key == testctx.appSetting.key);
            assert(response[0].settingType == testctx.appSetting.settingType);
            assert(response[0].vocab == testctx.appSetting.vocab);
            assert(response[0].defValue == testctx.appSetting.defValue);
            assert(response[0].value == testctx.appSetting.value);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }

    void "Update AppSetting value"(String tenantId, value) {
        when:"Update description for AppSetting"

            Map appSetting = [
                value : value
            ];
            String json = (new JsonBuilder(appSetting)).toString();

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform a search
            def response = doPut("${baseUrl}/rs/settings/appSettings/" + testctx.appSetting.id.toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
            log.debug("Response from updating AppSetting: " + response.toString());

        then:"Check we have a valid response"
            // Check the value
            assert(response != null);
            assert(response.value == value);

        where:
            tenantId   | value
            TENANT_ONE | "Value has been changed"
    }

    void "Delete a AppSetting"(String tenantId, String ignore) {
        when:"Delete a AppSetting"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Perform the delete
            def response = null;
            int statusCode = 204;

            try {
                response = doDelete("${baseUrl}/rs/settings/appSettings/" + testctx.appSetting.id.toString());
            } catch (groovyx.net.http.HttpException e) {
                statusCode = e.getStatusCode();
                response = e.getBody();
            }
            log.debug("Response from deleting a AppSetting: " + response == null ? "" : response.toString());

        then:"Check we have a valid response"
            // Check the status code
            assert(response == null);
            assert(statusCode == 204);

        where:
            tenantId   | ignore
            TENANT_ONE | ""
    }
}
