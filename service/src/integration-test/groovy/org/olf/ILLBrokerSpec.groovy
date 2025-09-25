package org.olf


import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import groovyx.net.http.ApacheHttpBuilder
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClientBuilder
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Ignore
import spock.lang.Stepwise

import java.time.ZonedDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

import static groovyx.net.http.ContentTypes.XML

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) //Otherwise the port will be random
class ILLBrokerSpec extends TestBase {

    final static String TENANT_ONE_NAME = "BrokerInstOne"
    final static String TENANT_TWO_NAME = "BrokerInstTwo"
    final static String TENANT_THREE_NAME = "BrokerInstThree"
    final static String SYMBOL_AUTHORITY = "ISIL"
    final static String SYMBOL_ONE_NAME = "BIO1"
    final static String SYMBOL_TWO_NAME = "BIO2"
    final static String SYMBOL_THREE_NAME = "BIO3"
    final static String BROKER_BASE_URL = "http://localhost:19082"

    def setupSpecWithSpring() {
        super.setupSpecWithSpring()
        log.debug("setup spec completed")
    }

    //Do we need to make sure the base version doesn't happen?
    def setupSpec() {
        log.debug("setupSpec called")
    }

    def setup() {
        if (testctx.niaInitialized == null) {
            testctx.niaInitialized = true
        }
    }

    def cleanup() {
        log.debug("Cleanup called")
    }

    public String getBaseUrl() {
        //For some reason the base url keeps getting 'null' inserted into it
        return super.getBaseUrl()?.replace("null", "")
    }




    private Map performActionFromFileAndCheckStatus(String performSupActionUrl, String actionFileName, String tenant,
            String reqStatus, String supStatus, String requesterTenantId, String responderTenantId,
            String patronReference) {
        String payload = new File("src/integration-test/resources/scenarios/${actionFileName}").text
        return performActionAndCheckStatus(performSupActionUrl, payload, tenant, reqStatus, supStatus,
                requesterTenantId, responderTenantId, patronReference);
    }


    private Map performActionAndCheckStatus(String performSupActionUrl, Object actionPayload, String tenant,
            String reqStatus, String supStatus, String requesterTenantId, String responderTenantId,
            String patronReference) {
        setHeaders(['X-Okapi-Tenant': tenant])
        doPost(performSupActionUrl, actionPayload)
        String requesterResult = waitForRequestState(requesterTenantId, 10000, patronReference, reqStatus);
        String responderResult = waitForRequestState(responderTenantId, 10000, patronReference, supStatus);
        return [responderResult : responderResult, requesterResult : requesterResult];
    }

    private Map getPatronRequestData(String requestId, String tenantId) {
        setHeaders(['X-Okapi-Tenant' : tenantId]);
        String patronRequestUrl = "${baseUrl}/rs/patronrequests/${requestId}";
        Map data = doGet(patronRequestUrl) as Map;
        return data;
    }

    void "Attempt to delete any old tenants"(tenantid, name) {
        when:"We post a delete request"
        boolean result = deleteTenant(tenantid, name)

        then:"Any old tenant removed"
        assert(result)

        where:
        tenantid | name
        TENANT_ONE_NAME   | TENANT_ONE_NAME
        TENANT_TWO_NAME   | TENANT_TWO_NAME
        TENANT_THREE_NAME | TENANT_THREE_NAME
    }

    void "Set up test tenants"(tenantid, name) {
        when:"We post a new tenant request to the OKAPI controller"
        boolean response = setupTenant(tenantid, name)

        then:"The response is correct"
        assert(response)

        where:
        tenantid | name
        TENANT_ONE_NAME   | TENANT_ONE_NAME
        TENANT_TWO_NAME   | TENANT_TWO_NAME
        TENANT_THREE_NAME | TENANT_THREE_NAME
    }

