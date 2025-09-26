package org.olf

import com.k_int.web.toolkit.refdata.RefdataCategory
import com.k_int.web.toolkit.refdata.RefdataValue
import groovyx.net.http.ApacheHttpBuilder
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClientBuilder
import org.olf.rs.GrailsEventIdentifier
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.k_int.web.toolkit.testing.HttpSpec;

import grails.events.bus.EventBus;
import groovy.json.JsonBuilder;
import groovy.util.logging.Slf4j;
import spock.lang.Shared;
import spock.util.concurrent.PollingConditions

import java.lang.reflect.Field
import java.util.*

import static groovyx.net.http.ContentTypes.XML;

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

    Map sendXMLMessage(String url, String message, Map additionalHeaders, long timeout) {
        Map result = [ messageStatus: EventISO18626IncomingAbstractService.STATUS_ERROR ]

        HttpBuilder http_client = ApacheHttpBuilder.configure({
            client.clientCustomizer({ HttpClientBuilder builder ->
                RequestConfig.Builder requestBuilder = RequestConfig.custom()
                requestBuilder.connectTimeout = timeout
                requestBuilder.connectionRequestTimeout = timeout
                requestBuilder.socketTimeout = timeout
                builder.defaultRequestConfig = requestBuilder.build()
            })
            request.uri = url
            request.contentType = XML[0]
            request.headers['accept'] = 'application/xml, text/xml'
            additionalHeaders?.each{ k, v ->
                request.headers[k] = v
            }
        })

        def response = http_client.post {
            request.body = message
            response.failure({ FromServer fromServer ->
                String errorMessage = "Error from address ${url}: ${fromServer.getStatusCode()} ${fromServer}"
                log.error(errorMessage)
                String responseStatus = fromServer.getStatusCode().toString() + " " + fromServer.getMessage()
                throw new RuntimeException(errorMessage)
            })
            response.success({ FromServer fromServer, xml ->
                String responseStatus = "${fromServer.getStatusCode()} ${fromServer.getMessage()}"
                log.debug("Got response: ${responseStatus}")
                if (xml != null) {
                    result.rawData = groovy.xml.XmlUtil.serialize(xml)
                } else {
                    result.errorData = EventISO18626IncomingAbstractService.ERROR_TYPE_NO_XML_SUPPLIED
                }

            })
        }
        log.debug("Got response message: ${response}")

        return result

    }

    String waitForRequestState(String tenant, long timeout, String patron_reference, String required_state) {
        Map params = [
                'max':'100',
                'offset':'0',
                'match':'patronReference',
                'term':patron_reference
        ]
        return waitForRequestStateParams(tenant, timeout, params, required_state)
    }

    String waitForRequestStateById(String tenant, long timeout, String id, String required_state) {
        Map params = [
                'max':'1',
                'offset':'0',
                'match':'id',
                'term':id
        ]
        return waitForRequestStateParams(tenant, timeout, params, required_state)
    }

    String waitForRequestStateByHrid(String tenant, long timeout, String hrid, String required_state) {
        Map params = [
                'max':'1',
                'offset':'0',
                'match':'hrid',
                'term':hrid
        ]
        return waitForRequestStateParams(tenant, timeout, params, required_state)
    }

    String waitForRequestStateParams(String tenant, long timeout, Map params, String required_state) {
        long start_time = System.currentTimeMillis()
        String request_id = null
        String request_state = null
        long elapsed = 0
        while ( ( required_state != request_state ) &&
                ( elapsed < timeout ) ) {

            setHeaders(['X-Okapi-Tenant': tenant])
            // https://east-okapi.folio-dev.indexdata.com/rs/patronrequests?filters=isRequester%3D%3Dtrue&match=patronGivenName&perPage=100&sort=dateCreated%3Bdesc&stats=true&term=Michelle
            def resp = doGet("${baseUrl}rs/patronrequests",
                    params)
            if (resp?.size() == 1) {
                request_id = resp[0].id
                request_state = resp[0].state?.code
            } else {
                log.debug("waitForRequestState: Request with params ${params} not found")
            }

            if (required_state != request_state) {
                // Request not found OR not yet in required state
                log.debug("Not yet found.. sleeping")
                Thread.sleep(1000)
            }
            elapsed = System.currentTimeMillis() - start_time
        }
        log.debug("Found request on tenant ${tenant} with params ${params} in state ${request_state} after ${elapsed} milliseconds")

        if ( required_state != request_state ) {
            throw new Exception("Expected ${required_state} but timed out waiting, current state is ${request_state}")
        }

        return request_id
    }

    // For the given tenant fetch the specified request
    Map fetchRequest(String tenant, String requestId) {

        setHeaders([ 'X-Okapi-Tenant': tenant ]);
        // https://east-okapi.folio-dev.indexdata.com/rs/patronrequests/{id}
        def response = doGet("${baseUrl}rs/patronrequests/${requestId}")
        return response;
    }

    protected String randomCrap(int length) {
        String source = (('A'..'Z') + ('a'..'z')).join();
        Random rand = new Random();
        return (1..length).collect { source[rand.nextInt(source.length())]}.join();
    }

    // Rudely copy-pasted from EventMessageRequestIndService
    RefdataValue findRefdataValue(String label, String vocabulary) {
        RefdataCategory cat = RefdataCategory.findByDesc(vocabulary);
        if (cat) {
            RefdataValue rdv = RefdataValue.findByOwnerAndValue(cat, label);
            return rdv;
        }
        return null;
    }

    //Hack to override the environment variable
    public static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }
}
