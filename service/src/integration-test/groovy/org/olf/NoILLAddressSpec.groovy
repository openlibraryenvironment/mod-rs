package org.olf

import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.web.databinding.GrailsWebDataBinder
import groovy.util.logging.Slf4j
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.routing.RankedSupplier
import org.olf.rs.routing.StaticRouterService
import org.olf.rs.statemodel.Status
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Stepwise

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

    StaticRouterService staticRouterService;
    GrailsWebDataBinder grailsWebDataBinder;



    def setupSpecWithSpring() {
        super.setupSpecWithSpring();
        log.debug("setup spec completed")
    }

    public String getBaseUrl() {
        //For some reason the base url keeps getting 'null' inserted into it
        return super.getBaseUrl()?.replace("null", "");
    }

    private String waitForRequestState(String tenant, long timeout, String patron_reference, String required_state) {
        Map params = [
                'max':'100',
                'offset':'0',
                'match':'patronReference',
                'term':patron_reference
        ];
        return waitForRequestStateParams(tenant, timeout, params, required_state);
    }

    private String waitForRequestStateById(String tenant, long timeout, String id, String required_state) {
        Map params = [
                'max':'1',
                'offset':'0',
                'match':'id',
                'term':id
        ];
        return waitForRequestStateParams(tenant, timeout, params, required_state);
    }

    private String waitForRequestStateByHrid(String tenant, long timeout, String hrid, String required_state) {
        Map params = [
                'max':'1',
                'offset':'0',
                'match':'hrid',
                'term':hrid
        ]
        return waitForRequestStateParams(tenant, timeout, params, required_state);
    }

    private String waitForRequestStateParams(String tenant, long timeout, Map params, String required_state) {
        long start_time = System.currentTimeMillis();
        String request_id = null;
        String request_state = null;
        long elapsed = 0;
        while ( ( required_state != request_state ) &&
                ( elapsed < timeout ) ) {

            setHeaders(['X-Okapi-Tenant': tenant]);
            // https://east-okapi.folio-dev.indexdata.com/rs/patronrequests?filters=isRequester%3D%3Dtrue&match=patronGivenName&perPage=100&sort=dateCreated%3Bdesc&stats=true&term=Michelle
            def resp = doGet("${baseUrl}rs/patronrequests",
                    params)
            if (resp?.size() == 1) {
                request_id = resp[0].id
                request_state = resp[0].state?.code
            } else {
                log.debug("waitForRequestState: Request with params ${params} not found");
            }

            if (required_state != request_state) {
                // Request not found OR not yet in required state
                log.debug("Not yet found.. sleeping");
                Thread.sleep(1000);
            }
            elapsed = System.currentTimeMillis() - start_time
        }
        log.debug("Found request on tenant ${tenant} with params ${params} in state ${request_state} after ${elapsed} milliseconds");

        if ( required_state != request_state ) {
            throw new Exception("Expected ${required_state} but timed out waiting, current state is ${request_state}");
        }

        return request_id;
    }

    void "Attempt to delete any old tenants"(tenantid, name) {
        when:"We post a delete request"
        boolean result = deleteTenant(tenantid, name);

        then:"Any old tenant removed"
        assert(result);

        where:
        tenantid | name
        TENANT_ONE_NAME| TENANT_ONE_NAME
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

    void "Bootstrap directory data for integration tests"(String tenant_id, List<Map> dirents) {
        when:"Load the default directory (test url is ${baseUrl})"
        boolean result = true

        Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {
            log.info("Filling out dummy directory entries for tenant ${tenant_id}");

            dirents.each { entry ->

                log.debug("Sync directory entry ${entry} - Detected runtime port is ${serverPort}")
                def SimpleMapDataBindingSource source = new SimpleMapDataBindingSource(entry)
                DirectoryEntry de = new DirectoryEntry()
                grailsWebDataBinder.bind(de, source)

                try {
                    de.save(flush:true, failOnError:true)
                    log.debug("Result of bind: ${de} ${de.id}");
                }
                catch ( Exception e ) {
                    log.error("problem bootstrapping directory data",e);
                    result = false;
                }

                if ( de.errors ) {
                    de.errors?.allErrors?.each { err ->
                        log.error(err?.toString())
                    }
                }
            }
        }

        then:"Test directory entries are present"
        assert result == true

        where:
        tenant_id | dirents
        TENANT_ONE_NAME | DIRECTORY_INFO
        TENANT_TWO_NAME | DIRECTORY_INFO
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


    void "Test a interaction with mock"() {
        String requesterTenantId = TENANT_ONE_NAME;
        String responderTenantId = TENANT_TWO_NAME;
        String patronIdentifier = "22-33-44";
        String patronReference = "ref-${patronIdentifier}";
        String systemInstanceIdentifier = "007-008-009";

        when: "We create a request"

        changeSettings( requesterTenantId, [ (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "http://localhost:8081/iso18626".toString() ] );
        changeSettings( requesterTenantId, [ (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}"]);
        //changeSettings( responderTenantId, [ (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${baseUrl}/rs/externalApi/iso18626".toString() ] );

        Map request = [
                patronReference: patronReference,
                title: "A test of the no ILL address system",
                author: "Lilly, Noel",
                requestingInstitutionSymbol: "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}",
                patronIdentifier: patronIdentifier,
                isRequester: true,
                systemInstanceIdentifier: systemInstanceIdentifier,
                supplierUniqueRecordId: "WILLSUPPLY_LOANED"
        ];

        setHeaders([ 'X-Okapi-Tenant': requesterTenantId ]);
        doPost("${baseUrl}/rs/patronrequests".toString(), request);

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_UNFILLED);
        //String responderRequestId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_IDLE);
        //log.debug("Responder (terminal transition test) request id is ${responderRequestId}");



        then:
        assert(true);
    }

}
