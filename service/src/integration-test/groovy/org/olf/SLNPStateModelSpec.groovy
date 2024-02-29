package org.olf

import com.k_int.web.toolkit.settings.AppSetting
import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.web.databinding.GrailsWebDataBinder
import groovy.util.logging.Slf4j
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.PatronRequest
import org.olf.rs.ReshareApplicationEventHandlerService
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.routing.RankedSupplier
import org.olf.rs.routing.StaticRouterService
import org.olf.rs.statemodel.*
import spock.lang.Shared
import spock.lang.Stepwise

@Slf4j
@Integration
@Stepwise
class SLNPStateModelSpec extends TestBase {

    @Shared
    private static List<Map> DIRECTORY_INFO = [
            [ id:'RS-T-D-0001', name: 'RSInstOne', slug:'RS_INST_ONE',     symbols: [[ authority:'ISIL', symbol:'RST1', priority:'a'] ],
              services:[
                      [
                              slug:'RSInstOne_ISO18626',
                              service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
                              customProperties:[
                                      'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
                                      'AdditionalHeaders':['X-Okapi-Tenant:RSInstOne']
                              ]
                      ]
              ]
            ],
            [ id:'RS-T-D-0002', name: 'RSInstTwo', slug:'RS_INST_TWO',     symbols: [[ authority:'ISIL', symbol:'RST2', priority:'a'] ],
              services:[
                      [
                              slug:'RSInstTwo_ISO18626',
                              service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
                              customProperties:[
                                      'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
                                      'AdditionalHeaders':['X-Okapi-Tenant:RSInstTwo']
                              ]
                      ]
              ]
            ],
            [ id:'RS-T-D-0003', name: 'RSInstThree', slug:'RS_INST_THREE', symbols: [[ authority:'ISIL', symbol:'RST3', priority:'a'] ],
              services:[
                      [
                              slug:'RSInstThree_ISO18626',
                              service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
                              customProperties:[
                                      'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
                                      'AdditionalHeaders':['X-Okapi-Tenant:RSInstThree']
                              ]
                      ]
              ]
            ]
    ]

    GrailsWebDataBinder grailsWebDataBinder
    StaticRouterService staticRouterService
    StatusService statusService

    // This method is declared in the HttpSpec
    def setupSpecWithSpring() {
        super.setupSpecWithSpring();
    }

    def setupSpec() {}

    def setup() {
        if ( testctx.initialised == null ) {
            log.debug("Inject actual runtime port number (${serverPort}) into directory entries (${baseUrl}) ");
            for ( Map entry: DIRECTORY_INFO ) {
                if ( entry.services != null ) {
                    for ( Map svc: entry.services ) {
                        svc.service.address = "${baseUrl}rs/externalApi/iso18626".toString()
                        log.debug("${entry.id}/${entry.name}/${svc.slug}/${svc.service.name} - address updated to ${svc.service.address}");
                    }
                }
            }
            testctx.initialised = true
        }
    }

    def cleanup() {}

    void "Attempt to delete any old tenants"(tenantid, name) {
        when:"We post a delete request"
        boolean result = deleteTenant(tenantid, name);

        then:"Any old tenant removed"
        assert(result);

        where:
        tenantid      | name
        'RSInstOne'   | 'RSInstOne'
        'RSInstTwo'   | 'RSInstTwo'
        'RSInstThree' | 'RSInstThree'
    }

    void "Set up test tenants "(tenantid, name) {
        when:"We post a new tenant request to the OKAPI controller"
        boolean response = setupTenant(tenantid, name);

        then:"The response is correct"
        assert(response);

        where:
        tenantid      | name
        'RSInstOne'   | 'RSInstOne'
        'RSInstTwo'   | 'RSInstTwo'
        'RSInstThree' | 'RSInstThree'
    }