    void "Configure Tenants for lending without rota or directory"(String tenant_id, Map changes_needed, Map changes_needed_hidden) {
        when:"We fetch the existing settings for ${tenant_id}"

        changeSettings(tenant_id, changes_needed)
        changeSettings(tenant_id, changes_needed_hidden, true)

        then:"Tenant is configured"
        1==1

        where:
        tenant_id          | changes_needed               | changes_needed_hidden
        TENANT_ONE_NAME    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'disabled',  'auto_rerequest':'yes',  'request_id_prefix' : 'TENANTONE', (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${BROKER_BASE_URL}/iso18626", (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}", (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}" ] | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']
        TENANT_TWO_NAME    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'on', 'routing_adapter':'disabled',  'request_id_prefix' : 'TENANTTWO', (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${BROKER_BASE_URL}/iso18626", (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}", (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}" ] | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']
        TENANT_THREE_NAME  | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'disabled',  'request_id_prefix' : 'TENANTHREE', (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${BROKER_BASE_URL}/iso18626", (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}", (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_THREE_NAME}" ] | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']

    }

    void "Create broker peers"(String peer_symbol, String tenant) {
        when:"We clean and update peer ${peer_symbol}"

        Map peerData = doGet("${BROKER_BASE_URL}/peers?cql=symbol+any+${peer_symbol}") as Map
        if (peerData.items?.size == 1) {
            doDelete("${BROKER_BASE_URL}/peers/${peerData.items[0].ID}")
        }

        String body = "{\"HttpHeaders\":{\"x-okapi-tenant\":\"${tenant}\"},\"Name\":\"${peer_symbol}\",\"RefreshPolicy\":\"never\",\"Symbols\":[\"${peer_symbol}\"],\"Url\":\"http://host.docker.internal:${serverPort}/rs/externalApi/iso18626\",\"Vendor\":\"illmock\",\"CustomData\":{},\"BrokerMode\":\"transparent\"}"
        doPost("${BROKER_BASE_URL}/peers", body)
        then:"Peer is saved"
        1==1

        where:
        peer_symbol                                | tenant
        "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}"   | TENANT_ONE_NAME
        "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}"   | TENANT_TWO_NAME
        "${SYMBOL_AUTHORITY}:${SYMBOL_THREE_NAME}" | TENANT_THREE_NAME
    }


    void "Test willsupply/loaned interaction with broker"() {
        String requesterTenantId = TENANT_ONE_NAME
        String supplierTenantId = TENANT_TWO_NAME
        String patronIdentifier = "Broker-test-1-" + System.currentTimeMillis()
        String patronReference = "ref-${patronIdentifier}"
        String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::WILLSUPPLY_LOANED" //test transmission to supplierUniqueRecordId

        when: "We create a request"
        Map request = [
                patronReference         : patronReference,
                title                   : "Integration testing with the broker",
                author                  : "Kerr, Bro",
                patronIdentifier        : patronIdentifier,
                isRequester             : true,
                systemInstanceIdentifier: systemInstanceIdentifier,
        ]

        setHeaders(['X-Okapi-Tenant': requesterTenantId])
        doPost("${baseUrl}/rs/patronrequests".toString(), request)

        String requestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)
        String supReqId = waitForRequestState(supplierTenantId, 10000, patronReference, Status.RESPONDER_IDLE)

        String performSupActionUrl = "${baseUrl}/rs/patronrequests/${supReqId}/performAction"
        String performActionUrl = "${baseUrl}/rs/patronrequests/${requestId}/performAction"
        // Respond yes
        performActionFromFileAndCheckStatus(performSupActionUrl, "nrSupplierAnswerYes.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, requesterTenantId, supplierTenantId, patronReference)

        // Mark pullslip
        performActionFromFileAndCheckStatus(performSupActionUrl, "nrSupplierPrintPullSlip.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_PICKING, requesterTenantId, supplierTenantId, patronReference)

        // Check in
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierCheckInToReshare.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_SHIP, requesterTenantId, supplierTenantId, patronReference)

        // Mark shipped
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierMarkShipped.json", supplierTenantId, Status.PATRON_REQUEST_SHIPPED, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Mark received
        performActionFromFileAndCheckStatus(performActionUrl, "requesterReceived.json", requesterTenantId, Status.PATRON_REQUEST_CHECKED_IN, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Patron returned
        performActionFromFileAndCheckStatus(performActionUrl, "patronReturnedItem.json", requesterTenantId, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Return shipped
        performActionFromFileAndCheckStatus(performActionUrl, "shippedReturn.json", requesterTenantId, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Status.RESPONDER_ITEM_RETURNED, requesterTenantId, supplierTenantId, patronReference)

        // Mark shipped
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierCheckOutOfReshare.json", supplierTenantId, Status.PATRON_REQUEST_REQUEST_COMPLETE, Status.RESPONDER_COMPLETE, requesterTenantId, supplierTenantId, patronReference)

        then:
        assert(true)
    }


    void "Test local supplier with broker" () {
        String requesterTenantId = TENANT_ONE_NAME;
        String responderTenantId = TENANT_TWO_NAME;
        String patronIdentifier = "Broker-test-2-" + System.currentTimeMillis();
        String patronReference = "ref-${patronIdentifier}";
        String systemInstanceIdentifier = "return-ISIL:${SYMBOL_ONE_NAME}::send_this_back;return-ISIL:${SYMBOL_TWO_NAME}::send_this_back2"; // we want to test local review
        String localSymbolsString = "ISIL:${SYMBOL_ONE_NAME}";
        changeSettings(requesterTenantId, [ "local_symbols" : localSymbolsString], false);

        when: "Create the request"

        Map request = [
                patronReference             : patronReference,
                title                       : "Local review state with broker test",
                author                      : "Bach, Kohm",
                patronIdentifier            : patronIdentifier,
                isRequester                 : true,
                systemInstanceIdentifier    : systemInstanceIdentifier
        ];

        setHeaders(['X-Okapi-Tenant': requesterTenantId]);
        def response = doPost("${baseUrl}/rs/patronrequests".toString(), request);
        String requestId = response?.id
        String requesterPerformActionUrl = "${baseUrl}/rs/patronrequests/${requestId}/performAction"

        waitForRequestStateById(requesterTenantId, 10000, requestId, Status.PATRON_REQUEST_LOCAL_REVIEW)

        String payload = new File("src/integration-test/resources/scenarios/requesterLoSupCannotSupply.json").text
        setHeaders(['X-Okapi-Tenant': requesterTenantId])
        doPost(requesterPerformActionUrl, payload)
        waitForRequestStateById(requesterTenantId, 10000, requestId, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)

        def responderId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_IDLE);
        String responderPerformActionUrl = "${baseUrl}/rs/patronrequests/${responderId}/performAction"

        String payload2 = new File("src/integration-test/resources/scenarios/supplierCannotSupply.json").text;
        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        doPost(responderPerformActionUrl, payload2);

        waitForRequestStateById(requesterTenantId, 10000, requestId, Status.PATRON_REQUEST_END_OF_ROTA);

        changeSettings(requesterTenantId, [ "local_symbols" : ""], false); //reset setting

        then:
        assert(true);
    }

    void "Test date loan period default and override"(
            String loanPeriodSetting,
            Integer loanOverrideDays
    ) {
        String requesterTenantId = TENANT_ONE_NAME
        String supplierTenantId = TENANT_TWO_NAME
        String patronIdentifier = "Broker-test-1-" + System.currentTimeMillis()
        String patronReference = "ref-${patronIdentifier}"
        String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::WILLSUPPLY_LOANED" //test transmission to supplierUniqueRecordId

        String overrideDateString = null;

        if (loanOverrideDays) {
            ZonedDateTime zdt = ZonedDateTime.now(ZoneOffset.UTC).plusDays(loanOverrideDays);
            overrideDateString = zdt.truncatedTo(ChronoUnit.SECONDS).toString();
        }

        when: "We create a request"
        changeSettings(supplierTenantId, [ (SettingsData.SETTING_DEFAULT_LOAN_PERIOD) : loanPeriodSetting ]);
        Map request = [
                patronReference         : patronReference,
                title                   : "Integration testing with the broker",
                author                  : "Kerr, Bro",
                patronIdentifier        : patronIdentifier,
                isRequester             : true,
                systemInstanceIdentifier: systemInstanceIdentifier,
        ]

        setHeaders(['X-Okapi-Tenant': requesterTenantId])
        doPost("${baseUrl}/rs/patronrequests".toString(), request)

        String requesterRequestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)
        String supplierRequestId = waitForRequestState(supplierTenantId, 10000, patronReference, Status.RESPONDER_IDLE)

        String supplierPerformActionUrl = "${baseUrl}/rs/patronrequests/${supplierRequestId}/performAction"
        String requesterPerformActionUrl = "${baseUrl}/rs/patronrequests/${requesterRequestId}/performAction"

        Map requestIdMap = performActionFromFileAndCheckStatus(supplierPerformActionUrl, "nrSupplierAnswerYes.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(supplierPerformActionUrl, "nrSupplierPrintPullSlip.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_PICKING, requesterTenantId, supplierTenantId, patronReference)

        Map supplierCheckInToReshareRequest = [
            action: "supplierCheckInToReshare",
            actionParams: [
                itemBarcodes: [[itemId: "123"]],
                loanDateOverride: overrideDateString
            ]
        ];
        performActionAndCheckStatus(supplierPerformActionUrl, supplierCheckInToReshareRequest, supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_SHIP, requesterTenantId, supplierTenantId, patronReference)

        Map supplierPRData = getPatronRequestData(supplierRequestId, supplierTenantId);
        Map requesterPRData = getPatronRequestData(requesterRequestId, requesterTenantId);

        performActionFromFileAndCheckStatus(supplierPerformActionUrl, "supplierMarkShipped.json", supplierTenantId, Status.PATRON_REQUEST_SHIPPED, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(requesterPerformActionUrl, "requesterReceived.json", requesterTenantId, Status.PATRON_REQUEST_CHECKED_IN, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(requesterPerformActionUrl, "patronReturnedItem.json", requesterTenantId, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(requesterPerformActionUrl, "shippedReturn.json", requesterTenantId, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Status.RESPONDER_ITEM_RETURNED, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(supplierPerformActionUrl, "supplierCheckOutOfReshare.json", supplierTenantId, Status.PATRON_REQUEST_REQUEST_COMPLETE, Status.RESPONDER_COMPLETE, requesterTenantId, supplierTenantId, patronReference)

        then:

        def patString = /(\d\d\d\d-\d\d-\d\d)T\d\d:\d\d:\d\d/;
        if (overrideDateString) {
            assert ((overrideDateString =~ patString)[0][1] == (supplierPRData.dueDateRS =~ patString)[0][1])
            //Make sure the day parts of the expected dates match...seconds might be slightly different, we dont' care
        }
        assert(true);

        where:
        loanPeriodSetting | loanOverrideDays
        "14"              | 7
        "14"              | null
    }

    void "Test parsing of deliveryaddress from front end"(String deliveryAddress, String expectedDeliveryAddress) {
        String requesterTenantId = TENANT_ONE_NAME
        String supplierTenantId = TENANT_TWO_NAME
        String patronIdentifier = "Broker-test-7-" + System.currentTimeMillis()
        String patronReference = "ref-${patronIdentifier}"
        String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::WILLSUPPLY_LOANED" //test transmission to supplierUniqueRecordId


        when: "We create a request"
        Map request = [
                patronReference         : patronReference,
                title                   : "Testing delivery address",
                author                  : "Eer, Send",
                patronIdentifier        : patronIdentifier,
                isRequester             : true,
                systemInstanceIdentifier: systemInstanceIdentifier,

                deliveryAddress: deliveryAddress
        ]

        setHeaders(['X-Okapi-Tenant': requesterTenantId])
        doPost("${baseUrl}/rs/patronrequests".toString(), request)

        String requestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)
        String supReqId = waitForRequestState(supplierTenantId, 10000, patronReference, Status.RESPONDER_IDLE)

        setHeaders(['X-Okapi-Tenant' : supplierTenantId]);
        def responderData = doGet("${baseUrl}rs/patronrequests/${supReqId}");

        assert(responderData.deliveryAddress == expectedDeliveryAddress)

        String performSupActionUrl = "${baseUrl}/rs/patronrequests/${supReqId}/performAction"
        String performActionUrl = "${baseUrl}/rs/patronrequests/${requestId}/performAction"

        // Finish the other stuff to move the request through
        performActionFromFileAndCheckStatus(performSupActionUrl, "nrSupplierAnswerYes.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(performSupActionUrl, "nrSupplierPrintPullSlip.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_PICKING, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierCheckInToReshare.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_SHIP, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierMarkShipped.json", supplierTenantId, Status.PATRON_REQUEST_SHIPPED, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(performActionUrl, "requesterReceived.json", requesterTenantId, Status.PATRON_REQUEST_CHECKED_IN, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(performActionUrl, "patronReturnedItem.json", requesterTenantId, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(performActionUrl, "shippedReturn.json", requesterTenantId, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Status.RESPONDER_ITEM_RETURNED, requesterTenantId, supplierTenantId, patronReference)
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierCheckOutOfReshare.json", supplierTenantId, Status.PATRON_REQUEST_REQUEST_COMPLETE, Status.RESPONDER_COMPLETE, requesterTenantId, supplierTenantId, patronReference)

        then:
        assert(true)

        where:
        deliveryAddress                                                                                                                                  | expectedDeliveryAddress
        "{\"line1\":\"Imaginary Melbourne Storage Location\",\"locality\":\"Somewhere\",\"postalCode\":\"2600\",\"region\":\"ACT\",\"country\":\"AUS\"}" | "Imaginary Melbourne Storage Location\nSomewhere, ACT, 2600"
        null                                                                                                                                             | null

    }

    void "Test Unfilled Notification should continue to next supplier"() {
        String requesterTenantId = TENANT_ONE_NAME
        String supplierTenantId = TENANT_TWO_NAME
        String patronIdentifier = "Broker-unfilled-test-" + System.currentTimeMillis()
        String patronReference = "ref-${patronIdentifier}"
        String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::WILLSUPPLY;return-ISIL:${SYMBOL_TWO_NAME}::WILLSUPPLY"

        when: "We create a request"
        Map request = [
                patronReference         : patronReference,
                title                   : "Unfilled notification test",
                author                  : "Test, Author",
                patronIdentifier        : patronIdentifier,
                isRequester             : true,
                systemInstanceIdentifier: systemInstanceIdentifier,
        ]

        setHeaders(['X-Okapi-Tenant': requesterTenantId])
        doPost("${baseUrl}/rs/patronrequests".toString(), request)

        String requestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)
        String supReqId = waitForRequestState(supplierTenantId, 10000, patronReference, Status.RESPONDER_IDLE)

        String performSupActionUrl = "${baseUrl}/rs/patronrequests/${supReqId}/performAction"

        // Supplier responds yes (gets to EXPECTS_TO_SUPPLY)
        performActionFromFileAndCheckStatus(performSupActionUrl, "nrSupplierAnswerYes.json",
            supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
            Status.RESPONDER_NEW_AWAIT_PULL_SLIP, requesterTenantId, supplierTenantId, patronReference)

        // Get the requester request data to build the XML message
        Map requesterData = getPatronRequestData(requestId, requesterTenantId)
        String requesterHrid = requesterData.hrid

        // Load and customize the Unfilled Notification XML
        String unfilledXml = new File("src/integration-test/resources/isoMessages/unfilledNotification.xml").text
        unfilledXml = unfilledXml.replace("supplierSymbol_holder", SYMBOL_TWO_NAME)
        unfilledXml = unfilledXml.replace("requesterSymbol_holder", SYMBOL_ONE_NAME)
        unfilledXml = unfilledXml.replace("requestId_holder", requesterHrid)
        unfilledXml = unfilledXml.replace("timestamp_holder", new Date().toInstant().toString())

        // Send the XML message to the requester's ISO18626 endpoint
        Map xmlHeaders = ['X-Okapi-Tenant': requesterTenantId]
        String iso18626Url = "${baseUrl}/rs/externalApi/iso18626"
        sendXMLMessage(iso18626Url, unfilledXml, xmlHeaders, 10000)

        // Should transition back to REQ_REQUEST_SENT_TO_SUPPLIER via UnfilledContinue qualifier
        waitForRequestStateById(requesterTenantId, 10000, requestId, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)

        then:
        assert(true)
    }

    void "Test interactions with supplier symbol that is different from default requester"() {
        String requesterTenantId = TENANT_ONE_NAME
        String supplierTenantId = TENANT_TWO_NAME
        String patronIdentifier = "Broker-test-1-" + System.currentTimeMillis()
        String patronReference = "ref-${patronIdentifier}"
        String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::WILLSUPPLY_LOANED" //test transmission to supplierUniqueRecordId

        String localSymbolsString = "ISIL:${SYMBOL_TWO_NAME}";
        changeSettings(supplierTenantId, [ "local_symbols" : localSymbolsString], false);

        changeSettings(supplierTenantId, [ (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}master" ])

        when: "We create a request"
        Map request = [
                patronReference         : patronReference,
                title                   : "Integration testing with the broker",
                author                  : "Kerr, Bro",
                patronIdentifier        : patronIdentifier,
                isRequester             : true,
                systemInstanceIdentifier: systemInstanceIdentifier,
        ]

        setHeaders(['X-Okapi-Tenant': requesterTenantId])
        doPost("${baseUrl}/rs/patronrequests".toString(), request)

        String requestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)
        String supReqId = waitForRequestState(supplierTenantId, 10000, patronReference, Status.RESPONDER_IDLE)

        String performSupActionUrl = "${baseUrl}/rs/patronrequests/${supReqId}/performAction"
        String performActionUrl = "${baseUrl}/rs/patronrequests/${requestId}/performAction"
        // Respond yes
        performActionFromFileAndCheckStatus(performSupActionUrl, "nrSupplierAnswerYes.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, requesterTenantId, supplierTenantId, patronReference)

        // Mark pullslip
        performActionFromFileAndCheckStatus(performSupActionUrl, "nrSupplierPrintPullSlip.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_PICKING, requesterTenantId, supplierTenantId, patronReference)

        // Check in
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierCheckInToReshare.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_SHIP, requesterTenantId, supplierTenantId, patronReference)

        // Mark shipped
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierMarkShipped.json", supplierTenantId, Status.PATRON_REQUEST_SHIPPED, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Mark received
        performActionFromFileAndCheckStatus(performActionUrl, "requesterReceived.json", requesterTenantId, Status.PATRON_REQUEST_CHECKED_IN, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Patron returned
        performActionFromFileAndCheckStatus(performActionUrl, "patronReturnedItem.json", requesterTenantId, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Return shipped
        performActionFromFileAndCheckStatus(performActionUrl, "shippedReturn.json", requesterTenantId, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Status.RESPONDER_ITEM_RETURNED, requesterTenantId, supplierTenantId, patronReference)

        // Mark shipped
        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierCheckOutOfReshare.json", supplierTenantId, Status.PATRON_REQUEST_REQUEST_COMPLETE, Status.RESPONDER_COMPLETE, requesterTenantId, supplierTenantId, patronReference)

        changeSettings(supplierTenantId, [ (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}" ]) //so future tests aren't messed
        changeSettings(supplierTenantId, [ "local_symbols" : ""], false);

        then:
        assert(true)
    }


    void "Test continuations after requester has rejected loan conditions (manual xml)"(String xmlFileTemplate, String finalState,
            String serviceType, String deliveryMethod) {
        String requesterTenantId = TENANT_ONE_NAME
        String supplierTenantId = TENANT_TWO_NAME
        String patronIdentifier = "Broker-reject-continue-test-" + System.currentTimeMillis()
        String patronReference = "ref-${patronIdentifier}"
        //String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::98754541231;return-ISIL:${SYMBOL_ONE_NAME}::98754541231" //test transmission to supplierUniqueRecordId
        String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::WILLSUPPLY_LOANED" //test transmission to supplierUniqueRecordId

        changeSettings(requesterTenantId, [ (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:DUMMY" ])

        when: "We create a request"
        Map request = [
                patronReference         : patronReference,
                title                   : "Testing cancel continuation with broker",
                author                  : "Bott, Rob",
                patronIdentifier        : patronIdentifier,
                isRequester             : true,
                systemInstanceIdentifier: systemInstanceIdentifier,
                serviceType             : serviceType,
                deliveryMethod          : deliveryMethod
        ]

        setHeaders(['X-Okapi-Tenant': requesterTenantId])
        doPost("${baseUrl}/rs/patronrequests".toString(), request)

        String requestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)
        String supReqId = waitForRequestState(supplierTenantId, 10000, patronReference, Status.RESPONDER_IDLE)

        Map requesterData = getPatronRequestData(requestId, requesterTenantId)
        String requesterHrid = requesterData.hrid

        String performSupActionUrl = "${baseUrl}/rs/patronrequests/${supReqId}/performAction"
        String performActionUrl = "${baseUrl}/rs/patronrequests/${requestId}/performAction"

        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierConditionalSupply.json", supplierTenantId, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, requesterTenantId, supplierTenantId, patronReference);

        performActionFromFileAndCheckStatus(performActionUrl, "requesterRejectConditions.json", requesterTenantId, Status.PATRON_REQUEST_CANCEL_PENDING, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, requesterTenantId, supplierTenantId, patronReference);

        //Send mocked XML
        String mockXMLString = new File("src/integration-test/resources/isoMessages/${xmlFileTemplate}").text;
        mockXMLString = mockXMLString.replace('_SUPPLY_AGENCY_ID_', SYMBOL_TWO_NAME);
        mockXMLString = mockXMLString.replace('_REQUEST_AGENCY_ID_', SYMBOL_ONE_NAME);
        mockXMLString = mockXMLString.replace('_REQUEST_ID_', requesterHrid);
        mockXMLString = mockXMLString.replace('_TIMESTAMP_', new Date().toInstant().toString())

        Map xmlHeaders = ['X-Okapi-Tenant': requesterTenantId];
        String iso18626Url = "${baseUrl}/rs/externalApi/iso18626";
        sendXMLMessage(iso18626Url, mockXMLString, xmlHeaders, 10000);

        waitForRequestStateById(requesterTenantId, 10000, requestId, finalState);


        //performActionFromFileAndCheckStatus(performSupActionUrl, "supplierRespondToCancelYes.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_CANCELLED, requesterTenantId, supplierTenantId, patronReference);


        changeSettings(requesterTenantId, [ (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}" ]) //so future tests aren't messed

        then:
        assert(true);

        where:
        xmlFileTemplate                          | finalState                                        | serviceType | deliveryMethod
        "statusChangeExpectToSupplyTemplate.xml" | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null        | null
        "statusChangeUnfilledTemplate.xml"       | Status.PATRON_REQUEST_END_OF_ROTA                 | null        | null
        "statusChangeExpectToSupplyTemplate.xml" | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | "Copy"      | "URL"
        "statusChangeUnfilledTemplate.xml"       | Status.PATRON_REQUEST_END_OF_ROTA                 | "Copy"      | "URL"
        "cancelResponseReplyNoTemplate.xml"      | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | null        | null


    }

    void "Test continuations after requester has rejected loan conditions"(String serviceType, String deliveryMethod) {
        String requesterTenantId = TENANT_ONE_NAME
        String supplierTenantId = TENANT_TWO_NAME
        String patronIdentifier = "Broker-reject-continue-test-" + System.currentTimeMillis()
        String patronReference = "ref-${patronIdentifier}"
        String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::98754541231;return-ISIL:${SYMBOL_THREE_NAME}::98754541231" //test transmission to supplierUniqueRecordId
        //String systemInstanceIdentifier = "return-ISIL:${SYMBOL_TWO_NAME}::WILLSUPPLY_LOANED" //test transmission to supplierUniqueRecordId

        changeSettings(requesterTenantId, [ (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:DUMMY" ])

        long start_time = System.currentTimeMillis();

        when: "We create a request"
        Map request = [
                patronReference         : patronReference,
                title                   : "Testing cancel continuation with broker",
                author                  : "Bott, Rob",
                patronIdentifier        : patronIdentifier,
                isRequester             : true,
                systemInstanceIdentifier: systemInstanceIdentifier,
                serviceType             : serviceType,
                deliveryMethod          : deliveryMethod
        ]

        setHeaders(['X-Okapi-Tenant': requesterTenantId])
        doPost("${baseUrl}/rs/patronrequests".toString(), request)

        String requestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER)
        String supReqId = waitForRequestState(supplierTenantId, 10000, patronReference, Status.RESPONDER_IDLE)

        Map requesterData = getPatronRequestData(requestId, requesterTenantId)
        String requesterHrid = requesterData.hrid

        String performSupActionUrl = "${baseUrl}/rs/patronrequests/${supReqId}/performAction"
        String performActionUrl = "${baseUrl}/rs/patronrequests/${requestId}/performAction"

        performActionFromFileAndCheckStatus(performSupActionUrl, "supplierConditionalSupply.json", supplierTenantId, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, requesterTenantId, supplierTenantId, patronReference);

        performActionFromFileAndCheckStatus(performActionUrl, "requesterRejectConditions.json", requesterTenantId, Status.PATRON_REQUEST_CANCEL_PENDING, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, requesterTenantId, supplierTenantId, patronReference);

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);

        //performActionFromFileAndCheckStatus(performSupActionUrl, "supplierRespondToCancelYes.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_CANCELLED, requesterTenantId, supplierTenantId, patronReference);

        long finish_time = System.currentTimeMillis()

        long elapsed_time = finish_time - start_time;
        log.debug("Test ran in ${elapsed_time} millis");

        changeSettings(requesterTenantId, [ (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}" ]) //so future tests aren't messed

        then:
        assert(elapsed_time < 10000);

        where:
        serviceType | deliveryMethod
        null        | null
        "Copy"      | "URL"


    }






}
