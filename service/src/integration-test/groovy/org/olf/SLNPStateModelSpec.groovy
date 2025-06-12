package org.olf

import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.web.databinding.GrailsWebDataBinder
import groovy.util.logging.Slf4j
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.DirectoryEntryService
import org.olf.rs.PatronRequest
import org.olf.rs.routing.RankedSupplier
import org.olf.rs.routing.StaticRouterService
import org.olf.rs.statemodel.*
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Stepwise

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SLNPStateModelSpec extends TestBase {

    @Shared
    private static List<Map> DIRECTORY_INFO = [
            [ id:'RS-T-D-0011', name: 'RSSlnpOne', slug:'RS_SLNP_ONE',     symbols: [[ authority:'ISIL', symbol:'RSS1', priority:'a'] ],
              services:[
                      [
                              slug:'RSSlnpOne_ISO18626',
                              service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
                              customProperties:[
                                      'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
                                      'AdditionalHeaders':['X-Okapi-Tenant:RSSlnpOne']
                              ]
                      ]
              ]
            ],
            [ id:'RS-T-D-0012', name: 'RSSlnpTwo', slug:'RS_SLNP_TWO',     symbols: [[ authority:'ISIL', symbol:'RSS2', priority:'a'] ],
              services:[
                      [
                              slug:'RSSlnpTwo_ISO18626',
                              service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
                              customProperties:[
                                      'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
                                      'AdditionalHeaders':['X-Okapi-Tenant:RSSlnpTwo']
                              ]
                      ]
              ]
            ],
            [ id:'RS-T-D-0013', name: 'RSSlnpThree', slug:'RS_SLNP_THREE', symbols: [[ authority:'ISIL', symbol:'RSS3', priority:'a'] ],
              services:[
                      [
                              slug:'RSSlnpThree_ISO18626',
                              service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
                              customProperties:[
                                      'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
                                      'AdditionalHeaders':['X-Okapi-Tenant:RSSlnpThree']
                              ]
                      ]
              ]
            ]
    ]

    GrailsWebDataBinder grailsWebDataBinder
    DirectoryEntryService directoryEntryService
    StaticRouterService staticRouterService
    StatusService statusService

    // This method is declared in the HttpSpec
    def setupSpecWithSpring() {
        super.setupSpecWithSpring();
    }

    def setupSpec() {}

    def setup() {
        if ( testctx.slnpInitialised == null ) {
            log.debug("Inject actual runtime port number (${serverPort}) into directory entries (${baseUrl}) ");
            for ( Map entry: DIRECTORY_INFO ) {
                if ( entry.services != null ) {
                    for ( Map svc: entry.services ) {
                        svc.service.address = "${baseUrl}rs/externalApi/iso18626".toString()
                        log.debug("${entry.id}/${entry.name}/${svc.slug}/${svc.service.name} - address updated to ${svc.service.address}");
                    }
                }
            }
            testctx.slnpInitialised = true
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
        'RSSlnpOne'   | 'RSSlnpOne'
        'RSSlnpTwo'   | 'RSSlnpTwo'
        'RSSlnpThree' | 'RSSlnpThree'
    }

    void "Set up test tenants "(tenantid, name) {
        when:"We post a new tenant request to the OKAPI controller"
        boolean response = setupTenant(tenantid, name);

        then:"The response is correct"
        assert(response);

        where:
        tenantid      | name
        'RSSlnpOne'   | 'RSSlnpOne'
        'RSSlnpTwo'   | 'RSSlnpTwo'
        'RSSlnpThree' | 'RSSlnpThree'
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
        tenant_id     | dirents
        'RSSlnpOne'   | DIRECTORY_INFO
        'RSSlnpTwo'   | DIRECTORY_INFO
        'RSSlnpThree' | DIRECTORY_INFO
    }

    /** Grab the settings for each tenant so we can modify them as needeed and send back,
     *  then work through the list posting back any changes needed for that particular tenant in this testing setup
     *  for now, disable all auto responders
     *  N.B. that the test "Send request using static router" below RELIES upon the static routes assigned to RSSlnpOne.
     *  changing this data may well break that test.
     */
    void "Configure Tenants for Mock Lending"(String tenant_id, Map changes_needed, Map changes_needed_hidden) {
        when:"We fetch the existing settings for ${tenant_id}"
        changeSettings(tenant_id, changes_needed);
        changeSettings(tenant_id, changes_needed_hidden, true)

        then:"Tenant is configured"
        1==1

        where:
        tenant_id      | changes_needed                                                                                                                                                                        | changes_needed_hidden
        'RSSlnpOne'    | [ 'copy_auto_responder_status':'off', 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'use_request_item':'ncip', 'static_routes':'SYMBOL:ISIL:RSS3,SYMBOL:ISIL:RSS2', 'loggingISO18626':'yes'] | ['requester_returnables_state_model':'SLNPRequester', 'responder_returnables_state_model':'SLNPResponder', 'requester_non_returnables_state_model':'SLNPNonReturnableRequester', 'responder_non_returnables_state_model':'SLNPNonReturnableResponder']
        'RSSlnpTwo'    | [ 'copy_auto_responder_status':'off', 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'use_request_item':'ncip', 'static_routes':'SYMBOL:ISIL:RSS1,SYMBOL:ISIL:RSS3', 'loggingISO18626':'yes'] | ['requester_returnables_state_model':'SLNPRequester', 'responder_returnables_state_model':'SLNPResponder', 'requester_non_returnables_state_model':'SLNPNonReturnableRequester', 'responder_non_returnables_state_model':'SLNPNonReturnableResponder']
        'RSSlnpThree'  | [ 'copy_auto_responder_status':'off', 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'use_request_item':'ncip', 'static_routes':'SYMBOL:ISIL:RSS1', 'loggingISO18626':'yes']                  | ['requester_returnables_state_model':'SLNPRequester', 'responder_returnables_state_model':'SLNPResponder', 'requester_non_returnables_state_model':'SLNPNonReturnableRequester', 'responder_non_returnables_state_model':'SLNPNonReturnableResponder']
    }

    void "Validate Static Router"() {

        when:"We call the static router"
        List<RankedSupplier> resolved_rota = null;
        Tenants.withId('RSSlnpOne_mod_rs'.toLowerCase()) {
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
            String tenant,
            String state,
            String requestPatronId,
            String requestTitle,
            String requestAuthor,
            String requestSymbol,
            String requestSystemId,
            Boolean isRequester,
            String hrid,
            String serviceType) {

        Map request = [
                patronReference: requestPatronId + "_test",
                title: requestTitle,
                author: requestAuthor,
                requestingInstitutionSymbol: requestSymbol,
                supplyingInstitutionSymbol: 'ISIL:RSS3',
                resolvedRequester:[ authority:'ISIL', symbol:'RSS1', priority:'a'],
                resolvedSupplier:[ authority:'ISIL', symbol:'RSS3', priority:'a'],
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: isRequester,
                hrid: hrid,
                serviceType: serviceType

        ];
        def resp = doPost("${baseUrl}/rs/patronrequests".toString(), request);
        log.info("new Request created: ${resp.id}")
        waitForNewEventProcessed(tenant, 10000L, resp?.id)
        PatronRequest slnpPatronRequest = PatronRequest.get(resp?.id);
        Status initialStatus = Status.lookup(state);
        slnpPatronRequest.state = initialStatus;
        return slnpPatronRequest.save(flush: true, failOnError: true);
    }

    private PatronRequest createPatronRequestWithoutInitialState(
            String requestPatronId,
            String requestTitle,
            String requestAuthor,
            String supplierSymbol,
            String requestSystemId,
            Boolean isRequester,
            String hrid,
            String peerRequestIdentifier,
            String serviceType) {

        Map request = [
                patronReference: requestPatronId + "_test",
                title: requestTitle,
                author: requestAuthor,
                supplyingInstitutionSymbol: supplierSymbol,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: isRequester,
                hrid: hrid,
                peerRequestIdentifier: peerRequestIdentifier,
                customIdentifiers: '{"schemeValue": "ZFL","identifiers": [{"key": "TitelId","value": "in00000000002"},{"key": "BsTyp2","value": "L"},{"key": "MedTyp","value": "ml"}]}',
                serviceType: serviceType
        ];
        def resp = doPost("${baseUrl}/rs/patronrequests".toString(), request);
        log.info("new Request created: ${resp.id}")
    }

    private static Symbol symbolFromString(String symbolString) {
        return DirectoryEntryService.resolveCombinedSymbol(symbolString);
    }

    private PatronRequest createPatronRequest(
            String tenant,
            String state,
            String requestPatronId,
            String requestTitle,
            String requestAuthor,
            String requestSymbol,
            String supplierSymbol,
            String requestSystemId,
            Boolean isRequester,
            String hrid,
            String peerRequestIdentifier,
            String serviceType) {

        Map request = [
                patronReference: requestPatronId + "_test",
                title: requestTitle,
                author: requestAuthor,
                requestingInstitutionSymbol: requestSymbol,
                supplyingInstitutionSymbol: supplierSymbol,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: isRequester,
                hrid: hrid,
                peerRequestIdentifier: peerRequestIdentifier,
                serviceType: serviceType
        ];
        def resp = doPost("${baseUrl}/rs/patronrequests".toString(), request);
        log.info("new Request created: ${resp.id}")
        waitForNewEventProcessed(tenant, 10000L, resp?.id)
        PatronRequest slnpPatronRequest = PatronRequest.get(resp?.id);
        Status initialStatus = Status.lookup(state);
        slnpPatronRequest.state = initialStatus;

        Symbol reqSymbol = symbolFromString(requestSymbol);
        Symbol suppSymbol = symbolFromString(supplierSymbol);

        slnpPatronRequest.resolvedRequester = reqSymbol;
        slnpPatronRequest.resolvedSupplier = suppSymbol;

        return slnpPatronRequest.save(flush: true, failOnError: true);
    }

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

    private String waitForNewEventProcessed(String tenant, long timeout, String id) {
        long start_time = System.currentTimeMillis();
        String request_id = null;
        Boolean needsAttention = null;
        long elapsed = 0;
        while ( ( needsAttention == null) &&
                ( elapsed < timeout ) ) {

            setHeaders([ 'X-Okapi-Tenant': tenant ]);
            // https://east-okapi.folio-dev.indexdata.com/rs/patronrequests?filters=isRequester%3D%3Dtrue&match=patronGivenName&perPage=100&sort=dateCreated%3Bdesc&stats=true&term=Michelle
            def resp = doGet("${baseUrl}rs/patronrequests",
                    [
                            'max':'100',
                            'offset':'0',
                            'match':'id',
                            'term':id
                    ])
            if ( resp?.size() == 1 ) {
                request_id = resp[0].id
                needsAttention = resp[0].needsAttention
            } else {
                log.debug("waitForRequestState: Request with hrid ${id} not found");
            }

            if ( needsAttention == null ) {
                // Request not found OR not yet in required state
                log.debug("Not yet found.. sleeping");
                Thread.sleep(500);
            }
            elapsed = System.currentTimeMillis() - start_time
        }

        if ( needsAttention == null ) {
            throw new Exception("Expected needsAttention to be FALSE but is ${needsAttention}");
        }

        return request_id;
    }

    void "Test end to end actions and events from supplier to requester"(
            String requesterTenantId,
            String responderTenantId,
            String requesterSymbol,
            String responderSymbol,
            String requesterInitialState,
            String requesterResultState,
            String responderInitialState,
            String responderResultState,
            String patronId,
            String title,
            String author,
            String action,
            String jsonFileName,
            String qualifier,
            boolean isAutoResponder
    ) {
        when: "Creating the Requester/Responder Patron Requests"

        String requesterSystemId = UUID.randomUUID().toString();
        String responderSystemId = UUID.randomUUID().toString();

        String hrid = Long.toUnsignedString(new Random().nextLong(), 16).toUpperCase();

        String requesterRequest;
        String responderRequest;

        // Create Requester PatronRequest
        PatronRequest requesterPatronRequest;
        Tenants.withId(requesterTenantId.toLowerCase() + '_mod_rs') {
            // Define headers
            def requesterHeaders = [
                    'X-Okapi-Tenant'     : requesterTenantId,
                    'X-Okapi-Token'      : 'dummy',
                    'X-Okapi-User-Id'    : 'dummy',
                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
            ]

            setHeaders(requesterHeaders);

            // Create PatronRequest
            requesterPatronRequest = createPatronRequest(requesterTenantId, requesterInitialState, patronId, title, author, requesterSymbol,
                    responderSymbol, requesterSystemId, true, hrid, hrid, 'loan');

            log.debug("Created patron request: ${requesterPatronRequest} ID: ${requesterPatronRequest?.id}");

            // Validate Requester initial status
            requesterRequest = waitForRequestStateByHrid(requesterTenantId, 20000, hrid, requesterInitialState)

            and: "Check requester initial status"
            assert requesterRequest != null
        }

        // Create Responder PatronRequest
        PatronRequest responderPatronRequest;
        Tenants.withId(responderTenantId.toLowerCase() + '_mod_rs') {
            // Define headers
            def headers = [
                    'X-Okapi-Tenant'     : responderTenantId,
                    'X-Okapi-Token'      : 'dummy',
                    'X-Okapi-User-Id'    : 'dummy',
                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
            ]

            setHeaders(headers);

            // Create PatronRequest
            responderPatronRequest = createPatronRequest(responderTenantId, responderInitialState, patronId, title, author, requesterSymbol,
                    responderSymbol, responderSystemId, false, hrid, hrid, 'loan');
            log.debug("Created patron request: ${responderPatronRequest} ID: ${responderPatronRequest?.id}");

            // Set Auto Responder
            changeSettings(responderTenantId, [ 'auto_responder_status' : isAutoResponder ? 'on:_loaned_and_cannot_supply' : 'off' ]);

            // Validate Responder initial status
            responderRequest = waitForRequestStateByHrid(responderTenantId, 20000, hrid, responderInitialState)

            and: "Check responder initial status"
            assert responderRequest != null

            // Perform action
            performAction(responderPatronRequest?.id, jsonFileName);
        }

        // Validate Requester/Responder states after performed actions/events
        responderRequest = waitForRequestStateByHrid(responderTenantId, 20000, hrid, responderResultState)
        requesterRequest = waitForRequestStateByHrid(requesterTenantId, 20000, hrid, requesterResultState)

        and: "Check the return value"
        assert responderRequest != null
        assert requesterRequest != null

        then: "Check values"
        assert true;

        where:
        requesterTenantId | responderTenantId | requesterSymbol | responderSymbol | requesterInitialState       | requesterResultState               | responderInitialState                      | responderResultState                | patronId    | title     | author     | action                                                       | jsonFileName                           | qualifier                                     | isAutoResponder
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_SHIPPED      | Status.SLNP_RESPONDER_AWAIT_PICKING        | Status.SLNP_RESPONDER_ITEM_SHIPPED  | '7732-4367' | 'title1'  | 'Author1'  | Actions.ACTION_SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED | 'slnpSupplierFillAndMarkShipped'       | ActionEventResultQualifier.QUALIFIER_LOANED   | false
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_ABORTED      | Status.SLNP_RESPONDER_IDLE                 | Status.SLNP_RESPONDER_ABORTED       | '7732-4364' | 'title2'  | 'Author2'  | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY                   | 'slnpResponderAbortSupply'             | ActionEventResultQualifier.QUALIFIER_ABORTED  | false
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_ABORTED      | Status.SLNP_RESPONDER_AWAIT_PICKING        | Status.SLNP_RESPONDER_ABORTED       | '7732-4362' | 'title3'  | 'Author3'  | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY                   | 'slnpResponderAbortSupply'             | ActionEventResultQualifier.QUALIFIER_ABORTED  | false
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_ABORTED      | Status.SLNP_RESPONDER_IDLE                 | Status.SLNP_RESPONDER_ABORTED       | '7732-4365' | 'title4'  | 'Author4'  | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY                   | 'slnpResponderAbortSupplyWithoutNote'  | ActionEventResultQualifier.QUALIFIER_ABORTED  | false
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_ABORTED      | Status.SLNP_RESPONDER_AWAIT_PICKING        | Status.SLNP_RESPONDER_ABORTED       | '7732-4369' | 'title5'  | 'Author5'  | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY                   | 'slnpResponderAbortSupplyWithoutNote'  | ActionEventResultQualifier.QUALIFIER_ABORTED  | false
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_CANCELLED    | Status.SLNP_RESPONDER_IDLE                 | Status.SLNP_RESPONDER_ABORTED       | '7732-4363' | 'title6'  | 'Author6'  | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY                   | 'slnpResponderAbortSupplyReasonFalse'  | ActionEventResultQualifier.QUALIFIER_ABORTED  | false
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_CANCELLED    | Status.SLNP_RESPONDER_AWAIT_PICKING        | Status.SLNP_RESPONDER_ABORTED       | '7732-4368' | 'title7'  | 'Author7'  | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY                   | 'slnpResponderAbortSupplyReasonFalse'  | ActionEventResultQualifier.QUALIFIER_ABORTED  | false
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

        // Set Auto Responder
        changeSettings(tenant_id, [ 'auto_responder_status' : 'off' ])

        def resp = doPost("${baseUrl}/rs/externalApi/iso18626".toString(), requestXml)

        log.debug("CreateReqTest2 -- Response: RESP:${resp.ISO18626Message} ")

        String req_request = waitForRequestStateByHrid(tenant_id, 20000, requestId, Status.SLNP_REQUESTER_IDLE)
        waitForNewEventProcessed(tenant_id, 10000, req_request)
        log.debug("Created new request for iso test case 1. RESQUESTER ID is : ${req_request}")

        String messageXml = new File("src/integration-test/resources/isoMessages/${messageFile}").text
        messageXml = messageXml.replace('agencyIdValue_holder', agencyIdValue)
                .replace('requestId_holder', requestId)
                .replace('supAgencyIdValue_holder', supAgencyId)
                .replace('status_holder',  statusChanged == ActionEventResultQualifier.QUALIFIER_ABORTED ? ActionEventResultQualifier.QUALIFIER_CANCELLED : statusChanged)
                .replace('note_holder', statusChanged == ActionEventResultQualifier.QUALIFIER_ABORTED ? '#ABORT#' : '')

        setHeaders([
                'X-Okapi-Tenant': tenant_id,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ])
        doPost("${baseUrl}/rs/externalApi/iso18626".toString(), messageXml)

        String message_request = waitForRequestStateByHrid(tenant_id, 20000, requestId, finalStatus)
        log.debug("Updated status. RESQUESTER ID is : ${message_request} with status: ${finalStatus}")

        then:"Check the return value"
        assert req_request != null
        assert message_request != null

        where:
        tenant_id   | peer_tenant   | agencyIdValue | supAgencyId | requestId     | patronId    | requestFile         | requesting_symbol | messageFile                               | statusChanged                                  | finalStatus                     | tags
        'RSSlnpOne' | 'RSSlnpThree' | 'RSS1'        | 'RSS3'      | '1234-5678-1' | '1234-5679' | 'patronRequest.xml' | 'ISIL:RSS1'       | 'supplyingAgencyMessage_statusChange.xml' | ActionEventResultQualifier.QUALIFIER_LOANED    | Status.SLNP_REQUESTER_SHIPPED   | [ 'RS-TESTCASE-ISO-1' ]
        'RSSlnpOne' | 'RSSlnpThree' | 'RSS1'        | 'RSS3'      | '1234-5678-2' | '1234-567a' | 'patronRequest.xml' | 'ISIL:RSS1'       | 'supplyingAgencyMessage_statusChange.xml' | ActionEventResultQualifier.QUALIFIER_CANCELLED | Status.SLNP_REQUESTER_CANCELLED | [ 'RS-TESTCASE-ISO-2' ]
        'RSSlnpOne' | 'RSSlnpThree' | 'RSS1'        | 'RSS3'      | '1234-5678-3' | '1234-5671' | 'patronRequest.xml' | 'ISIL:RSS1'       | 'supplyingAgencyMessage_statusChange.xml' | ActionEventResultQualifier.QUALIFIER_ABORTED   | Status.SLNP_REQUESTER_ABORTED   | [ 'RS-TESTCASE-ISO-3' ]
    }

    void "Send ISO request and retry"(String tenant_id,
                            String peer_tenant,
                            String agencyIdValue,
                            String requestId,
                            String patronId,
                            String requestFile,
                            String requesting_symbol,
                            String messageFile,
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

        // Set Auto Responder
        changeSettings(tenant_id, [ 'auto_responder_status' : 'off' ])

        def resp = doPost("${baseUrl}/rs/externalApi/iso18626".toString(), requestXml)

        log.debug("CreateReqTest2 -- Response: RESP:${resp.ISO18626Message} ")

        String req_request = waitForRequestStateByHrid(tenant_id, 20000, requestId, Status.SLNP_REQUESTER_IDLE)
        waitForNewEventProcessed(tenant_id, 10000, req_request)
        log.debug("Created new request for iso test case 1. RESQUESTER ID is : ${req_request}")

        String messageXml = new File("src/integration-test/resources/isoMessages/${messageFile}").text
        messageXml = messageXml.replace('requestId_holder', requestId)

        setHeaders([
                'X-Okapi-Tenant': tenant_id,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ])
        doPost("${baseUrl}/rs/externalApi/iso18626".toString(), messageXml)

        String message_request = waitForRequestStateByHrid(tenant_id, 20000, requestId, finalStatus)
        log.debug("Updated status. RESQUESTER ID is : ${message_request} with status: ${finalStatus}")

        setHeaders([ 'X-Okapi-Tenant': tenant_id ]);
        def request = doGet("${baseUrl}rs/patronrequests",
                [
                        'max':'100',
                        'offset':'0',
                        'match':'hrid',
                        'term':requestId
                ])

        then:"Check the return value"
        assert req_request != null
        assert message_request != null
        assert request[0].requestingInstitutionSymbol == "ISIL:RSS1"

        where:
        tenant_id   | peer_tenant   | agencyIdValue | requestId     | patronId    | requestFile         | requesting_symbol | messageFile              | finalStatus                 | tags
        'RSSlnpOne' | 'RSSlnpThree' | 'RSS1'        | '1234-5678-8' | '1234-5679' | 'patronRequest.xml' | 'ISIL:RSS1'       | 'patronRequestRetry.xml' | Status.SLNP_REQUESTER_IDLE  | [ 'RS-TESTCASE-ISO-RETRY-1' ]
    }

    void "Test initial state transition to result state by performed action"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol,
            String initialState,
            String resultState,
            String action,
            String jsonFileName,
            Boolean isRequester) {
        when: "Performing the action"

        Tenants.withId(tenantId.toLowerCase()+'_mod_rs') {
            // Define headers
            def headers = [
                    'X-Okapi-Tenant': tenantId,
                    'X-Okapi-Token': 'dummy',
                    'X-Okapi-User-Id': 'dummy',
                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
            ]

            setHeaders(headers);

            // Create PatronRequest
            String hrid = Long.toUnsignedString(new Random().nextLong(), 16).toUpperCase();
            PatronRequest slnpPatronRequest = createPatronRequest(tenantId, initialState, requestPatronId, requestTitle, requestAuthor, requestSymbol,
                    requestSystemId, isRequester, hrid, 'loan');
            log.debug("Created patron request: ${slnpPatronRequest} ID: ${slnpPatronRequest?.id}");

            // Validate initial status
            String request = waitForRequestStateByHrid(tenantId, 20000, hrid, initialState)

            and: "Check initial state"
            assert request != null

            // Perform action
            performAction(slnpPatronRequest?.id, jsonFileName);

            // Validate result status after performed action
            NewStatusResult newResultStatus = statusService.lookupStatus(slnpPatronRequest, action, null, true, true);
            validateStateTransition(newResultStatus, resultState);
        }

        then: "Check values"
        assert true;

        where:
        tenantId      | requestTitle  | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol | initialState                        | resultState                          | action                                                       | jsonFileName                         | isRequester
        'RSSlnpOne'   | 'request1'    | 'test1'       | '1234-5678-9123-1231' | '9876-1231'       | 'ISIL:RSS1'   | 'SLNP_REQ_IDLE'                     | 'SLNP_REQ_CANCELLED'                 | Actions.ACTION_REQUESTER_REQUESTER_CANCEL                    | 'slnpRequesterCancel'                | true
        'RSSlnpOne'   | 'request2'    | 'test2'       | '1234-5678-9123-1232' | '9876-1232'       | 'ISIL:RSS1'   | 'SLNP_REQ_ABORTED'                  | 'SLNP_REQ_CANCELLED'                 | Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT                   | 'slnpRequesterHandleAbort'           | true
        'RSSlnpOne'   | 'request3'    | 'test3'       | '1234-5678-9123-1233' | '9876-1233'       | 'ISIL:RSS1'   | 'SLNP_REQ_SHIPPED'                  | 'SLNP_REQ_CHECKED_IN'                | Actions.ACTION_SLNP_REQUESTER_REQUESTER_RECEIVED             | 'slnpRequesterRequesterReceived'     | true
        'RSSlnpOne'   | 'request4'    | 'test4'       | '1234-5678-9123-1234' | '9876-1235'       | 'ISIL:RSS1'   | 'SLNP_REQ_CHECKED_IN'               | 'SLNP_REQ_AWAITING_RETURN_SHIPPING'  | Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM                | 'patronReturnedItem'                 | true
        'RSSlnpOne'   | 'request41'   | 'test41'      | '1234-5678-9123-1234' | '9876-1235'       | 'ISIL:RSS1'   | 'SLNP_REQ_CHECKED_IN'               | 'SLNP_REQ_COMPLETE'                  | Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED    | 'patronReturnedItemAndShippedReturn' | true
        'RSSlnpOne'   | 'request5'    | 'test5'       | '1234-5678-9123-1235' | '9876-1237'       | 'ISIL:RSS1'   | 'SLNP_REQ_AWAITING_RETURN_SHIPPING' | 'SLNP_REQ_COMPLETE'                  | Actions.ACTION_REQUESTER_SHIPPED_RETURN                      | 'shippedReturn'                      | true
        'RSSlnpOne'   | 'request6'    | 'test6'       | '1234-5678-9123-1299' | '9876-1299'       | 'ISIL:RSS1'   | 'SLNP_REQ_CHECKED_IN'               | 'SLNP_REQ_ITEM_LOST'                 | Actions.ACTION_SLNP_REQUESTER_MARK_ITEM_LOST                 | 'slnpRequesterMarkItemLost'          | true
        'RSSlnpOne'   | 'respond1'    | 'test8'       | '1234-5678-9123-1238' | '9876-1238'       | 'ISIL:RSS1'   | 'SLNP_RES_IDLE'                     | 'SLNP_RES_AWAIT_PICKING'             | Actions.ACTION_SLNP_RESPONDER_RESPOND_YES                    | 'slnpSupplierAnswerYes'              | false
        'RSSlnpOne'   | 'respond2'    | 'test9'       | '1234-5678-9123-1239' | '9876-1239'       | 'ISIL:RSS1'   | 'SLNP_RES_IDLE'                     | 'SLNP_RES_UNFILLED'                  | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY              | 'supplierCannotSupply'               | false
        'RSSlnpOne'   | 'respond3'    | 'test10'      | '1234-5678-9123-1240' | '9876-1240'       | 'ISIL:RSS1'   | 'SLNP_RES_IDLE'                     | 'SLNP_RES_ABORTED'                   | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY                   | 'slnpResponderAbortSupply'           | false
        'RSSlnpOne'   | 'respond5'    | 'test12'      | '1234-5678-9123-1246' | '9876-1246'       | 'ISIL:RSS1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_ITEM_SHIPPED'              | Actions.ACTION_SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED | 'slnpSupplierFillAndMarkShipped'     | false
        'RSSlnpOne'   | 'respond7'    | 'test14'      | '1234-5678-9123-1248' | '9876-1248'       | 'ISIL:RSS1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_UNFILLED'                  | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY              | 'supplierCannotSupply'               | false
        'RSSlnpOne'   | 'respond8'    | 'test15'      | '1234-5678-9123-1244' | '9876-1244'       | 'ISIL:RSS1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_ABORTED'                   | Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY                   | 'slnpResponderAbortSupply'           | false
        'RSSlnpOne'   | 'respond9'    | 'test16'      | '1234-5678-9123-1242' | '9876-1242'       | 'ISIL:RSS1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_AWAIT_PICKING'             | Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP            | 'supplierPrintPullSlip'              | false
        'RSSlnpThree' | 'respond10'   | 'test17'      | '1234-5678-9123-1252' | '9876-4444'       | 'ISIL:RSS3'   | 'SLNP_RES_ITEM_SHIPPED'             | 'SLNP_RES_COMPLETE'                  | Actions.ACTION_SLNP_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE   | 'slnpSupplierCheckOutOfReshare'      | false
    }

    void "Test event responder new SLNP patron request inidication service"(
            String responderTenantId,
            String responderSymbol,
            String responderInitialState,
            String responderResultState,
            String patronId,
            String title,
            String author,
            boolean autoLoanEnabled
    ) {
        when: "Create Responder Patron request"

        String hrid = Long.toUnsignedString(new Random().nextLong(), 16).toUpperCase();

        String responderRequest;

        // Create Responder PatronRequest
        PatronRequest responderPatronRequest;
        Tenants.withId(responderTenantId.toLowerCase() + '_mod_rs') {
            // Define headers
            def headers = [
                    'X-Okapi-Tenant'     : responderTenantId,
                    'X-Okapi-Token'      : 'dummy',
                    'X-Okapi-User-Id'    : 'dummy',
                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
            ]

            setHeaders(headers);

            // Set auto-loan setting
            changeSettings(responderTenantId, [ 'auto_responder_status': (autoLoanEnabled ? 'on:_loaned_and_cannot_supply' : 'off') ]);

            if (autoLoanEnabled && responderResultState == Status.SLNP_RESPONDER_UNFILLED) {
                changeSettings(responderTenantId, [ 'host_lms_integration' : '123554353424231']);
            }

            // Create PatronRequest
            responderPatronRequest = createPatronRequestWithoutInitialState(patronId, title, author,
                    responderSymbol, UUID.randomUUID().toString(), false, hrid, hrid, 'loan')
            log.debug("Created patron request: ${responderPatronRequest} ID: ${responderPatronRequest?.id}");
        }

        // Validate Responder states after performed event indication service
        responderRequest = waitForRequestStateByHrid(responderTenantId, 20000, hrid, responderResultState)

        and: "Check the return value"
        assert responderRequest != null

        then: "Check values"
        assert true;

        where:
        responderTenantId | responderSymbol | responderInitialState        | responderResultState                      | patronId        | title       | author       | autoLoanEnabled
        'RSSlnpOne'       | 'ISIL:RSS1'     | Status.SLNP_RESPONDER_IDLE   | Status.SLNP_RESPONDER_IDLE                | '7732-4367-333' | 'title123'  | 'Author123'  | false
        'RSSlnpOne'       | 'ISIL:RSS1'     | Status.SLNP_RESPONDER_IDLE   | Status.SLNP_RESPONDER_AWAIT_PICKING       | '7732-4362-331' | 'title234'  | 'Author234'  | true
        'RSSlnpOne'       | 'ISIL:RSS1'     | Status.SLNP_RESPONDER_IDLE   | Status.SLNP_RESPONDER_UNFILLED            | '7732-4364-332' | 'title345'  | 'Author345'  | true
    }

    void "Test initial state transition to result state by performed action for non returnables"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol,
            String initialState,
            String resultState,
            String action,
            String jsonFileName,
            Boolean isRequester) {
        when: "Performing the action"
        Tenants.withId(tenantId.toLowerCase()+'_mod_rs') {

            // Define headers
            def headers = [
                    'X-Okapi-Tenant': tenantId,
                    'X-Okapi-Token': 'dummy',
                    'X-Okapi-User-Id': 'dummy',
                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
            ]

            setHeaders(headers)

            // Create PatronRequest
            String hrid = Long.toUnsignedString(new Random().nextLong(), 16).toUpperCase()
            PatronRequest slnpPatronRequest = createPatronRequest(tenantId, initialState, requestPatronId, requestTitle, requestAuthor, requestSymbol,
                    requestSystemId, isRequester, hrid, 'copy')
            log.debug("Created patron request: ${slnpPatronRequest} ID: ${slnpPatronRequest?.id}")

            // Validate initial status
            String request = waitForRequestStateByHrid(tenantId, 20000, hrid, initialState)

            and: "Check initial state"
            assert request != null

            // Perform action
            performAction(slnpPatronRequest?.id, jsonFileName)

            // Validate result status after performed action
            NewStatusResult newResultStatus = statusService.lookupStatus(slnpPatronRequest, action, null, true, true)
            validateStateTransition(newResultStatus, resultState)
        }

        then: "Check values"
        assert true;

        where:
        tenantId      | requestTitle  | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol | initialState                        | resultState                          | action                                                                     | jsonFileName                                  | isRequester
        'RSSlnpOne'   | 'request1nrs' | 'test1nrs'    | '1234-5678-9123-2221' | '9876-7771'       | 'ISIL:RSS1'   | 'SLNP_REQ_IDLE'                     | 'SLNP_REQ_CANCELLED'                 | Actions.ACTION_REQUESTER_REQUESTER_CANCEL                                  | 'slnpRequesterCancel'                         | true
        'RSSlnpOne'   | 'request2nrs' | 'test2nrs'    | '1234-5678-9123-2222' | '9876-7772'       | 'ISIL:RSS1'   | 'SLNP_REQ_ABORTED'                  | 'SLNP_REQ_CANCELLED'                 | Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT                                 | 'slnpRequesterHandleAbort'                    | true
        'RSSlnpOne'   | 'request3nrs' | 'test3nrs'    | '1234-5678-9123-2223' | '9876-7773'       | 'ISIL:RSS1'   | 'SLNP_REQ_DOCUMENT_AVAILABLE'       | 'SLNP_REQ_DOCUMENT_SUPPLIED'         | Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_REQUESTER_RECEIVED            | 'slnpNonReturnableRequesterRequesterReceived' | true
        'RSSlnpOne'   | 'request4nrs' | 'test4nrs'    | '1234-5678-9123-2224' | '9876-7774'       | 'ISIL:RSS1'   | 'SLNP_REQ_IDLE'                     | 'SLNP_REQ_DOCUMENT_SUPPLIED'         | Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED        | 'manuallyMarkSupplied'                        | true
        'RSSlnpOne'   | 'request5nrs' | 'test5nrs'    | '1234-5678-9123-2234' | '9876-7274'       | 'ISIL:RSS1'   | 'SLNP_REQ_IDLE'                     | 'SLNP_REQ_DOCUMENT_AVAILABLE'        | Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE       | 'manuallyMarkAvailable'                       | true
        'RSSlnpOne'   | 'supply1nrs'  | 'test5nrs'    | '1234-5678-9123-2225' | '9876-7775'       | 'ISIL:RSS1'   | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'      | 'SLNP_RES_AWAIT_PICKING'             | Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP                          | 'supplierPrintPullSlip'                       | false
        'RSSlnpOne'   | 'supply2nrs'  | 'test6nrs'    | '1234-5678-9123-2226' | '9876-7776'       | 'ISIL:RSS1'   | 'SLNP_RES_NEW_AWAIT_PULL_SLIP'      | 'SLNP_RES_UNFILLED'                  | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY                            | 'supplierCannotSupply'                        | false
        'RSSlnpOne'   | 'supply3nrs'  | 'test7nrs'    | '1234-5678-9123-2217' | '9876-7717'       | 'ISIL:RSS1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_DOCUMENT_SUPPLIED'         | Actions.ACTION_SLNP_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT                   | 'slnpNonReturnableSupplierSuppliesDocument'   | false
        'RSSlnpOne'   | 'supply4nrs'  | 'test8nrs'    | '1234-5678-9123-2218' | '9876-7718'       | 'ISIL:RSS1'   | 'SLNP_RES_AWAIT_PICKING'            | 'SLNP_RES_UNFILLED'                  | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY                            | 'supplierCannotSupply'                        | false
    }

    void "Test end to end actions and events from supplier to requester for nonreturnables"(
            String requesterTenantId,
            String responderTenantId,
            String requesterSymbol,
            String responderSymbol,
            String requesterInitialState,
            String requesterResultState,
            String responderInitialState,
            String responderResultState,
            String patronId,
            String title,
            String author,
            String action,
            String jsonFileName
    ) {
        when: "Creating the Requester/Responder Patron Requests"

        String requesterSystemId = UUID.randomUUID().toString()
        String responderSystemId = UUID.randomUUID().toString()

        String hrid = Long.toUnsignedString(new Random().nextLong(), 16).toUpperCase()

        String requesterRequest;
        String responderRequest;

        // Create Requester PatronRequest
        PatronRequest requesterPatronRequest;
        Tenants.withId(requesterTenantId.toLowerCase() + '_mod_rs') {
            // Define headers
            def requesterHeaders = [
                    'X-Okapi-Tenant'     : requesterTenantId,
                    'X-Okapi-Token'      : 'dummy',
                    'X-Okapi-User-Id'    : 'dummy',
                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
            ]

            setHeaders(requesterHeaders);

            // Create PatronRequest
            requesterPatronRequest = createPatronRequest(requesterTenantId, requesterInitialState, patronId, title, author, requesterSymbol,
                    responderSymbol, requesterSystemId, true, hrid, hrid, 'copy');

            log.debug("Created patron request: ${requesterPatronRequest} ID: ${requesterPatronRequest?.id}")

            // Validate Requester initial status
            requesterRequest = waitForRequestStateByHrid(requesterTenantId, 20000, hrid, requesterInitialState)

            and: "Check requester initial status"
            assert requesterRequest != null
        }

        // Create Responder PatronRequest
        PatronRequest responderPatronRequest;
        Tenants.withId(responderTenantId.toLowerCase() + '_mod_rs') {

            // Define headers
            def headers = [
                    'X-Okapi-Tenant'     : responderTenantId,
                    'X-Okapi-Token'      : 'dummy',
                    'X-Okapi-User-Id'    : 'dummy',
                    'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
            ]

            setHeaders(headers);

            // Create PatronRequest
            responderPatronRequest = createPatronRequest(responderTenantId, responderInitialState, patronId, title, author, requesterSymbol,
                    responderSymbol, responderSystemId, false, hrid, hrid, 'copy');
            log.debug("Created patron request: ${responderPatronRequest} ID: ${responderPatronRequest?.id}")

            // Validate Responder initial status
            responderRequest = waitForRequestStateByHrid(responderTenantId, 20000, hrid, responderInitialState)

            and: "Check responder initial status"
            assert responderRequest != null

            // Perform action
            performAction(responderPatronRequest?.id, jsonFileName)
        }

        // Validate Requester/Responder states after performed actions/events
        responderRequest = waitForRequestStateByHrid(responderTenantId, 20000, hrid, responderResultState)
        requesterRequest = waitForRequestStateByHrid(requesterTenantId, 20000, hrid, requesterResultState)

        and: "Check the return value"
        assert responderRequest != null
        assert requesterRequest != null

        then: "Check values"
        assert true;

        where:
        requesterTenantId | responderTenantId | requesterSymbol | responderSymbol | requesterInitialState       | requesterResultState              | responderInitialState                      | responderResultState            | patronId    | title             | author     | action                                                     | jsonFileName
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_CANCELLED   | Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP  | Status.SLNP_RESPONDER_UNFILLED  | '7657-3543' | 'NonReturnable5'  | 'Rebo5'    | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY            | 'supplierCannotSupply'
        'RSSlnpTwo'       | 'RSSlnpOne'       | 'ISIL:RSS2'     | 'ISIL:RSS1'     | Status.SLNP_REQUESTER_IDLE  | Status.SLNP_REQUESTER_CANCELLED   | Status.SLNP_RESPONDER_AWAIT_PICKING        | Status.SLNP_RESPONDER_UNFILLED  | '6668-8562' | 'NonReturnable7'  | 'Rebo7'    | Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY            | 'supplierCannotSupply'
    }
}