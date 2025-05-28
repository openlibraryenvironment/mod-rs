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

import static groovyx.net.http.ContentTypes.XML //euw, a star import

@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) //Otherwise the port will be random
class ILLBrokerSpec extends TestBase {

    final static String TENANT_ONE_NAME = "BrokerInstOne"
    final static String TENANT_TWO_NAME = "BrokerInstTwo"
    final static String SYMBOL_AUTHORITY = "ISIL"
    final static String SYMBOL_ONE_NAME = "BIO1"
    final static String SYMBOL_TWO_NAME = "BIO2"
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

    Map sendXMLMessage(String url, String message, Map additionalHeaders, long timeout) {
        Map result = [ messageStatus: EventISO18626IncomingAbstractService.STATUS_ERROR ]

        HttpBuilder http_client = ApacheHttpBuilder.configure({
            client.clientCustomizer({  HttpClientBuilder builder ->
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

    private String waitForRequestState(String tenant, long timeout, String patron_reference, String required_state) {
        Map params = [
                'max':'100',
                'offset':'0',
                'match':'patronReference',
                'term':patron_reference
        ]
        return waitForRequestStateParams(tenant, timeout, params, required_state)
    }

    private String waitForRequestStateById(String tenant, long timeout, String id, String required_state) {
        Map params = [
                'max':'1',
                'offset':'0',
                'match':'id',
                'term':id
        ]
        return waitForRequestStateParams(tenant, timeout, params, required_state)
    }

    private String waitForRequestStateByHrid(String tenant, long timeout, String hrid, String required_state) {
        Map params = [
                'max':'1',
                'offset':'0',
                'match':'hrid',
                'term':hrid
        ]
        return waitForRequestStateParams(tenant, timeout, params, required_state)
    }

    private String waitForRequestStateParams(String tenant, long timeout, Map params, String required_state) {
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

    private void performActionAndCheckStatus(String performSupActionUrl, String fileName, String tenant, String reqStatus, String supStatus, String requesterTenantId, String responderTenantId, String patronReference){
        String payload = new File("src/integration-test/resources/scenarios/${fileName}").text
        setHeaders(['X-Okapi-Tenant': tenant])
        doPost(performSupActionUrl, payload)
        waitForRequestState(requesterTenantId, 10000, patronReference, reqStatus)
        waitForRequestState(responderTenantId, 10000, patronReference, supStatus)

    }

    void "Attempt to delete any old tenants"(tenantid, name) {
        when:"We post a delete request"
        boolean result = deleteTenant(tenantid, name)

        then:"Any old tenant removed"
        assert(result)

        where:
        tenantid | name
        TENANT_ONE_NAME | TENANT_ONE_NAME
        TENANT_TWO_NAME | TENANT_TWO_NAME
    }

    void "Set up test tenants"(tenantid, name) {
        when:"We post a new tenant request to the OKAPI controller"
        boolean response = setupTenant(tenantid, name)

        then:"The response is correct"
        assert(response)

        where:
        tenantid | name
        TENANT_ONE_NAME | TENANT_ONE_NAME
        TENANT_TWO_NAME | TENANT_TWO_NAME
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
        TENANT_TWO_NAME    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'disabled',  'request_id_prefix' : 'TENANTTWO', (SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS) : "${BROKER_BASE_URL}/iso18626", (SettingsData.SETTING_DEFAULT_PEER_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}", (SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) : "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}" ] | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']
    }

    void "Create broker peers"(String peer_symbol, String tenant) {
        when:"We clean and update peer ${peer_symbol}"

        Map peerData = doGet("${BROKER_BASE_URL}/peers?cql=symbol+any+${peer_symbol}")
        if (peerData.items?.size == 1) {
            doDelete("${BROKER_BASE_URL}/peers/${peerData.items[0].ID}")
        }

        String body = "{\"HttpHeaders\":{\"x-okapi-tenant\":\"${tenant}\"},\"Name\":\"${peer_symbol}\",\"RefreshPolicy\":\"never\",\"Symbols\":[\"${peer_symbol}\"],\"Url\":\"http://host.docker.internal:${serverPort}/rs/externalApi/iso18626\",\"Vendor\":\"illmock\",\"CustomData\":{}}"
        doPost("${BROKER_BASE_URL}/peers", body)
        then:"Peer is saved"
        1==1

        where:
        peer_symbol | tenant
        "${SYMBOL_AUTHORITY}:${SYMBOL_ONE_NAME}" | TENANT_ONE_NAME
        "${SYMBOL_AUTHORITY}:${SYMBOL_TWO_NAME}" | TENANT_TWO_NAME
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
        performActionAndCheckStatus(performSupActionUrl, "nrSupplierAnswerYes.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, requesterTenantId, supplierTenantId, patronReference)

        // Mark pullslip
        performActionAndCheckStatus(performSupActionUrl, "nrSupplierPrintPullSlip.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_PICKING, requesterTenantId, supplierTenantId, patronReference)

        // Check in
        performActionAndCheckStatus(performSupActionUrl, "supplierCheckInToReshare.json", supplierTenantId, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Status.RESPONDER_AWAIT_SHIP, requesterTenantId, supplierTenantId, patronReference)

        // Mark shipped
        performActionAndCheckStatus(performSupActionUrl, "supplierMarkShipped.json", supplierTenantId, Status.PATRON_REQUEST_SHIPPED, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Mark received
        performActionAndCheckStatus(performActionUrl, "requesterReceived.json", requesterTenantId, Status.PATRON_REQUEST_CHECKED_IN, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Patron returned
        performActionAndCheckStatus(performActionUrl, "patronReturnedItem.json", requesterTenantId, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Status.RESPONDER_ITEM_SHIPPED, requesterTenantId, supplierTenantId, patronReference)

        // Return shipped
        performActionAndCheckStatus(performActionUrl, "shippedReturn.json", requesterTenantId, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Status.RESPONDER_ITEM_RETURNED, requesterTenantId, supplierTenantId, patronReference)

        // Mark shipped
        performActionAndCheckStatus(performSupActionUrl, "supplierCheckOutOfReshare.json", supplierTenantId, Status.PATRON_REQUEST_REQUEST_COMPLETE, Status.RESPONDER_COMPLETE, requesterTenantId, supplierTenantId, patronReference)

        then:
        assert(true)
    }

    @Ignore
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

        then:
        
        assert(true);
    }


}