    void "Bootstrap directory data for integration tests"(String tenant_id, List<Map> dirents) {
        when:"Load the default directory (test url is ${baseUrl})"
        boolean result = true

        Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {
            log.info("Filling out dummy directory entries for tenant ${tenant_id}");

            dirents.each { entry ->

                /*
                entry.symbols.each { sym ->

                  String symbol_string = sym.authority instanceof String ? sym.authority : sym.authority.symbol;

                  NamingAuthority na = NamingAuthority.findBySymbol(symbol_string)

                  if ( na != null ) {
                    log.debug("[${tenant_id}] replace symbol string ${symbol_string} with a reference to the object (${na.id},${na.symbol}) to prevent duplicate creation");
                    sym.authority = [ id: na.id, symbol: na.symbol ]
                  }
                  else {
                    sym.authority = symbol_string;
                  }
                }
                */

                log.debug("Sync directory entry ${entry} - Detected runtime port is ${serverPort}")
                def SimpleMapDataBindingSource source = new SimpleMapDataBindingSource(entry)
                DirectoryEntry de = new DirectoryEntry()
                grailsWebDataBinder.bind(de, source)

                // log.debug("Before save, ${de}, services:${de.services}");
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
        'RSInstOne' | DIRECTORY_INFO
        'RSInstTwo' | DIRECTORY_INFO
        'RSInstThree' | DIRECTORY_INFO
    }

    /** Grab the settings for each tenant so we can modify them as needeed and send back,
     *  then work through the list posting back any changes needed for that particular tenant in this testing setup
     *  for now, disable all auto responders
     *  N.B. that the test "Send request using static router" below RELIES upon the static routes assigned to RSInstOne.
     *  changing this data may well break that test.
     */
    void "Configure Tenants for Mock Lending"(String tenant_id, Map changes_needed, Map changes_needed_hidden) {
        when:"We fetch the existing settings for ${tenant_id}"
        changeSettings(tenant_id, changes_needed);
        changeSettings(tenant_id, changes_needed_hidden, true);

        then:"Tenant is configured"
        1==1

        where:
        tenant_id      | changes_needed                                                                                                                                    | changes_needed_hidden
        'RSInstOne'    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST3,SYMBOL:ISIL:RST2'] | ['state_model_requester':'SLNPRequester', 'state_model_responder':'SLNPResponder']
        'RSInstTwo'    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST1,SYMBOL:ISIL:RST3'] | ['state_model_requester':'SLNPRequester', 'state_model_responder':'SLNPResponder']
        'RSInstThree'  | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST1']                  | ['state_model_requester':'SLNPRequester', 'state_model_responder':'SLNPResponder']

    }

    void "Validate Static Router"() {

        when:"We call the static router"
        List<RankedSupplier> resolved_rota = null;
        Tenants.withId('RSInstOne_mod_rs'.toLowerCase()) {
            resolved_rota = staticRouterService.findMoreSuppliers([title:'Test'], null)
        }
        log.debug("Static Router resolved to ${resolved_rota}");

        then:"Then expect result is returned"
        resolved_rota.size() == 2;
    }

    private static void validateStateTransition(NewStatusResult newStatusResult, expectedState) {
        if (newStatusResult == null) {
            throw new Exception("New status result is null");
        }
        log.debug("Expected status is: ${expectedState} and actual status is: ${newStatusResult.status.code}");
        assert(newStatusResult.status.code == expectedState);
    }

    private void performAction(String id, String actionFileName) {
        String jsonPayload = new File('src/integration-test/resources/scenarios/' + actionFileName + '.json').text;

        log.debug("jsonPayload: ${jsonPayload}");
        String performActionURL = "${baseUrl}/rs/patronrequests/${id}/performAction".toString();
        log.debug("Posting to performAction at ${performActionURL}");

        // Execute action
        doPost(performActionURL, jsonPayload);
    }

    private PatronRequest createPatronRequest(
            String state,
            String requestPatronId,
            String requestTitle,
            String requestAuthor,
            String requestSymbol,
            String requestSystemId,
            String action,
            Boolean isRequester) {

        Map request = [
                patronReference: requestPatronId + action,
                title: requestTitle,
                author: requestAuthor,
                requestingInstitutionSymbol: requestSymbol,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: isRequester
        ];
        def resp = doPost("${baseUrl}/rs/patronrequests".toString(), request);

        PatronRequest slnpPatronRequest = PatronRequest.get(resp?.id);
        Status initialStatus = Status.lookup(state);
        slnpPatronRequest.state = initialStatus;
        return slnpPatronRequest.save(flush: true, failOnError: true);
    }

    private Symbol symbolFromString(String symbolString) {
        def parts = symbolString.tokenize(":");
        return ReshareApplicationEventHandlerService.resolveSymbol(parts[0], parts[1]);
    }

    private PatronRequest createPatronRequest(
            String state,
            String requestPatronId,
            String requestTitle,
            String requestAuthor,
            String requestSymbol,
            String supplierSymbol,
            String requestSystemId,
            String action,
            Boolean isRequester,
            String hrid,
            String peerRequestIdentifier) {

        Map request = [
                patronReference: requestPatronId + action,
                title: requestTitle,
                author: requestAuthor,
                requestingInstitutionSymbol: requestSymbol,
                supplyingInstitutionSymbol: supplierSymbol,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: isRequester,
                hrid: hrid,
                peerRequestIdentifier: peerRequestIdentifier
        ];
        def resp = doPost("${baseUrl}/rs/patronrequests".toString(), request);

        PatronRequest slnpPatronRequest = PatronRequest.get(resp?.id);
        Status initialStatus = Status.lookup(state);
        slnpPatronRequest.state = initialStatus;

        Symbol reqSymbol = symbolFromString(requestSymbol);
        Symbol suppSymbol = symbolFromString(supplierSymbol);

        slnpPatronRequest.resolvedRequester = reqSymbol;
        slnpPatronRequest.resolvedSupplier = suppSymbol;

        return slnpPatronRequest.save(flush: true, failOnError: true);
    }

//    void "Test end to end actions from supplier to requester"(
//            String requesterTenantId,
//            String responderTenantId,
//            String requesterSymbol,
//            String responderSymbol,
//            String requesterInitialState,
//            String responderInitialState,
//            String patronId,
//            String title,
//            String author,
//            String action,
//            String jsonFileName,
//            String requesterResultState,
//            String responderResultState,
//            String qualifier
//    ) {
//        when: "Creating the Requester/Responder Patron Requests"
//
//        String requesterSystemId = UUID.randomUUID().toString();
//        String responderSystemId = UUID.randomUUID().toString();
//
//        String requesterHrid = Long.toUnsignedString(new Random().nextLong(), 16).toUpperCase();
//        String responderHrid = Long.toUnsignedString(new Random().nextLong(), 16).toUpperCase();
//
//        // Create Requester PatronRequest
//        PatronRequest requesterPatronRequest;
//        Tenants.withId(requesterTenantId.toLowerCase() + '_mod_rs') {
//            // Define headers
//            def requesterHeaders = [
//                    'X-Okapi-Tenant'     : requesterTenantId,
//                    'X-Okapi-Token'      : 'dummy',
//                    'X-Okapi-User-Id'    : 'dummy',
//                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
//            ]
//
//            setHeaders(requesterHeaders);
//
//            // Save the app settings
//            AppSetting setting = AppSetting.findByKey(SettingsData.SETTING_STATE_MODEL_REQUESTER);
//            setting.value = StateModel.MODEL_SLNP_REQUESTER;
//            setting.save(flush: true, failOnError: true);
//
//            // Create PatronRequest
//            requesterPatronRequest = createPatronRequest(requesterInitialState, patronId, title, author, requesterSymbol,
//                    responderSymbol, requesterSystemId, action, true, requesterHrid, responderHrid);
//
//            log.debug("Created patron request: ${requesterPatronRequest} ID: ${requesterPatronRequest?.id}");
//        }
//
//        // Create Responder PatronRequest
//        PatronRequest responderPatronRequest;
//        Tenants.withId(responderTenantId.toLowerCase() + '_mod_rs') {
//            // Define headers
//            def headers = [
//                    'X-Okapi-Tenant'     : responderTenantId,
//                    'X-Okapi-Token'      : 'dummy',
//                    'X-Okapi-User-Id'    : 'dummy',
//                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
//            ]
//
//            setHeaders(headers);
//
//            // Save the app settings
//            AppSetting setting = AppSetting.findByKey(SettingsData.SETTING_STATE_MODEL_RESPONDER);
//            setting.value = StateModel.MODEL_SLNP_RESPONDER;
//            setting.save(flush: true, failOnError: true)
//
//            // Create PatronRequest
//            responderPatronRequest = createPatronRequest(responderInitialState, patronId, title, author, requesterSymbol,
//                    responderSymbol, responderSystemId, action, false, responderHrid, requesterHrid);
//            log.debug("Created patron request: ${responderPatronRequest} ID: ${responderPatronRequest?.id}");
//
//            // Validate initial status
//            NewStatusResult newResultStatus = statusService.lookupStatus(responderPatronRequest, null, null, true, true);
//            validateStateTransition(newResultStatus, responderInitialState);
//
//            // Perform action
//            performAction(responderPatronRequest?.id, jsonFileName);
//
//            // Validate RESPONDER result status after performed action
//            newResultStatus = statusService.lookupStatus(responderPatronRequest, action, null, true, true);
//            validateStateTransition(newResultStatus, responderResultState);
//
//            // Validate REQUESTER result status after performed action
//            newResultStatus = statusService.lookupStatus(requesterPatronRequest, action, qualifier, true, true);
//            validateStateTransition(newResultStatus, responderResultState);
//        }
//
//        then: "Check values"
//        assert true;
//
//        where:
//        requesterTenantId | responderTenantId | requesterSymbol | responderSymbol | requesterInitialState       | responderInitialState            | patronId    | title    | author    | action                                         | jsonFileName          | requesterResultState            | responderResultState               | qualifier
//        'RSInstOne'       | 'RSInstTwo'       | 'ISIL:RST1'     | 'ISIL:RST2'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_RESPONDER_AWAIT_SHIP | '9876-1231' | 'title'  | 'Author'  | Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED | 'supplierMarkShipped' | Status.SLNP_REQUESTER_SHIPPED   | Status.SLNP_RESPONDER_ITEM_SHIPPED | ActionEventResultQualifier.QUALIFIER_LOANED
//    }

    // For the given tenant, block up to timeout ms until the given request is found in the given state
    private String waitForRequestStateByHrid(String tenant, long timeout, String hrid, String required_state) {
        long start_time = System.currentTimeMillis();
        String request_id = null;
        String request_state = null;
        long elapsed = 0;
        while ( ( required_state != request_state ) &&
                ( elapsed < timeout ) ) {

            setHeaders([ 'X-Okapi-Tenant': tenant ]);
            // https://east-okapi.folio-dev.indexdata.com/rs/patronrequests?filters=isRequester%3D%3Dtrue&match=patronGivenName&perPage=100&sort=dateCreated%3Bdesc&stats=true&term=Michelle
            def resp = doGet("${baseUrl}rs/patronrequests",
                    [
                            'max':'100',
                            'offset':'0',
                            'match':'hrid',
                            'term':hrid
                    ])
            if ( resp?.size() == 1 ) {
                request_id = resp[0].id
                request_state = resp[0].state?.code
            } else {
                log.debug("waitForRequestState: Request with hrid ${hrid} not found");
            }

            if ( required_state != request_state ) {
                // Request not found OR not yet in required state
                log.debug("Not yet found.. sleeping");
                Thread.sleep(1000);
            }
            elapsed = System.currentTimeMillis() - start_time
        }

        if ( required_state != request_state ) {
            throw new Exception("Expected ${required_state} but timed out waiting, current state is ${request_state}");
        }

        return request_id;
    }

    void "Send ISO request"(String tenant_id,
                            String peer_tenant,
                            String agencyIdValue,
                            String supAgencyId,
                            String requestId,
                            String patronId,
                            String requestFile,
                            String requesting_symbol,
                            String messageFile,
                            String statusChanged,
                            String finalStatus,
                            String[] tags) {
        when:"post new request"
        log.debug("Create a new request ${tenant_id} ${tags} ${requestId} ${patronId}")

        String requestXml = new File("src/integration-test/resources/isoMessages/${requestFile}").text
        requestXml = requestXml.replace('agencyIdValue_holder', agencyIdValue)
                .replace('requestId_holder', requestId)
                .replace('patronId_holder', patronId)

        setHeaders([
                'X-Okapi-Tenant': tenant_id,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ])
        def resp = doPost("${baseUrl}/rs/externalApi/iso18626".toString(), requestXml)

        log.debug("CreateReqTest2 -- Response: RESP:${resp.ISO18626Message} ")

        String req_request = waitForRequestStateByHrid(tenant_id, 10000, requestId, Status.SLNP_REQUESTER_IDLE)
        log.debug("Created new request for iso test case 1. RESQUESTER ID is : ${req_request}")

        String messageXml = new File("src/integration-test/resources/isoMessages/${messageFile}").text
        messageXml = messageXml.replace('agencyIdValue_holder', agencyIdValue)
                .replace('requestId_holder', requestId)
                .replace('supAgencyIdValue_holder', supAgencyId)
                .replace('status_holder', statusChanged)

        setHeaders([
                'X-Okapi-Tenant': tenant_id,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ])
        resp = doPost("${baseUrl}/rs/externalApi/iso18626".toString(), messageXml)

        String message_request = waitForRequestStateByHrid(tenant_id, 10000, requestId, finalStatus)
        log.debug("Updated status. RESQUESTER ID is : ${message_request} with status: ${finalStatus}")

        then:"Check the return value"
        assert req_request != null
        assert message_request != null

        where:
        tenant_id   | peer_tenant   | agencyIdValue | supAgencyId | requestId     | patronId    | requestFile         | requesting_symbol | messageFile                         | statusChanged                                  | finalStatus                     | tags
        'RSInstOne' | 'RSInstThree' | 'RST1'        | 'RST3'      | '1234-5678-1' | '1234-5679' | 'patronRequest.xml' | 'ISIL:RST1'       | 'supplyingAgencyMessage_loaned.xml' | ActionEventResultQualifier.QUALIFIER_LOANED    | Status.SLNP_REQUESTER_SHIPPED   | [ 'RS-TESTCASE-ISO-1' ]
        'RSInstOne' | 'RSInstThree' | 'RST1'        | 'RST3'      | '1234-5678-2' | '1234-567a' | 'patronRequest.xml' | 'ISIL:RST1'       | 'supplyingAgencyMessage_loaned.xml' | ActionEventResultQualifier.QUALIFIER_CANCELLED | Status.SLNP_REQUESTER_CANCELLED | [ 'RS-TESTCASE-ISO-2' ]
    }

//    void "Test initial state transition to result state by performed action"(
//            String tenantId,
//            String requestTitle,
//            String requestAuthor,
//            String requestSystemId,
//            String requestPatronId,
//            String requestSymbol,
//            String initialState,
//            String resultState,
//            String action,
//            String jsonFileName,
//            Boolean isRequester) {
//        when: "Performing the action"
//
//        Tenants.withId(tenantId.toLowerCase()+'_mod_rs') {
//            // Define headers
//            def headers = [
//                    'X-Okapi-Tenant': tenantId,
//                    'X-Okapi-Token': 'dummy',
//                    'X-Okapi-User-Id': 'dummy',
//                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
//            ]
//
//            setHeaders(headers);
//
//            // Save the app settings
//            AppSetting setting = AppSetting.findByKey(isRequester ? SettingsData.SETTING_STATE_MODEL_REQUESTER : SettingsData.SETTING_STATE_MODEL_RESPONDER);
//            setting.value = isRequester ? StateModel.MODEL_SLNP_REQUESTER : StateModel.MODEL_SLNP_RESPONDER;
//            setting.save(flush: true, failOnError: true)
//
//            // Create PatronRequest
//            PatronRequest slnpPatronRequest = createPatronRequest(initialState, requestPatronId, requestTitle, requestAuthor, requestSymbol, requestSystemId, action, isRequester);
//            log.debug("Created patron request: ${slnpPatronRequest} ID: ${slnpPatronRequest?.id}");
//
//            // Validate initial status
//            NewStatusResult newResultStatus = statusService.lookupStatus(slnpPatronRequest, null, null, true, false);
//            validateStateTransition(newResultStatus, initialState);
//
//            // Perform action
//            performAction(slnpPatronRequest?.id, jsonFileName);
//
//            // Validate result status after performed action
//            newResultStatus = statusService.lookupStatus(slnpPatronRequest, action, null, true, true);
//            validateStateTransition(newResultStatus, resultState);
//        }
//
//        then: "Check values"
//        assert true;
//
//        where:
//        tenantId    | requestTitle  | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol | initialState                        | resultState                          | action                                               | jsonFileName                  | isRequester
//        'RSInstOne' | 'request1'    | 'test1'       | '1234-5678-9123-1231' | '9876-1231'       | 'ISIL:RST1'   | 'SLNP_REQ_IDLE'                     | 'SLNP_REQ_CANCELLED'                 | Actions.ACTION_REQUESTER_CANCEL_LOCAL                | 'slnpRequesterCancelLocal'    | true
//        'RSInstOne' | 'request2'    | 'test2'       | '1234-5678-9123-1232' | '9876-1232'       | 'ISIL:RST1'   | 'SLNP_REQ_ABORTED'                  | 'SLNP_REQ_CANCELLED'                 | Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT           | 'slnpRequesterHandleAbort'    | true
//        'RSInstOne' | 'request3'    | 'test3'       | '1234-5678-9123-1233' | '9876-1233'       | 'ISIL:RST1'   | 'SLNP_REQ_SHIPPED'                  | 'SLNP_REQ_CHECKED_IN'                | Actions.ACTION_REQUESTER_REQUESTER_RECEIVED          | 'requesterReceived'           | true
//        'RSInstOne' | 'request4'    | 'test4'       | '1234-5678-9123-1234' | '9876-1234'       | 'ISIL:RST1'   | 'SLNP_REQ_SHIPPED'                  | 'SLNP_REQ_SHIPPED'                   | Actions.ACTION_SLNP_REQUESTER_PRINT_PULL_SLIP        | 'slnpRequesterPrintPullSlip'  | true
//        'RSInstOne' | 'request5'    | 'test5'       | '1234-5678-9123-1235' | '9876-1235'       | 'ISIL:RST1'   | 'SLNP_REQ_CHECKED_IN'               | 'SLNP_REQ_AWAITING_RETURN_SHIPPING'  | Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM        | 'patronReturnedItem'          | true
//        'RSInstOne' | 'request6'    | 'test6'       | '1234-5678-9123-1236' | '9876-1236'       | 'ISIL:RST1'   | 'SLNP_REQ_CHECKED_IN'               | 'SLNP_REQ_CHECKED_IN'                | Actions.ACTION_SLNP_REQUESTER_PRINT_PULL_SLIP        | 'slnpRequesterPrintPullSlip'  | true
//        'RSInstOne' | 'request7'    | 'test7'       | '1234-5678-9123-1237' | '9876-1237'       | 'ISIL:RST1'   | 'SLNP_REQ_AWAITING_RETURN_SHIPPING' | 'SLNP_REQ_COMPLETE'                  | Actions.ACTION_REQUESTER_SHIPPED_RETURN              | 'shippedReturn'               | true
//        'RSInstOne' | 'respond8'    | 'test8'       | '1234-5678-9123-1238' | '9876-1238'       | 'ISIL:RST1'   | 'SLNP_RES_IDLE'                     | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'       | Actions.ACTION_RESPONDER_RESPOND_YES                 | 'supplierAnswerYes'           | false
//        'RSInstOne' | 'respond9'    | 'test9'       | '1234-5678-9123-1239' | '9876-1239'       | 'ISIL:RST1'   | 'SLNP_RES_IDLE'                     | 'SLNP_RES_UNFILLED'                  | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY      | 'supplierCannotSupply'        | false
//        'RSInstOne' | 'respond10'   | 'test10'      | '1234-5678-9123-1240' | '9876-1240'       | 'ISIL:RST1'   | 'SLNP_RES_IDLE'                     | 'SLNP_RES_ABORTED'                   | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY           | 'slnpResponderAbortSupply'    | false
//        'RSInstOne' | 'respond11'   | 'test11'      | '1234-5678-9123-1241' | '9876-1241'       | 'ISIL:RST1'   | 'SLNP_RES_IDLE'                     | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'       | Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY | 'supplierConditionalSupply'   | false
//        'RSInstOne' | 'respond12'   | 'test12'      | '1234-5678-9123-1242' | '9876-1242'       | 'ISIL:RST1'   | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'      | 'SLNP_RES_AWAIT_PICKING'             | Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP    | 'supplierPrintPullSlip'       | false
//        'RSInstOne' | 'respond13'   | 'test13'      | '1234-5678-9123-1243' | '9876-1243'       | 'ISIL:RST1'   | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'      | 'SLNP_RES_UNFILLED'                  | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY      | 'supplierCannotSupply'        | false
//        'RSInstOne' | 'respond14'   | 'test14'      | '1234-5678-9123-1244' | '9876-1244'       | 'ISIL:RST1'   | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'      | 'SLNP_RES_ABORTED'                   | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY           | 'slnpResponderAbortSupply'    | false
//        'RSInstOne' | 'respond15'   | 'test15'      | '1234-5678-9123-1245' | '9876-1245'       | 'ISIL:RST1'   | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'      | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'       | Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY | 'supplierConditionalSupply'   | false
//        'RSInstOne' | 'respond16'   | 'test16'      | '1234-5678-9123-1246' | '9876-1246'       | 'ISIL:RST1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_AWAIT_SHIP'                | Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE | 'supplierCheckInToReshare'    | false
//        'RSInstOne' | 'respond17'   | 'test17'      | '1234-5678-9123-1247' | '9876-1247'       | 'ISIL:RST1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_AWAIT_PICKING'             | Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY | 'supplierConditionalSupply'   | false
//        'RSInstOne' | 'respond18'   | 'test18'      | '1234-5678-9123-1248' | '9876-1248'       | 'ISIL:RST1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_UNFILLED'                  | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY      | 'supplierCannotSupply'        | false
//        'RSInstOne' | 'respond19'   | 'test19'      | '1234-5678-9123-1249' | '9876-1249'       | 'ISIL:RST1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_AWAIT_PICKING'             | Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP    | 'supplierPrintPullSlip'       | false
//        'RSInstOne' | 'respond20'   | 'test20'      | '1234-5678-9123-1250' | '9876-1250'       | 'ISIL:RST1'   | 'SLNP_RES_AWAIT_SHIP'               | 'SLNP_RES_ITEM_SHIPPED'              | Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED       | 'supplierMarkShipped'         | false
//        'RSInstOne' | 'respond21'   | 'test21'      | '1234-5678-9123-1251' | '9876-1251'       | 'ISIL:RST1'   | 'SLNP_RES_AWAIT_SHIP'               | 'SLNP_RES_AWAIT_SHIP'                | Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY | 'supplierConditionalSupply'   | false
//        'RSInstOne' | 'respond22'   | 'test22'      | '1234-5678-9123-1252' | '9876-1252'       | 'ISIL:RST1'   | 'SLNP_RES_ITEM_SHIPPED'             | 'SLNP_RES_COMPLETE'                  | Actions.ACTION_RESPONDER_ITEM_RETURNED               | 'supplierItemReturned'        | false
//    }
//
//    void "Test undo action"(
//            String tenantId,
//            String requestTitle,
//            String requestAuthor,
//            String requestSystemId,
//            String requestPatronId,
//            String requestSymbol) {
//        when: "Performing the action"
//
//        Tenants.withId(tenantId.toLowerCase()+'_mod_rs') {
//            // Define headers
//            def headers = [
//                    'X-Okapi-Tenant': tenantId,
//                    'X-Okapi-Token': 'dummy',
//                    'X-Okapi-User-Id': 'dummy',
//                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
//            ]
//
//            setHeaders(headers);
//
//            // Save the app settings
//            AppSetting setting = AppSetting.findByKey(SettingsData.SETTING_STATE_MODEL_RESPONDER);
//            setting.value = StateModel.MODEL_SLNP_RESPONDER;
//            setting.save(flush: true, failOnError: true);
//
//            // Create PatronRequest
//            PatronRequest slnpPatronRequest = createPatronRequest(Status.SLNP_RESPONDER_AWAIT_PICKING, requestPatronId, requestTitle, requestAuthor, requestSymbol, requestSystemId, Actions.ACTION_UNDO, false);
//            log.debug("Created patron request: ${slnpPatronRequest} ID: ${slnpPatronRequest?.id}");
//
//            // Validate initial state
//            NewStatusResult newResultStatus = statusService.lookupStatus(slnpPatronRequest, null, null, true, true);
//            validateStateTransition(newResultStatus, Status.SLNP_RESPONDER_AWAIT_PICKING);
//
//            // Perform supplierCheckInToReshare action
//            performAction(slnpPatronRequest?.id, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE);
//
//            // Validate status after action - supplierCheckInToReshare
//            newResultStatus = statusService.lookupStatus(slnpPatronRequest, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, null, true, true);
//            validateStateTransition(newResultStatus, Status.SLNP_RESPONDER_AWAIT_SHIP);
//
//            // Perform undo action
//            performAction(slnpPatronRequest?.id, Actions.ACTION_UNDO);
//
//            // Validate status after action - undo
//            newResultStatus = statusService.lookupStatus(slnpPatronRequest, Actions.ACTION_UNDO, null, true, true);
//            validateStateTransition(newResultStatus, Status.SLNP_RESPONDER_AWAIT_PICKING);
//        }
//
//        then: "Check values"
//        assert true;
//
//        where:
//        tenantId    | requestTitle  | requestAuthor | requestSystemId         | requestPatronId   | requestSymbol
//        'RSInstOne' | 'respond1'    | 'test1'       | '1234-5678-9123-12599 ' | '9876-1252'       | 'ISIL:RST1'
//    }
}