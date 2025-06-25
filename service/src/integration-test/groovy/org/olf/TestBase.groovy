package org.olf

import org.olf.rs.GrailsEventIdentifier;
import org.springframework.beans.factory.annotation.Value;

import com.k_int.web.toolkit.testing.HttpSpec;

import grails.events.bus.EventBus;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j;
import spock.lang.Shared;
import spock.util.concurrent.PollingConditions;

@Slf4j
class TestBase extends HttpSpec {

    protected static final String TENANT_ONE   = 'RSInstOne';
    protected static final String TENANT_TWO   = 'RSInstTwo';
    protected static final String TENANT_THREE = 'RSInstThree';
    protected static final String TENANT_FOUR  = 'RSInstFour';

    @Shared
    protected static Map testctx = [
        request_data : [ : ]
    ]

    // Default grails event bus is named targetEvenBus to avoid collision with reactor's event bus.
    @Autowired
    EventBus targetEventBus

    @Value(value = '${local.server.port}')
    Integer serverPort;

    /** Contains the tenants that have the ref data loaded */
    static final List<String> refDataLoadedTenants = [];

    // This method is declared in the HttpSpec
    def setupSpecWithSpring() {

        targetEventBus.subscribe(GrailsEventIdentifier.REFERENCE_DATA_LOADED) { final String tenant ->
            log.debug("Ref data loaded for ${tenant.replace("_mod_rs", "")}");
            refDataLoadedTenants.add(tenant.replace("_mod_rs", ""));
        }

        super.setupSpecWithSpring();
    }

    def setupSpec() {
        httpClientConfig = {
            client.clientCustomizer { HttpURLConnection conn ->
                conn.connectTimeout = 5000
                conn.readTimeout = 25000
            }
        }
    }

    def cleanup() {
    }

    protected boolean deleteTenant(String tenantId, String name) {
        try {
            setHeaders(['X-Okapi-Tenant': tenantId, 'accept': 'application/json; charset=UTF-8'])
            def resp = doDelete("${baseUrl}_/tenant".toString(),null)
            refDataLoadedTenants.remove(tenantId);
        } catch ( Exception e ) {
            // If there is no TestTenantG we'll get an exception here, it's fine
        }
        return(true);
    }

    protected boolean setupTenant(String tenantId, String name) {

        log.debug("Post new tenant request for ${tenantId} to ${baseUrl}_/tenant");

        setHeaders(['X-Okapi-Tenant': tenantId]);
        // post to tenant endpoint
        def resp = doPost("${baseUrl}_/tenant".toString(), ['parameters':[[key:'loadSample', value:'true'],[key:'loadReference',value:'true']]]);

        // Lets record how long it took to get the lock
        long startTime = System.currentTimeMillis();

        // Wait for the refdata to be loaded.
        PollingConditions conditions = new PollingConditions(timeout: 30)
        conditions.eventually {
            // The tenant id sent through the event handler comes through as lowercase
            refDataLoadedTenants.contains(tenantId.toLowerCase());
        }

        log.info("Ref data loaded event after " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds, so it should be safe to assume the reference data has been setup");
        log.debug("Got response for new tenant: ${resp}");
        log.debug("refDataLoadedTenants: " + refDataLoadedTenants.toString());

        return((resp != null) && refDataLoadedTenants.contains(tenantId.toLowerCase()));
    }

    protected List changeSettings(String tenantId, Map changesNeeded, boolean hidden = false) {
        // RequestRouter = Static
        setHeaders([
            'X-Okapi-Tenant': tenantId,
            'X-Okapi-Permissions': '["rs.settings.get", "rs.settings.getsection.all"]'
        ]);
        def resp = doGet("${baseUrl}rs/settings/appSettings", [ 'max':'100', 'offset':'0', 'filters' : "hidden==${hidden}"]);
        log.debug("Number of settings found: " + resp.size() + ", hidden: " + hidden + ", results: " + resp.toString());
        if ( changesNeeded != null ) {
            resp.each { setting ->
                //log.debug("Considering updating setting ${setting.id}, ${setting.section} ${setting.key} (currently = ${setting.value})");
                if ( changesNeeded.containsKey(setting.key) ) {
                    def new_value = changesNeeded[setting.key];
                    log.debug("Post update to tenant ${tenantId} setting ${setting} ==> ${new_value}");
                    setting.value = new_value;
                    def update_setting_result = doPut("${baseUrl}rs/settings/appSettings/${setting.id}".toString(), setting);
                    log.debug("Result of settings update: ${update_setting_result}");
                }
            }

            // Get hold of the updated settings and return them
            resp = doGet("${baseUrl}rs/settings/appSettings", [ 'max':'100', 'offset':'0', 'filters' : "hidden==${hidden}"]);
        }

        // Return the settings
        return(resp);
    }

    protected String createHostLMSShelvingLocation(String tenantId) {
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);
        Map hostLMSShelvingLocation = [
            code : "ShelvingLocation1",
            name : "Shelving Location 1",
            supplyPreference: 1,
            hidden: false
        ];
        String json = (new JsonBuilder(hostLMSShelvingLocation)).toString();

        def response = null;
        try {
            response = doPost("${baseUrl}/rs/shelvingLocations".toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
        } catch (groovyx.net.http.HttpException e) {
            response = e.getBody();
        }

        // return the id of the shelving location to the caller
        return(response?.id);
    }

    protected String createHostLMSLocation(String tenantId) {
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);
        Map hostLMSLocation = [
            code : "Location 1",
            name : "Location name 1",
            icalRule: "ical rule",
            supplyPreference: 2,
            hidden: false
        ];
        String json = (new JsonBuilder(hostLMSLocation)).toString();

        def response = null;
        try {
            response = doPost("${baseUrl}/rs/hostLMSLocations".toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(json);
            });
        } catch (groovyx.net.http.HttpException e) {
            response = e.getBody();
        }

        // Return the id of the location
        return(response?.id);
    }
}
