package org.olf

import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.web.databinding.GrailsWebDataBinder
import groovy.util.logging.Slf4j
import groovyx.net.http.ApacheHttpBuilder
import org.apache.http.client.config.RequestConfig
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.routing.StaticRouterService
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService
import org.olf.rs.statemodel.events.EventStatusReqRequestSentToSupplierIndService
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Stepwise
import org.apache.http.impl.client.HttpClientBuilder;
import groovyx.net.http.*; //euw, a star import

import static groovyx.net.http.ContentTypes.XML

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) //Otherwise the port will be random
class NoILLAddressSpec extends TestBase {

    final static String TENANT_ONE_NAME = "NIAInstOne";
    final static String TENANT_TWO_NAME = "NIAInstTwo";
    final static String SYMBOL_AUTHORITY = "DMMY";
    final static String SYMBOL_ONE_NAME = "NIA1";
    final static String SYMBOL_TWO_NAME = "NIA2";
    final static String TENANT_ONE_DB = (TENANT_ONE_NAME + "_mod_rs").toLowerCase();
    final static String TENANT_TWO_DB = (TENANT_TWO_NAME + "_mod_rs").toLowerCase();

    /*
    @Shared
    private static List<Map> DIRECTORY_INFO = [
            [ id:'RS-T-D-0001',
              name: TENANT_ONE_NAME,
              slug:'NIA_INST_ONE',
              symbols: [[ authority:SYMBOL_AUTHORITY, symbol:SYMBOL_ONE_NAME, priority:'a']],
              type : "Institution"
            ],
            [ id:'RS-T-D-0002',
              name: TENANT_TWO_NAME,
              slug:'NIA_INST_TWO',
              symbols: [[ authority:SYMBOL_AUTHORITY, symbol:SYMBOL_TWO_NAME, priority:'a']],
              type: "Institution"
            ]
    ];

     */

    GrailsWebDataBinder grailsWebDataBinder;



    def setupSpecWithSpring() {
        super.setupSpecWithSpring();
        log.debug("setup spec completed")
    }

    //Do we need to make sure the base version doesn't happen?
    def setupSpec() {
        log.debug("setupSpec called");
    }

    def setup() {
        if (testctx.niaInitialized == null) {
            testctx.niaInitialized = true;
        }
    }

    def cleanup() {
        log.debug("Cleanup called");
    }

    public String getBaseUrl() {
        //For some reason the base url keeps getting 'null' inserted into it
        return super.getBaseUrl()?.replace("null", "");
    }


    void "Test local symbol parsing" (String localSymbol, String localSymbolList, boolean expectedResult) {
        when:
            boolean result = EventStatusReqRequestSentToSupplierIndService.symbolPresent(localSymbol, localSymbolList);

        then:
            assert(result == expectedResult);

        where:
        localSymbol | localSymbolList | expectedResult
        'auth:sym1' | 'auth:sym1,auth:sym2' | true
        ''          | 'auth:sym1'           | false
        'sym1'      | 'auth:sym1,auth:sym2' | false
        'auth:sym1' | 'AUTH:SYM1,AUTH:SYM2' | true
    }


    void "Attempt to delete any old tenants"(tenantid, name) {
        when:"We post a delete request"
        boolean result = deleteTenant(tenantid, name);

        then:"Any old tenant removed"
        assert(result);

        where:
        tenantid | name
        TENANT_ONE_NAME | TENANT_ONE_NAME
        TENANT_TWO_NAME | TENANT_TWO_NAME
    }

    void "Set up test tenants"(tenantid, name) {
        when:"We post a new tenant request to the OKAPI controller"
        boolean response = setupTenant(tenantid, name);

        then:"The response is correct"
        assert(response);

        where:
        tenantid | name
        TENANT_ONE_NAME | TENANT_ONE_NAME
        TENANT_TWO_NAME | TENANT_TWO_NAME
    }

    @Shared
    private final TENANT_ONE_SETTINGS_VISIBLE = [
            'auto_responder_status':'off',
            'auto_responder_cancel': 'off',
            'routing_adapter':'static',
            'static_routes':"${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}",
            'auto_rerequest':'yes',
            'request_id_prefix' : 'TENANTONE',
            (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${baseUrl}/rs/externalApi/iso18626".toString()
    ];

    @Shared
    private final TENANT_ONE_SETTINGS_HIDDEN = [];

    @Shared
    private final TENANT_TWO_SETTINGS_VISIBLE = [
            'auto_responder_status':'off',
            'auto_responder_cancel': 'off',
            'routing_adapter':'static',
            'static_routes':"${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}",
            'request_id_prefix' : 'TENANTTWO',
            (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${baseUrl}/rs/externalApi/iso18626".toString()
    ];

    @Shared
    private final TENANT_TWO_SETTINGS_HIDDEN = [];

    void "Configure Tenants for lending without rota or directory"(String tenant_id, Map changes_needed, Map changes_needed_hidden) {
        when:"We fetch the existing settings for ${tenant_id}"

        changeSettings(tenant_id, changes_needed);
        changeSettings(tenant_id, changes_needed_hidden, true)

        then:"Tenant is configured"
        1==1


        where:
        tenant_id          | changes_needed               | changes_needed_hidden
        TENANT_ONE_NAME    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'disabled',  'auto_rerequest':'yes',  'request_id_prefix' : 'TENANTONE' ] | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']
        TENANT_TWO_NAME    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'disabled',  'request_id_prefix' : 'TENANTTWO' ] | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']
    }


    void "Test willsupply/loaned interaction with mock"() {
        String requesterTenantId = TENANT_ONE_NAME;
        String responderTenantId = TENANT_TWO_NAME;
        String patronIdentifier = "22-33-44";
        String patronReference = "ref-${patronIdentifier}";
        String systemInstanceIdentifier = "WILLSUPPLY_LOANED"; //test transmission to supplierUniqueRecordId

        when: "We create a request"

        changeSettings( requesterTenantId, [ (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "http://localhost:19083/iso18626".toString() ] );
        changeSettings( requesterTenantId, [ (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}"]);
        changeSettings( requesterTenantId, [ (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}"]);
        //changeSettings( responderTenantId, [ (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${baseUrl}/rs/externalApi/iso18626".toString() ] );

        Map request = [
                patronReference: patronReference,
                title: "A test of the no ILL address system",
                author: "Lilly, Noel",
                patronIdentifier: patronIdentifier,
                isRequester: true,
                systemInstanceIdentifier: systemInstanceIdentifier,
                //supplierUniqueRecordId: "WILLSUPPLY_LOANED"
        ];

        setHeaders([ 'X-Okapi-Tenant': requesterTenantId ]);
        doPost("${baseUrl}/rs/patronrequests".toString(), request);


        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);

        String requestId =  waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_SHIPPED);

        String performActionUrl = "${baseUrl}/rs/patronrequests/${requestId}/performAction".toString();
        String jsonPayloadRecieved =  new File("src/integration-test/resources/scenarios/requesterReceived.json").text;
        doPost(performActionUrl, jsonPayloadRecieved);

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_CHECKED_IN);

        String jsonPayloadReturn = new File("src/integration-test/resources/scenarios/patronReturnedItem.json").text;
        doPost(performActionUrl, jsonPayloadReturn);

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING);

        String jsonPayloadShipped = new File("src/integration-test/resources/scenarios/shippedReturn.json").text;
        doPost(performActionUrl, jsonPayloadShipped);

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER);

        def requestData = doGet("${baseUrl}rs/patronrequests/${requestId}");


        then:
        assert(true);
    }

    void "Test willsupply/unfilled interaction with mock"(
            String deliveryMethod,
            String serviceType,
            String patronIdentifier
    ) {
        String requesterTenantId = TENANT_ONE_NAME;
        String responderTenantId = TENANT_TWO_NAME;
        //String patronIdentifier = "23-23-24";

        String patronReference = "ref-${patronIdentifier}";
        String systemInstanceIdentifier = "007-008-009";

        when: "We create a request"

        changeSettings( requesterTenantId, [ (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "http://localhost:19083/iso18626".toString() ] );
        changeSettings( requesterTenantId, [ (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}"]);
        changeSettings( requesterTenantId, [ (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}"]);
        //changeSettings( responderTenantId, [ (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${baseUrl}/rs/externalApi/iso18626".toString() ] );

        Map request = [
                patronReference: patronReference,
                title: "Yet another test of the no ILL address system",
                author: "Gon, Etsch",
                patronIdentifier: patronIdentifier,
                isRequester: true,
                systemInstanceIdentifier: "WILLSUPPLY_UNFILLED",
                deliveryMethod: deliveryMethod,
                serviceType: serviceType
        ];

        setHeaders([ 'X-Okapi-Tenant': requesterTenantId ]);
        doPost("${baseUrl}/rs/patronrequests".toString(), request);

        String requestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);

        def requestData = doGet("${baseUrl}rs/patronrequests/${requestId}");
        
        assert(requestData.requestingInstitutionSymbol == "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}");
        assert(requestData.supplyingInstitutionSymbol == "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}");

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_END_OF_ROTA);



        then:
        assert(true);

        where:
        deliveryMethod | serviceType | patronIdentifier
        null           | null        | "23-23-24"
        "URL"          | "Copy"      | "223-223-224"
    }

    void "Test acting as supplier to mock"() {
        String responderTenantId = TENANT_ONE_NAME;
        String patronReference = "ref-33-44-55"; //from xml file

        when: "We post a new request to the mock to act as a requester"
        String requestBody = new File("src/integration-test/resources/isoMessages/illmockrequest.xml").text;
        changeSettings(responderTenantId, [ (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "http://localhost:19083/iso18626".toString() ] );
        changeSettings(responderTenantId, [ (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}"]);
        changeSettings(responderTenantId, [ (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}"]);

        sendXMLMessage("http://localhost:19083/iso18626".toString(), requestBody, null, 10000);

        String requestId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_IDLE);
        setHeaders([ 'X-Okapi-Tenant': responderTenantId ]);

        def requestData = doGet("${baseUrl}/rs/patronrequests/${requestId}");

        String performActionUrl = "${baseUrl}/rs/patronrequests/${requestId}/performAction".toString();
        String jsonPayloadWillsupply = new File("src/integration-test/resources/scenarios/supplierAnswerYes.json").text;


        doPost(performActionUrl, jsonPayloadWillsupply);

        waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_CANCEL_REQUEST_RECEIVED);

        String jsonPayloadRespondCancelYes = new File("src/integration-test/resources/scenarios/supplierRespondToCancelYes.json").text;
        doPost(performActionUrl, jsonPayloadRespondCancelYes);

        waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_CANCELLED);






        then:
        assert(true);


    }



}
