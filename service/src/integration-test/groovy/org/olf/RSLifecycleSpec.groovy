package org.olf

import com.k_int.web.toolkit.refdata.RefdataValue
import grails.databinding.SimpleMapDataBindingSource
import grails.util.Holders
import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.Duration
import org.grails.datastore.gorm.events.AutoTimestampEventListener
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.rs.constants.Directory
import org.olf.rs.lms.HostLMSActions
import org.olf.rs.statemodel.StateModel
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.web.databinding.GrailsWebDataBinder
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.olf.rs.*
import org.olf.rs.dynamic.DynamicGroovyService
import org.olf.rs.logging.DoNothingHoldingLogDetails
import org.olf.rs.logging.IHoldingLogDetails
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.routing.RankedSupplier
import org.olf.rs.routing.StaticRouterService
import org.olf.rs.settings.ISettings
import org.olf.rs.statemodel.Status
import org.olf.rs.timers.TimerCheckForStaleSupplierRequestsService
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Ignore
import java.text.SimpleDateFormat


@Slf4j
@Integration
@Stepwise
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class RSLifecycleSpec extends TestBase {

    // The scenario details that are maintained between tests
    private static final String SCENARIO_PATRON_REFERENCE = "scenario-patronReference";
    private static final String SCENARIO_REQUESTER_ID = "scenario-requesterId";
    private static final String SCENARIO_RESPONDER_ID = "scenario-responderId";
    private static final String RANDOM_URL = 'https://www.url.com'

    private static String LONG_300_CHAR_TITLE = '123456789A123456789B123456789C123456789D123456789E123456789F123456789G123456789H123456789I123456789J123456789k123456789l123456789m123456789n123456789o123456789p123456789q123456789r123456789s123456789t123456789U123456789V123456789W123456789Y123456789Y12345XXXXX'
    private SimpleDateFormat scenarioDateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS");

    // Warning: You will notice that these directory entries carry and additional customProperty: AdditionalHeaders
    // When okapi fronts the /rs/externalApi/iso18626 endpoint it does so through a root path like
    // _/invoke/tenant/TENANT_ID/rs/externalApi/iso18626 - it then calls the relevant path with the TENANT_ID as a header
    // Because we want our tests to run without an OKAPI, we need to supply the tenant-id that OKAPI normally would and that
    // is the function of the AdditionalHeaders custom property here
    @Shared
    private static List<Map> DIRECTORY_INFO = [
            [ id:'RS-T-D-0001', name: 'RSInstOne', slug:'RS_INST_ONE',     symbols: [[ authority:'ISIL', symbol:'RST1', priority:'a']], type : "Institution",
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
            [ id:'RS-T-D-0002', name: 'RSInstTwo', slug:'RS_INST_TWO',     symbols: [[ authority:'ISIL', symbol:'RST2', priority:'a']], type: "Institution",
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
            [ id:'RS-T-D-0003', name: 'RSInstThree', slug:'RS_INST_THREE', symbols: [[ authority:'ISIL', symbol:'RST3', priority:'a']], type: "Branch" ,
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


  DynamicGroovyService dynamicGroovyService;
  GrailsWebDataBinder grailsWebDataBinder
  HostLMSService hostLMSService
  HostLMSLocationService hostLMSLocationService
  HostLMSShelvingLocationService hostLMSShelvingLocationService
  StaticRouterService staticRouterService
  Z3950Service z3950Service
  SettingsService settingsService;
  AutoTimestampEventListener autoTimestampEventListener;





    // This method is declared in the HttpSpec
    def setupSpecWithSpring() {
        super.setupSpecWithSpring();
    }

    def setupSpec() {
    }

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

    void "Attempt to delete any old tenants"(tenantid, name) {
        when:"We post a delete request"
        boolean result = deleteTenant(tenantid, name);

        then:"Any old tenant removed"
        assert(result);

        where:
        tenantid | name
        'RSInstOne' | 'RSInstOne'
        'RSInstTwo' | 'RSInstTwo'
        'RSInstThree' | 'RSInstThree'
    }

    void "test presence of HOST LMS adapters"(String name, boolean should_be_found) {

        when: "We try to look up ${name} as a host adapter"
        log.debug("Lookup LMS adapter ${name}");
        HostLMSActions actions = hostLMSService.getHostLMSActionsFor(name)
        log.debug("result of lookup : ${actions}");

        then: "We expect that the adapter should ${should_be_found ? 'BE' : 'NOT BE'} found. result was ${actions}."
        if ( should_be_found ) {
            actions != null
        }
        else {
            actions == null
        }

        where:
        name        | should_be_found
        'alma'      | true
        'aleph'     | true
        'ncsu'      | true
        'wms'       | true
        'wms2'      | true
        'default'   | true
        'manual'    | true
        'folio'     | true
        'symphony'  | true
        'sierra'    | true
        'polaris'   | true
        'evergreen' | true
        'tlc'       | true
        'wibble'    | false
    }

    void "Set up test tenants "(tenantid, name) {
        when:"We post a new tenant request to the OKAPI controller"
        boolean response = setupTenant(tenantid, name);

        then:"The response is correct"
        assert(response);

        where:
        tenantid | name
        'RSInstOne' | 'RSInstOne'
        'RSInstTwo' | 'RSInstTwo'
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


    def cleanup() {
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

    // For the given tenant fetch the specified request
    private Map fetchRequest(String tenant, String requestId) {

        setHeaders([ 'X-Okapi-Tenant': tenant ]);
        // https://east-okapi.folio-dev.indexdata.com/rs/patronrequests/{id}
        def response = doGet("${baseUrl}rs/patronrequests/${requestId}")
        return response;
    }

    private String randomCrap(int length) {
        String source = (('A'..'Z') + ('a'..'z')).join();
        Random rand = new Random();
        return (1..length).collect { source[rand.nextInt(source.length())]}.join();
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
        changeSettings(tenant_id, changes_needed_hidden, true)

        then:"Tenant is configured"
        1==1


    where:
      tenant_id      | changes_needed                                                                                                                                                            | changes_needed_hidden
      'RSInstOne'    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST3,SYMBOL:ISIL:RST2', 'auto_rerequest':'yes', 'request_id_prefix' : 'TENANTONE'] | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']
      'RSInstTwo'    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST1,SYMBOL:ISIL:RST3', 'request_id_prefix' : 'TENANTTWO']                          | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']
      'RSInstThree'  | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST1', 'request_id_prefix' : 'TENANTTHREE']                                        | ['requester_returnables_state_model':'PatronRequest', 'responder_returnables_state_model':'Responder', 'requester_non_returnables_state_model':'NonreturnableRequester', 'responder_non_returnables_state_model':'NonreturnableResponder', 'requester_digital_returnables_state_model':'DigitalReturnableRequester', 'state_model_responder_cdl':'CDLResponder']


    }

    void "Validate Static Router"() {

        when:"We call the static router"
        List<RankedSupplier> resolved_rota = null;
        Tenants.withId('RSInstOne_mod_rs'.toLowerCase()) {
            resolved_rota = staticRouterService.findMoreSuppliers([title:'Test'], null)
        }
        log.debug("Static Router resolved to ${resolved_rota}");

        then:"The expecte result is returned"
        resolved_rota.size() == 2;
    }


    void "Test retrieval of local directory entries"(
            String tenantId
    ) {
        when: "We need to get our list of local directories"
        String localSymbolsString = "ISIL:RST1,ISIL:RST2";
        Set localDirectoryEntries = [];
        Tenants.withId(tenantId.toLowerCase() + '_mod_rs', {
            List localSymbols = DirectoryEntryService.resolveSymbolsFromStringList(localSymbolsString);
            localSymbols.each {
                localDirectoryEntries.add(it.owner);
            }
        });

        then:
        localDirectoryEntries.size() == 2;

        where:
        tenantId    | _
        "RSInstOne" | _
    }




    /**
     * Send a test request from RSInstOne(ISIL:RST1) to RSInstThree(ISIL:RST3)
     * This test bypasses the request routing component by providing a pre-established rota
     */
    void "Send request with preset rota"(String tenant_id,
                                         String peer_tenant,
                                         String p_title,
                                         String p_author,
                                         String p_systemInstanceIdentifier,
                                         String p_patron_id,
                                         String p_patron_reference,
                                         String requesting_symbol,
                                         String responder_symbol) {
        when:"post new request"
        log.debug("Create a new request ${tenant_id} ${p_title} ${p_patron_id}");

        // Create a request from OCLC:PPPA TO OCLC:AVL
        def req_json_data = [
                requestingInstitutionSymbol:requesting_symbol,
                title: p_title,
                author: p_author,
                systemInstanceIdentifier: p_systemInstanceIdentifier,
                bibliographicRecordId: p_systemInstanceIdentifier,
                patronReference:p_patron_reference,
                patronIdentifier:p_patron_id,
                isRequester:true,
                rota:[
                        [directoryId:responder_symbol, rotaPosition:"0", 'instanceIdentifier': '001TagFromMarc', 'copyIdentifier':'COPYBarcode from 9xx']
                ],
                tags: [ 'RS-TESTCASE-1' ]
        ]

        setHeaders([
                'X-Okapi-Tenant': tenant_id,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ])

        log.debug("Post to patronrequests: ${req_json_data}");
        def resp = doPost("${baseUrl}/rs/patronrequests".toString(), req_json_data)

        log.debug("CreateReqTest1 -- Response: RESP:${resp} ID:${resp.id}");

        // Stash the ID
        this.testctx.request_data[p_patron_reference] = resp.id

        String peer_request = waitForRequestState(peer_tenant, 10000, p_patron_reference, 'RES_IDLE')
        log.debug("Created new request for with-rota test case 1. REQUESTER ID is : ${this.testctx.request_data[p_patron_reference]}")
        log.debug("                                               RESPONDER ID is : ${peer_request}");


        then:"Check the return value"
        assert this.testctx.request_data[p_patron_reference] != null;
        assert peer_request != null

        where:
        tenant_id   | peer_tenant   | p_title             | p_author         | p_systemInstanceIdentifier | p_patron_id | p_patron_reference        | requesting_symbol | responder_symbol
        'RSInstOne' | 'RSInstThree' | 'Brain of the firm' | 'Beer, Stafford' | '1234-5678-9123-4566'      | '1234-5678' | 'RS-LIFECYCLE-TEST-00001' | 'ISIL:RST1'       | 'ISIL:RST3'
    }

    void "Confirm fullRecord flag expands records in request list"(String tenant_id) {
        when:"Fetch requests"
        setHeaders([ 'X-Okapi-Tenant': tenant_id ]);
        def resp = doGet("${baseUrl}rs/patronrequests",
                [
                        'max':'1',
                        'fullRecord':'true'
                ]);
        then:
        assert resp?.size() == 1 && resp[0].containsKey('rota') && resp[0].containsKey('audit');
        where:
        tenant_id | _
        'RSInstOne' | _
    }

    /**
     * Important note for this test case:: peer_tenant is set to RSInstThree and this works because RSInstOne has a static rota set up
     * so that RSInstThree is the first option for sending a request to. Any changes in the test data will likely break this test. Watch out
     */
    void "Send request using static router"(String tenant_id,
                                            String peer_tenant,
                                            String p_title,
                                            String p_author,
                                            String p_systemInstanceIdentifier,
                                            String p_patron_id,
                                            String p_patron_reference,
                                            String requesting_symbol,
                                            String[] tags) {
        when:"post new request"
        log.debug("Create a new request ${tenant_id} ${tags} ${p_title} ${p_patron_id}");

        // Create a request from OCLC:PPPA TO OCLC:AVL
        def req_json_data = [
                requestingInstitutionSymbol:requesting_symbol,
                title: p_title,
                author: p_author,
                systemInstanceIdentifier: p_systemInstanceIdentifier,
                patronReference:p_patron_reference,
                patronIdentifier:p_patron_id,
                isRequester:true,
                tags: tags
        ]

        setHeaders([
                'X-Okapi-Tenant': tenant_id,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ])
        def resp = doPost("${baseUrl}/rs/patronrequests".toString(), req_json_data)

        log.debug("CreateReqTest2 -- Response: RESP:${resp} ID:${resp.id}");

        // Stash the ID
        this.testctx.request_data[p_patron_reference] = resp.id

        String peer_request = waitForRequestState(peer_tenant, 10000, p_patron_reference, 'RES_IDLE')
        log.debug("Created new request for with-rota test case 1. REQUESTER ID is : ${this.testctx.request_data[p_patron_reference]}")
        log.debug("                                               RESPONDER ID is : ${peer_request}");


        then:"Check the return value"
        assert this.testctx.request_data[p_patron_reference] != null;
        assert peer_request != null

        where:
        tenant_id   | peer_tenant   | p_title               | p_author         | p_systemInstanceIdentifier | p_patron_id | p_patron_reference        | requesting_symbol | tags
        'RSInstOne' | 'RSInstThree' | 'Platform For Change' | 'Beer, Stafford' | '1234-5678-9123-4577'      | '1234-5679' | 'RS-LIFECYCLE-TEST-00002' | 'ISIL:RST1'       | [ 'RS-TESTCASE-2' ]
        'RSInstOne' | 'RSInstThree' | LONG_300_CHAR_TITLE   | 'Author, Some'   | '1234-5678-9123-4579'      | '1234-567a' | 'RS-LIFECYCLE-TEST-00003' | 'ISIL:RST1'       | [ 'RS-TESTCASE-3' ]
    }

    // For RSInstThree tenant should return the sample data loaded
    void "test API for retrieving shelving locations for #tenant_id"() {

        when:"We post to the shelvingLocations endpoint for tenant"
        setHeaders([
                'X-Okapi-Tenant': tenant_id
        ])
        def resp = doGet("${baseUrl}rs/shelvingLocations".toString());

        then:"Got results"
        resp != null;
        log.debug("Got get shelving locations response: ${resp}");

        where:
        tenant_id | _
        'RSInstThree' | _
    }

    void "test API for creating shelving locations for #tenant_id"() {
        when:"We post to the shelvingLocations endpoint for tenant"
        setHeaders([
                'X-Okapi-Tenant': tenant_id
        ])
        def resp = doPost("${baseUrl}rs/shelvingLocations".toString(),
                [
                        code:'stacks',
                        name:'stacks',
                        supplyPreference:1
                ])
        then:"Created"
        resp != null;
        log.debug("Got create shelving locations response: ${resp}");
        where:
        tenant_id | _
        'RSInstOne' | _
    }

    void "test API for creating patron profiles for #tenant_id"() {
        when:"We post to the hostLMSPatronProfiles endpoint for tenant"
        setHeaders([
                'X-Okapi-Tenant': tenant_id
        ])
        def resp = doPost("${baseUrl}rs/hostLMSPatronProfiles".toString(),
                [
                        code:'staff',
                        name:'staff'
                ])
        then:"Created"
        resp != null;
        log.debug("Got create hostLMSPatronProfiles response: ${resp}");
        where:
        tenant_id | _
        'RSInstOne' | _
    }

    // For RSInstThree tenant should return the sample data loaded
    void "test API for retrieving patron profiles for #tenant_id"() {

        when:"We GET to the hostLMSPatronProfiles endpoint for tenant"
        setHeaders([
                'X-Okapi-Tenant': tenant_id
        ])
        def resp = doGet("${baseUrl}rs/hostLMSPatronProfiles".toString());

        then:"Got results"
        resp != null;
        log.debug("Got get hostLMSPatronProfiles response: ${resp}");

        where:
        tenant_id | _
        'RSInstThree' | _
    }
    void "test determineBestLocation for WMS LMS adapters" () {
        when: "We mock lookupViaConnector and run determineBestLocation for WMS"
        def result = [:];
        def xml = null;
        Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {
            def actions = hostLMSService.getHostLMSActionsFor(lms);
            xml = new XmlSlurper().parseText(new File("src/test/resources/zresponsexml/${zResponseFile}").text);
            actions.metaClass.lookupViaConnector { String query, ISettings settings, IHoldingLogDetails holdingLogDetails ->
                actions.extractAvailableItemsFrom(xml, null, new DoNothingHoldingLogDetails())
            };
            def pr = new PatronRequest(supplierUniqueRecordId: '1010578748');
            result['viaOCLC'] = actions.determineBestLocation(settingsService, pr, new DoNothingHoldingLogDetails());
            result['location'] = HostLMSLocation.findByCode(location);
            result['shelvingLocation'] = HostLMSShelvingLocation.findByCode(shelvingLocation);
        }

        then: 'Confirm location and shelving location'
        xml != null;
        result?.viaOCLC?.location == location;
        result?.location?.code == location;
        result?.shelvingLocation?.code == shelvingLocation;

        where:
        tenant_id | lms | zResponseFile | location | shelvingLocation | _
        'RSInstThree' | 'wms' | 'wms-lfmm.xml' | 'LFMM' | 'General Collection' | _
        'RSInstThree' | 'wms2' | 'wms-lfmm.xml' | 'LFMM' | 'General Collection' | _
    }

    void "test determineBestLocation for LMS adapters"() {
        when:"We mock z39 and run determineBestLocation"
        z3950Service.metaClass.query = { ISettings settings, String query, int max, String schema, IHoldingLogDetails holdingLogDetails -> new XmlSlurper().parseText(new File("src/test/resources/zresponsexml/${zResponseFile}").text) };
        def result = [:];
        Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {
            // perhaps generalise this to set preferences per test-case, for now we're just using it to see a temporaryLocation respected
            def nonlendingVoyager = hostLMSLocationService.ensureActive('BASS, Lower Level, 24-Hour Reserve','');
            nonlendingVoyager.setSupplyPreference(-1);

            def nonlendingFolioShelvingLocation = hostLMSShelvingLocationService.ensureExists('Olin Reserve','', -1);

            def actions = hostLMSService.getHostLMSActionsFor(lms);
            def pr = new PatronRequest(supplierUniqueRecordId: '123');
            result['viaId'] = actions.determineBestLocation(settingsService, pr, new DoNothingHoldingLogDetails());
            pr = new PatronRequest(isbn: '123');
            result['viaPrefix'] = actions.determineBestLocation(settingsService, pr, new DoNothingHoldingLogDetails());
            result['location'] = HostLMSLocation.findByCode(location);
            result['shelvingLocation'] = HostLMSShelvingLocation.findByCode(shelvingLocation);
        }

        then:"Confirm location and shelving location were created and properly returned"
        result?.viaId?.location == location;
        result?.viaPrefix?.location == location;
        result?.location?.code == location;
        result?.viaId?.shelvingLocation == shelvingLocation;
        result?.viaPrefix?.shelvingLocation == shelvingLocation;
        result?.shelvingLocation?.code == shelvingLocation;

        where:
        tenant_id | lms | zResponseFile | location | shelvingLocation | _
        'RSInstThree' | 'alma'      | 'alma-princeton.xml'                                          | 'Firestone Library'           | 'stacks: Firestone Library'   | _
        'RSInstThree' | 'alma'      | 'alma-princeton-notfound.xml'                                 | null                          | null                          | _
        'RSInstThree' | 'alma'      | 'alma-dickinson-multiple.xml'                                 | null                          | null                          | _
        'RSInstThree' | 'horizon'   | 'horizon-jhu.xml'                                             | 'Eisenhower'                  | null                          | _
        'RSInstThree' | 'symphony'  | 'symphony-stanford.xml'                                       | 'SAL3'                        | 'STACKS'                      | _
        'RSInstThree' | 'voyager'   | 'voyager-temp.xml'                                            | null                          | null                          | _
        'RSInstThree' | 'folio'     | 'folio-not-requestable.xml'                                   | null                          | null                          | _
        'RSInstThree' | 'polaris'   | 'polaris-with-in-item-status.xml'                             | 'Juvenile nonfiction shelves' | 'Main Library'                | _
        'RSInstThree' | 'polaris'   | 'polaris-with-out-item-status.xml'                            | null                          | null                          | _
        'RSInstThree' | 'polaris'   | 'polaris-with-held-item-status.xml'                           | null                          | null                          | _
        'RSInstThree' | 'evergreen' | 'evergreen-east-central-with-checked-out-item-status.xml'     | null                          | null                          | _
        'RSInstThree' | 'evergreen' | 'evergreen-east-central-with-in-transit-item-status.xml'      | null                          | null                          | _
        'RSInstThree' | 'evergreen' | 'evergreen-lake-agassiz-with-available-item-status.xml'       | 'HAWLEY'                      | 'Main'                        | _
        'RSInstThree' | 'evergreen' | 'evergreen-north-west-with-available-item-status.xml'         | 'ROSEAU'                      | 'Main'                        | _
        'RSInstThree' | 'evergreen' | 'evergreen-traverse-de-sioux-with-available-item-status.xml'  | 'AM'                          | 'Children\'s Literature Area' | _
        'RSInstThree' | 'tlc'       | 'tlc-eastern.xml'                                             | 'Warner Library'              | 'WARNER STACKS'               | _
    }


    void "Test local directory entries endpoint"(
            String tenantId,
            String type,
            int expectedResults
    ) {
        given:
        setHeaders([
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ]);
        String localSymbolsString = "ISIL:RST1,ISIL:RST3";
        changeSettings(tenantId, [ "local_symbols" : localSymbolsString], false);
        when:
        def resp;
        if (type) {
            resp = doGet("$baseUrl/rs/localEntries?type=$type".toString());
        } else {
            resp = doGet("$baseUrl/rs/localEntries".toString());
        }
        then:
        assert(resp != null);
        assert(resp.size() == expectedResults);
        //assert(resp["localSymbols"] == localSymbolsString);
        cleanup:
        changeSettings(tenantId, ["local_symbols" : ""], false);

        where:
        tenantId | type | expectedResults
        'RSInstOne' | null | 2
        //'RSInstOne' | 'Institution' | 1

    }


    /**
     * Important note for the scenario test case, as we are relying on the routing and directory entries that have been setup earlier
     * so if the scenario test is moved out we will also need to setup the directories and settings in that spec file as well
     */
    private void createScenarioRequest(String requesterTenantId, int scenarioNo, String patronIdentifier = null,
                                       String deliveryMethod = null, String serviceType = null) {
        // Create the request based on the scenario
        Map request = [
                patronReference: 'Scenario-' + scenarioNo + '-' + scenarioDateFormatter.format(new Date()),
                title: 'Testing-Scenario-' + scenarioNo,
                author: 'Author-Scenario-' + scenarioNo,
                requestingInstitutionSymbol: 'ISIL:RST1',
                systemInstanceIdentifier: '123-Scenario-' + scenarioNo,
                patronIdentifier: ((patronIdentifier == null) ? '987-Scenario-' + scenarioNo : patronIdentifier),
                isRequester: true
        ];
        serviceType && (request.serviceType = serviceType);
        deliveryMethod && (request.deliveryMethod = deliveryMethod)
        deliveryMethod && (request.pickupURL = RANDOM_URL)

        log.debug("Create a new request for ${requesterTenantId}, patronReference: ${request.patronReference}, title: ${request.title}");

        setHeaders([ 'X-Okapi-Tenant': requesterTenantId ]);
        def requestResponse = doPost("${baseUrl}/rs/patronrequests".toString(), request);

        log.debug("${request.title} -- Response: Response: ${requestResponse} Id: ${requestResponse.id}");

        // Stash the id and patron reference
        this.testctx.request_data[SCENARIO_REQUESTER_ID] = requestResponse.id;
        this.testctx.request_data[SCENARIO_PATRON_REFERENCE] = requestResponse.patronReference;
    }

    private String performScenarioAction(
            String requesterTenantId,
            String responderTenantId,
            boolean isRequesterAction,
            String actionFile
    ) {
        String actionRequestId = this.testctx.request_data[SCENARIO_REQUESTER_ID]
        String actionTenant = requesterTenantId;
        String peerTenant = responderTenantId;
        if (!isRequesterAction) {
            // It is the responder performing the action
            actionRequestId = this.testctx.request_data[SCENARIO_RESPONDER_ID];
            actionTenant = responderTenantId;
            peerTenant = requesterTenantId;
        }

        String jsonAction = new File("src/integration-test/resources/scenarios/${actionFile}").text;
        log.debug("Action json: ${jsonAction}");
        setHeaders([ 'X-Okapi-Tenant': actionTenant ]);

        if (actionRequestId == null) {
            throw new Exception("Unable to continue, no request ID has been populated");
        }
        String actionUrl = "${baseUrl}/rs/patronrequests/${actionRequestId}/performAction".toString();
        log.debug("Posting to action url at $actionUrl");
        // Execute the action
        def actionResponse = doPost(actionUrl, jsonAction);
        return(actionResponse.toString());
    }

    private String doScenarioAction(
            String requesterTenantId,
            String responderTenantId,
            int scenario,
            boolean isRequesterAction,
            String actionFile,
            String requesterStatus,
            String responderStatus,
            String newResponderTenant,
            String newResponderStatus,
            String patronIdentifier = null,
            String serviceType = null,
            String deliveryMethod = null
    ) {
        String actionResponse = null;

        try {
            log.debug("Performing action for scenario " + scenario + " using file " + actionFile + ", expected requester status " + requesterStatus + ", expected responder status " + responderStatus);
            // Are we creating a fresh request
            if (responderTenantId == null) {
                // So we need to create  new request
                createScenarioRequest(requesterTenantId, scenario, patronIdentifier, deliveryMethod, serviceType);
            } else {
                // We need to perform an action
                actionResponse = performScenarioAction(requesterTenantId, responderTenantId, isRequesterAction, actionFile);
            }

            // Wait for this side of the request to move to the appropriate Action
            waitForRequestState(isRequesterAction ? requesterTenantId : responderTenantId, 10000, this.testctx.request_data[SCENARIO_PATRON_REFERENCE], isRequesterAction ? requesterStatus : responderStatus);

            // Wait for the other side to change state if it is not a new request
            if (actionFile != null) {
                waitForRequestState(isRequesterAction ? responderTenantId : requesterTenantId, 10000, this.testctx.request_data[SCENARIO_PATRON_REFERENCE], isRequesterAction ? responderStatus : requesterStatus);
            }

            // Are we moving onto a new responder
            if (newResponderTenant != null) {
                // Wait for the status and get the responder id
                this.testctx.request_data[SCENARIO_RESPONDER_ID] = waitForRequestState(newResponderTenant, 10000, this.testctx.request_data[SCENARIO_PATRON_REFERENCE], newResponderStatus);
            }
        } catch(Exception e) {
            log.error("Exception Performing action for scenario " + scenario + " using file " + actionFile + ", expected requester status " + requesterStatus + ", expected responder status " + responderStatus, e);
            throw(e);
        }
        return(actionResponse);
    }

    /**
     * This test case is actually being executed multiple times as it works through the scenarios, so it could actually take a long time to run
     */
    void "test scenarios"(
            String requesterTenantId,
            String responderTenantId,
            int scenario,
            boolean isRequesterAction,
            String actionFile,
            String requesterStatus,
            String responderStatus,
            String newResponderTenant,
            String newResponderStatus,
            String deliveryMethod,
            String serviceType,
            String expectedActionResponse,
            Closure requestClosure
    ) {
        when:"Progress the request"

        String actionResponse = doScenarioAction(
                requesterTenantId,
                responderTenantId,
                scenario,
                isRequesterAction,
                actionFile,
                requesterStatus,
                responderStatus,
                newResponderTenant,
                newResponderStatus,
                null,
                serviceType,
                deliveryMethod
        );

        log.debug("Scenario: ${scenario}, Responder id: ${this.testctx.request_data[SCENARIO_RESPONDER_ID]}, action file: ${actionFile}");
        log.debug("Expected Action response: ${expectedActionResponse}, action response: ${actionResponse}");

        then:"Check the response value"
        assert this.testctx.request_data[SCENARIO_REQUESTER_ID] != null;
        assert this.testctx.request_data[SCENARIO_RESPONDER_ID] != null;
        assert this.testctx.request_data[SCENARIO_PATRON_REFERENCE] != null;
        if (expectedActionResponse != null) {
            assert expectedActionResponse == actionResponse;
        }
        if (requestClosure) {
            def requesterRequest;
            def responderRequest;
            Tenants.withId(requesterTenantId.toLowerCase()+'_mod_rs') {
                requesterRequest = PatronRequest.findById(this.testctx.request_data[SCENARIO_REQUESTER_ID]);
            };
            Tenants.withId(responderTenantId.toLowerCase()+'_mod_rs') {
                responderRequest = PatronRequest.findById(this.testctx.request_data[SCENARIO_RESPONDER_ID]);
            }
            assert requestClosure(requesterRequest, responderRequest);
        }

        where:
        requesterTenantId | responderTenantId | scenario | isRequesterAction | actionFile                          | requesterStatus                                   | responderStatus                             | newResponderTenant | newResponderStatus    | deliveryMethod | serviceType | expectedActionResponse | requestClosure
        "RSInstOne"       | null              | 0        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null           | null        | null                   | null
        "RSInstOne"       | "RSInstThree"     | 0        | false             | "supplierAnswerYes.json"            | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "URL"          | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 0        | false             | "supplierPrintPullSlip.json"        | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_AWAIT_PICKING              | null               | null                  | "URL"          | null        | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 0        | false             | "supplierCheckInToReshare.json"     | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_AWAIT_SHIP                 | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 0        | false             | "supplierMarkShipped.json"          | Status.PATRON_REQUEST_SHIPPED                     | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 0        | true              | "requesterReceived.json"            | Status.PATRON_REQUEST_CHECKED_IN                  | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 0        | true              | "patronReturnedItem.json"           | Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING    | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null           | null        | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 0        | true              | "shippedReturn.json"                | Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER         | Status.RESPONDER_ITEM_RETURNED              | null               | null                  | null           | null        | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 0        | false             | "supplierCheckOutOfReshare.json"    | Status.PATRON_REQUEST_REQUEST_COMPLETE            | Status.RESPONDER_COMPLETE                   | null               | null                  | null           | null        | "{status=true}"        | null
        "RSInstOne"       | null              | 1        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null           | null        | null                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierConditionalSupplyWithCost.json"    | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | null           | null        | "{}"                   | ({ r, s -> r.conditions[0].cost == 42.54 && r.conditions[0].costCurrency.value == "cad" && s.conditions[0].cost == 42.54 && s.conditions[0].costCurrency.value == "cad" })
        "RSInstOne"       | "RSInstThree"     | 1        | true              | "requesterAgreeConditions.json"     | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | true              | "requesterCancel.json"              | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierRespondToCancelNo.json"    | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierAddCondition.json"         | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierMarkConditionsAgreed.json" | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierPrintPullSlip.json"        | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_AWAIT_PICKING              | null               | null                  | null           | null        | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierCheckInToReshare.json"     | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_AWAIT_SHIP                 | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "localNote.json"                    | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_AWAIT_SHIP                 | null               | null                  | null           | null        | "{}"                   | ({ r, s -> s.localNote == 'Noted!' })
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierMarkShipped.json"          | Status.PATRON_REQUEST_SHIPPED                     | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | true              | "requesterReceived.json"            | Status.PATRON_REQUEST_CHECKED_IN                  | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 1        | true              | "patronReturnedItem.json"           | Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING    | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null           | null        | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 1        | true              | "shippedReturn.json"                | Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER         | Status.RESPONDER_ITEM_RETURNED              | null               | null                  | null           | null        | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierCheckOutOfReshare.json"    | Status.PATRON_REQUEST_REQUEST_COMPLETE            | Status.RESPONDER_COMPLETE                   | null               | null                  | null           | null        | "{status=true}"        | null
        "RSInstOne"       | null              | 2        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null           | null        | null                   | null
        "RSInstOne"       | "RSInstThree"     | 2        | false             | "supplierAnswerYes.json"            | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 2        | false             | "supplierCannotSupply.json"         | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | Status.RESPONDER_UNFILLED                   | "RSInstTwo"        | Status.RESPONDER_IDLE | null           | null        | "{}"                   | null
        "RSInstOne"       | null              | 3        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null           | null        | null                   | null
        "RSInstOne"       | "RSInstThree"     | 3        | false             | "supplierConditionalSupply.json"    | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 3        | true              | "requesterRejectConditions.json"    | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 3        | false             | "supplierRespondToCancelYes.json"   | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | Status.RESPONDER_CANCELLED                  | "RSInstTwo"        | Status.RESPONDER_IDLE | null           | null        | "{}"                   | null
        "RSInstOne"       | null              | 4        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null           | null        | null                   | null
        "RSInstOne"       | "RSInstThree"     | 4        | true              | "requesterCancel.json"              | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 4        | false             | "supplierRespondToCancelYes.json"   | Status.PATRON_REQUEST_CANCELLED                   | Status.RESPONDER_CANCELLED                  | null               | null                  | null           | null        | null                   | null
        "RSInstOne"       | "RSInstThree"     | 4        | true              | "rerequest.json"                    | Status.PATRON_REQUEST_CANCELLED                   | Status.RESPONDER_CANCELLED                  | null               | null                  | null           | null        | "{status=true}"        | null
        "RSInstOne"       | null              | 5        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "URL"          | null        | null                   | null
        "RSInstOne"       | "RSInstThree"     | 5        | false             | "supplierAnswerYes.json"            | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "URL"          | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 5        | false             | "supplierPrintPullSlip.json"        | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_AWAIT_PICKING              | null               | null                  | "URL"          | null        | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 5        | false             | "supplierCheckInToReshare.json"     | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_SEQUESTERED                | null               | null                  | "URL"          | null        | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 5        | false             | "supplierFillDigitalLoan.json"      | Status.REQUESTER_LOANED_DIGITALLY                 | Status.RESPONDER_LOANED_DIGITALLY           | null               | null                  | "URL"          | null        | "{}"                   | null
        "RSInstOne"       | null              | 6        | true              | "null"                              | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "URL"          | "Copy"      | null                   | null
        "RSInstOne"       | "RSInstThree"     | 6        | false             | "nrSupplierAnswerYesNoPick.json"    | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 6        | false             | "nrSupplierPrintPullSlip.json"      | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_COPY_AWAIT_PICKING         | null               | null                  | "URL"          | "Copy"      | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 6        | false             | "nrSupplierAddURLToDocument.json"   | Status.PATRON_REQUEST_DOCUMENT_DELIVERED          | Status.RESPONDER_DOCUMENT_DELIVERED         | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        //"RSInstOne"       | "RSInstThree"     | 6        | true              | "nrRequesterCompleteRequest.json"   | Status.PATRON_REQUEST_REQUEST_COMPLETE            | Status.RESPONDER_DOCUMENT_DELIVERED         | null               | null                  | "URL"          | "Copy"      | "{status=true}"        | null
        "RSInstOne"       | null              | 7        | true              | "null"                              | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "URL"          | "Copy"      | null                   | null
        "RSInstOne"       | "RSInstThree"     | 7        | false             | "nrSupplierAnswerYesNoPick.json"    | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 7        | false             | "nrSupplierCannotSupply.json"       | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | Status.RESPONDER_UNFILLED                   | "RSInstTwo"        | Status.RESPONDER_IDLE | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | null              | 8        | true              | "null"                              | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "URL"          | "Copy"      | null                   | null
        "RSInstOne"       | "RSInstThree"     | 8        | true              | "nrRequesterCancel.json"            | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | null           | null        | "{}"                   | null
        "RSInstOne"       | null              | 9        | true              | "null"                              | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "URL"          | "Copy"      | null                   | null
        "RSInstOne"       | "RSInstThree"     | 9        | false             | "nrSupplierAnswerYesNoPick.json"    | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 9        | true              | "nrRequesterCancel.json"            | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 9        | false             | "nrSupplierRespondToCancelNo.json"  | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 9        | true              | "nrRequesterCancel.json"            | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 9        | false             | "nrSupplierRespondToCancelYes.json" | Status.PATRON_REQUEST_CANCELLED                   | Status.RESPONDER_CANCELLED                  | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | null              | 10       | true              | "null"                              | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "URL"          | "Copy"      | null                   | null
        "RSInstOne"       | "RSInstThree"     | 10       | false             | "supplierConditionalSupply.json"    | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 10       | true              | "requesterAgreeConditions.json"     | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 10       | false             | "supplierAddCondition.json"         | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 10       | false             | "supplierMarkConditionsAgreed.json" | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "URL"          | "Copy"      | "{}"                   | null
        "RSInstOne"       | "RSInstThree"     | 10       | false             | "nrSupplierPrintPullSlip.json"      | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_COPY_AWAIT_PICKING         | null               | null                  | "URL"          | "Copy"      | "{status=true}"        | null
        "RSInstOne"       | "RSInstThree"     | 10       | false             | "nrSupplierAddURLToDocument.json"   | Status.PATRON_REQUEST_DOCUMENT_DELIVERED          | Status.RESPONDER_DOCUMENT_DELIVERED         | null               | null                  | "URL"          | "Copy"      | "{}"                   | ({ r, s -> r.pickupURL == 'https://some.domain?param=value' })
    }

    void "test Dynamic Groovy"() {
        when: "Initialise the groovy source"
        String scriptSource =
                '''
    parameter1.equals("Test");
''';
        String scriptAsClassSource =
                '''
    arguments.parameter1.equals("Test");
''';
        Map ScriptArguments = ["parameter1": "Test"];
        Object scriptResult = dynamicGroovyService.executeScript(scriptSource, ScriptArguments);
        Object scriptResultAsClass = dynamicGroovyService.executeScript("scriptCacheKey", scriptAsClassSource, ScriptArguments);
        Object scriptResultAsClassCache = dynamicGroovyService.executeScript("scriptCacheKey", scriptAsClassSource, ScriptArguments);

        String classSource = '''
class DosomethingSimple {
    public String perform(Map args) {
        return(args.parameter1 + "-" + args.secondParameter);
    }
    public String toString() {
        return("Goodness gracious me");
    }
}
''';

        Object classResultDefaultMethod = dynamicGroovyService.executeClass("cacheKey", classSource, ["parameter1": "request", "secondParameter": 4]);
        Object classResultCacheMethod = dynamicGroovyService.executeClass("cacheKey", "As long as its null it will be taken from the cache", null, "toString");

        then:"Confirm confirm we get the expected results from executing dynamic groovy"
        assert(scriptResult instanceof Boolean);
        assert (scriptResult == true);
        assert(scriptResultAsClass instanceof Boolean);
        assert (scriptResultAsClass == true);
        assert(scriptResultAsClassCache instanceof Boolean);
        assert (scriptResultAsClassCache == true);
        assert(classResultDefaultMethod instanceof String);
        assert(classResultDefaultMethod == "request-4");
        assert(classResultCacheMethod instanceof String);
        assert(classResultCacheMethod == "Goodness gracious me");
    }

    void "test Import"(String tenantId, String importFile) {
        when: "Perform the import"
        String jsonImportFile = new File("src/integration-test/resources/stateModels/${importFile}").text;
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Perform the import
        def responseJson = doPost("${baseUrl}/rs/stateModel/import".toString(), jsonImportFile);

        then:"Confirm the result json has no errors"
        if (responseJson instanceof Map) {
            responseJson.each { arrayElement ->
                // The value should be of type ArrayList
                if (arrayElement.value instanceof ArrayList) {
                    arrayElement.value.each { error ->
                        if (error instanceof String) {
                            // Ignore lines beginning with "No array of "
                            if (!error.startsWith("No array of ")) {
                                // It should end with ", errors: 0" otherwise we have problems
                                assert(error.endsWith(", errors: 0"));
                            }
                        } else {
                            log.debug("List element is not of type String, it has type " + error.getClass());
                            assert(false);
                        }
                    }
                } else {
                    // For some reason we did not get an array list
                    log.debug("Map element with key " + arrayElement.key + " is not an ArrayList");
                    assert(false);
                }
            }
        } else {
            // We obviously did not get json returned
            log.debug("Json returned by import is not a Map");
            assert(false);
        }

        where:
        tenantId      | importFile
        "RSInstThree" | "testResponder.json"
    }

    void "set_responder_state_model"(String tenantId, String stateModel) {
        when:"Progress the request"
            // Ensure we have the correct model for the responder
            log.debug("Setting responder state model to " + stateModel);
            changeSettings(tenantId, [ responder_returnables_state_model : stateModel ], true);


        then:"If no exception assume it has been set"
        assert(true);

        where:
        tenantId      | stateModel
        "RSInstThree" | "testResponder"
    }

    /**
     * This test case is actually being executed multiple times as it works through the scenarios, so it could actually take a long time to run
     */
    void "test inherited_scenario"(
            String requesterTenantId,
            String responderTenantId,
            int scenario,
            boolean isRequesterAction,
            String actionFile,
            String requesterStatus,
            String responderStatus,
            String newResponderTenant,
            String newResponderStatus,
            String patronIdentifier,
            String expectedActionResponse
    ) {
        when:"Progress the request"
        // Default state model for instance 3 should have been set to testResponder

        String actionResponse = doScenarioAction(
                requesterTenantId,
                responderTenantId,
                scenario,
                isRequesterAction,
                actionFile,
                requesterStatus,
                responderStatus,
                newResponderTenant,
                newResponderStatus,
                patronIdentifier
        );

        log.debug("Scenario: ${scenario}, Responder id: ${this.testctx.request_data[SCENARIO_RESPONDER_ID]}, action file: ${actionFile}");
        log.debug("Expected Action response: ${expectedActionResponse}, action response: ${actionResponse}");

        then:"Check the response value"
        assert this.testctx.request_data[SCENARIO_REQUESTER_ID] != null;
        assert this.testctx.request_data[SCENARIO_RESPONDER_ID] != null;
        assert this.testctx.request_data[SCENARIO_PATRON_REFERENCE] != null;
        if (expectedActionResponse != null) {
            assert expectedActionResponse == actionResponse;
        }

        where:
        requesterTenantId | responderTenantId | scenario | isRequesterAction | actionFile                          | requesterStatus                                   | responderStatus                             | newResponderTenant | newResponderStatus    | patronIdentifier | expectedActionResponse
        "RSInstOne"       | null              | 101      | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "Unknown"        | null
        "RSInstOne"       | "RSInstThree"     | 10101    | false             | "supplierConditionalSupply.json"    | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10102    | true              | "requesterAgreeConditions.json"     | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10103    | false             | "goSwimming.json"                   | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | "goneSwimming"                              | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10104    | false             | "finishedSwimming.json"             | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10105    | false             | "supplierAddCondition.json"         | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10106    | false             | "supplierMarkConditionsAgreed.json" | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10107    | false             | "supplierPrintPullSlip.json"        | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_AWAIT_PICKING              | null               | null                  | null             | "{status=true}"
        "RSInstOne"       | "RSInstThree"     | 10108    | false             | "goSwimming.json"                   | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | "goneSwimming"                              | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10109    | false             | "finishedSwimming.json"             | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_AWAIT_PICKING              | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10110    | false             | "supplierCheckInToReshare.json"     | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_AWAIT_SHIP                 | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10111    | false             | "supplierMarkShipped.json"          | Status.PATRON_REQUEST_SHIPPED                     | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10112    | false             | "doSudoku.json"                     | Status.PATRON_REQUEST_SHIPPED                     | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10114    | true              | "requesterReceived.json"            | Status.PATRON_REQUEST_CHECKED_IN                  | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10115    | true              | "patronReturnedItem.json"           | Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING    | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null             | "{status=true}"
        "RSInstOne"       | "RSInstThree"     | 10116    | false             | "doSudoku.json"                     | Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING    | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | null             | "{}"
        "RSInstOne"       | "RSInstThree"     | 10118    | true              | "shippedReturn.json"                | Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER         | Status.RESPONDER_ITEM_RETURNED              | null               | null                  | null             | "{status=true}"
        "RSInstOne"       | "RSInstThree"     | 10119    | false             | "supplierCheckOutOfReshare.json"    | Status.PATRON_REQUEST_REQUEST_COMPLETE            | Status.RESPONDER_COMPLETE                   | null               | null                  | null             | "{status=true}"
    }

    void "check_is_available_groovy"(
            String requesterTenantId,
            String responderTenantId
    ) {
        when:"Create the request and get valid actions"
        // For the patron identifier NoSwimming, the action goSwimming should not be available
        String actionResponse = doScenarioAction(
                requesterTenantId,
                null,
                100001,
                true,
                null,
                Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
                null,
                responderTenantId,
                Status.RESPONDER_IDLE,
                "NoSwimming"
        );

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': responderTenantId ]);

        String validActionsUrl = "${baseUrl}/rs/patronrequests/${this.testctx.request_data[SCENARIO_RESPONDER_ID]}/validActions";
        log.debug("doGet(" + validActionsUrl + "");
        // Get hold of the valid actions for the responder
        def validActionsResponse = doGet(validActionsUrl);

        log.debug("Valid responder actions response: ${validActionsResponse.actions.toString()}");

        then:"Check that goSwimmin is not a valid action"
        assert(!validActionsResponse.actions.contains("goSwimming"));

        where:
        requesterTenantId | responderTenantId
        "RSInstOne"       | "RSInstThree"
    }

    void "Check_Statistics_returned"(String tenantId, String ignore) {
        when:"We download the statistics"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Request the statistics
        def statisticsResponse = doGet("${baseUrl}rs/statistics");
        log.debug("Response from statistics: " + statisticsResponse.toString());

        then:"Check we have received some statistics"
        // Should have the current statistics
        assert(statisticsResponse?.current != null);

        // We should also have the requests by state
        assert(statisticsResponse.requestsByState != null);

        // We should have the number of requests that are actively borrowing
        assert(statisticsResponse?.current.find { statistic -> statistic.context.equals("/activeBorrowing") } != null);

        // We should also have the number of requests that are currently on loan
        assert(statisticsResponse?.current.find { statistic -> statistic.context.equals("/activeLoans") } != null);

        where:
        tenantId      | ignore
        'RSInstThree' | ''
    }

    void "setup requests awaiting to be printed"(
            String requesterTenantId,
            String responderTenantId,
            int scenario,
            boolean isRequesterAction,
            String actionFile,
            String requesterStatus,
            String responderStatus,
            String newResponderTenant,
            String newResponderStatus,
            String patronIdentifier,
            String expectedActionResponse
    ) {
        when:"Progress the request"
        // Default state model for instance 3 should have been set to testResponder

        String actionResponse = doScenarioAction(
                requesterTenantId,
                responderTenantId,
                scenario,
                isRequesterAction,
                actionFile,
                requesterStatus,
                responderStatus,
                newResponderTenant,
                newResponderStatus,
                patronIdentifier
        );

        log.debug("Scenario: ${scenario}, Responder id: ${this.testctx.request_data[SCENARIO_RESPONDER_ID]}, action file: ${actionFile}");
        log.debug("Expected Action response: ${expectedActionResponse}, action response: ${actionResponse}");

        then:"Check the response value"
        assert this.testctx.request_data[SCENARIO_REQUESTER_ID] != null;
        assert this.testctx.request_data[SCENARIO_RESPONDER_ID] != null;
        assert this.testctx.request_data[SCENARIO_PATRON_REFERENCE] != null;
        if (expectedActionResponse != null) {
            assert expectedActionResponse == actionResponse;
        }

        where:
        requesterTenantId | responderTenantId | scenario | isRequesterAction | actionFile                          | requesterStatus                                   | responderStatus                             | newResponderTenant | newResponderStatus    | patronIdentifier | expectedActionResponse
        "RSInstOne"       | null              | 201      | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "Unknown"        | null
        "RSInstOne"       | "RSInstThree"     | 20101    | false             | "supplierAnswerYes.json"            | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null             | "{}"
        "RSInstOne"       | null              | 202      | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "Unknown"        | null
        "RSInstOne"       | "RSInstThree"     | 20201    | false             | "supplierAnswerYes.json"            | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null             | "{}"
        "RSInstOne"       | null              | 203      | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | "Unknown"        | null
        "RSInstOne"       | "RSInstThree"     | 20301    | false             | "supplierAnswerYes.json"            | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | null             | "{}"
    }

    void "Generate a Batch"(String tenantId, String ignore) {
        when:"We generate a batch"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Create a batch
        def generateBatchResponse = doGet("${baseUrl}rs/patronrequests/generatePickListBatch?filters=isRequester==false&filters=state.terminal==false&filters=state.code==RES_NEW_AWAIT_PULL_SLIP");
        log.debug("Response from generate batch: " + generateBatchResponse.toString());

        // set the batch id in the test context so the next text can retrieve it
        testctx.batchId = generateBatchResponse.batchId;

        then:"Check we have a batch id"
        // Should have the a response
        assert(generateBatchResponse != null);

        // Should have a batch id as part of the response
        assert(generateBatchResponse.batchId != null);

        where:
        tenantId      | ignore
        'RSInstThree' | ''
    }

    void "Print pullslip from batch"(String tenantId, String ignore) {
        when:"Request the pull slip from a batch"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Generate the picklist
        def generatePickListResponse = doGet("${baseUrl}/rs/report/generatePicklist?batchId=${testctx.batchId}");

        then:"Check we have file in response"
        // We should have a byte array
        assert(generatePickListResponse instanceof byte[]);

        where:
        tenantId      | ignore
        'RSInstThree' | ''
    }

    void "Print pullslip from batch generate error"(String tenantId, String ignore) {
        when:"Request the pull slip from a batch"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Generate the picklist
        def generatePickListResponse = null;
        int statusCode = 200;
        try {
            generatePickListResponse = doGet("${baseUrl}/rs/report/generatePicklist?batchId=nonExistantBatchId");
        } catch (groovyx.net.http.HttpException e) {
            statusCode = e.getStatusCode();
            generatePickListResponse = e.getBody();
        }
        log.debug("Response from generatePickList: " + generatePickListResponse.toString());

        then:"Check we have an error response"
        // The error element should exist
        assert(generatePickListResponse?.error != null);
        assert(statusCode == 400);

        where:
        tenantId      | ignore
        'RSInstThree' | ''
    }

    void "Action requsts as printed from batch"(String tenantId, String ignore) {
        when:"Action requests in batch to be marked as printed"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Action the requests in the batch as printed
        def markBatchAsPrintedResponse = doGet("${baseUrl}/rs/patronrequests/markBatchAsPrinted?batchId=${testctx.batchId}");
        log.debug("Response from markBatchAsPrinted: " + markBatchAsPrintedResponse.toString());

        then:"Check we have file in response"
        // The error element should exist
        assert(markBatchAsPrintedResponse.successful.size() == 3);
        assert(markBatchAsPrintedResponse.failed.size() == 0);
        assert(markBatchAsPrintedResponse.notValid.size() == 0);

        where:
        tenantId      | ignore
        'RSInstThree' | ''
    }

    void "Action requests as printed invalid batch generate error"(String tenantId, String ignore) {
        when:"Request the pull slip from a batch"

        // Set the headers
        setHeaders([ 'X-Okapi-Tenant': tenantId ]);

        // Generate the picklist
        def markBatchAsPrintedResponse = null;
        int statusCode = 200;
        try {
            markBatchAsPrintedResponse = doGet("${baseUrl}/rs/patronrequests/markBatchAsPrinted?batchId=nonExistantBatchId");
        } catch (groovyx.net.http.HttpException e) {
            statusCode = e.getStatusCode();
            markBatchAsPrintedResponse = e.getBody();
        }
        log.debug("Response from markBatchAsPrinted: " + markBatchAsPrintedResponse.toString());

        then:"Check we have a valid error response"
        // The error element should exist
        assert(markBatchAsPrintedResponse?.error != null);
        assert(statusCode == 400);

        where:
        tenantId      | ignore
        'RSInstThree' | ''
    }

    void "Test to see if duplicate requests get flagged properly"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol) {
        when: "Post new duplicate requests"
        changeSettings(tenantId, [ (SettingsData.SETTING_CHECK_DUPLICATE_TIME) : 3 ]);

        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ]

        def req_one_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_one",
                tags: [ 'RS-DUPLICATE-TEST-1']
        ];

        def req_two_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_two",
                tags: [ 'RS-DUPLICATE-TEST-2']
        ];


        setHeaders(headers);

        def respOne = doPost("${baseUrl}/rs/patronrequests".toString(), req_one_json);
        log.debug("Created PatronRequest 1: RESP: ${respOne} ID: ${respOne?.id}");

        waitForRequestState(tenantId, 20000, requestPatronId + "_one",
                Status.PATRON_REQUEST_IDLE);

        Thread.sleep(2000); // I hate doing this

        String patronRequestTwoPost = "${baseUrl}/rs/patronrequests".toString();
        log.debug("Posting duplicate Patron Request to $patronRequestTwoPost");
        def respTwo = doPost(patronRequestTwoPost, req_two_json);
        log.debug("Created PatronRequest 2: RESP: ${respTwo} ID: ${respTwo?.id}");

        waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                Status.PATRON_REQUEST_DUPLICATE_REVIEW);

        String jsonPayload = new File("src/integration-test/resources/scenarios/requesterApproveDuplicate.json").text;

        log.debug("jsonPayload: ${jsonPayload}");
        String performActionURL = "${baseUrl}/rs/patronrequests/${respTwo.id}/performAction".toString();
        log.debug("Posting to performAction at $performActionURL");
        def actionResponse = doPost(performActionURL, jsonPayload);

        waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                Status.PATRON_REQUEST_VALIDATED);


        then: "Check values"
        assert true;

        where:
        tenantId    | requestTitle                      | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol
        'RSInstOne' | 'Excellent Argument, However...'  | 'Mom, U.R.'   | '1234-5678-9123-4567' | '9876-1234'       | 'ISIL:RST1'


    }

    void "Test to see if requests with precededBy are ignored for duplicates"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol) {
        when: "Post new duplicate requests"
        changeSettings(tenantId, [ (SettingsData.SETTING_CHECK_DUPLICATE_TIME) : 3 ]);

        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ]

        def req_one_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_one",
                tags: [ 'RS-DUPLICATE-TEST-1']
        ];


        setHeaders(headers);

        def respOne = doPost("${baseUrl}/rs/patronrequests".toString(), req_one_json);
        log.debug("Created PatronRequest 1: RESP: ${respOne} ID: ${respOne?.id}");

        waitForRequestState(tenantId, 20000, requestPatronId + "_one",
                Status.PATRON_REQUEST_IDLE);

        Thread.sleep(2000); // I hate doing this

        def req_two_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_two",
                precededBy: respOne.id,
                tags: [ 'RS-DUPLICATE-TEST-2']
        ];

        String patronRequestTwoPost = "${baseUrl}/rs/patronrequests".toString();
        log.debug("Posting duplicate Patron Request to $patronRequestTwoPost");
        def respTwo = doPost(patronRequestTwoPost, req_two_json);
        log.debug("Created PatronRequest 2: RESP: ${respTwo} ID: ${respTwo?.id}");

        boolean dupeFailed = false;
        try {
            waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                    Status.PATRON_REQUEST_DUPLICATE_REVIEW);
        } catch (Exception e) {
            dupeFailed = true;
        }


        then: "Check values"
        assert(dupeFailed);

        where:
        tenantId    | requestTitle                      | requestAuthor     | requestSystemId       | requestPatronId   | requestSymbol
        'RSInstOne' | 'Do We Gotta?'                    | 'Saydso, Cozzi'   | '2234-5978-9123-9567' | '9176-9234'       | 'ISIL:RST1'


    }

    void "Test to see if we can cancel a duplicate request"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol) {
        when: "Post new duplicate requests"
        changeSettings(tenantId, [ (SettingsData.SETTING_CHECK_DUPLICATE_TIME) : 3 ]);

        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ]

        def req_one_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_one",
                tags: [ 'RS-DUPLICATE-TEST-1']
        ];

        def req_two_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_two",
                tags: [ 'RS-DUPLICATE-TEST-2']
        ];

        setHeaders(headers);

        def respOne = doPost("${baseUrl}/rs/patronrequests".toString(), req_one_json);
        log.debug("Created PatronRequest 1: RESP: ${respOne} ID: ${respOne?.id}");

        waitForRequestState(tenantId, 20000, requestPatronId + "_one",
                Status.PATRON_REQUEST_IDLE);

        Thread.sleep(2000); // Booo

        String patronRequestTwoPost = "${baseUrl}/rs/patronrequests".toString();
        log.debug("Posting duplicate Patron Request to $patronRequestTwoPost");
        def respTwo = doPost(patronRequestTwoPost, req_two_json);
        log.debug("Created PatronRequest 2: RESP: ${respTwo} ID: ${respTwo?.id}");

        waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                Status.PATRON_REQUEST_DUPLICATE_REVIEW);

        String jsonPayload = new File("src/integration-test/resources/scenarios/requesterCancelDuplicate.json").text;

        log.debug("jsonPayload: ${jsonPayload}");
        String performActionURL = "${baseUrl}/rs/patronrequests/${respTwo.id}/performAction".toString();
        log.debug("Posting to performAction at $performActionURL");
        def actionResponse = doPost(performActionURL, jsonPayload);

        waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                Status.PATRON_REQUEST_CANCELLED);

        then: "Check values"
        assert true;

        where:
        tenantId    | requestTitle                      | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol
        'RSInstOne' | 'Not My Problem'  | 'Brokit, C.U.'   | '4321-8765-1239-1234' | '6789-3241'       | 'ISIL:RST1'


    }

    void "Ensure the lenders of last resort populate the rota"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol,
            String lastResort) {
        when: "Post new request with last resort enabled"
        changeSettings(tenantId, [ (SettingsData.SETTING_LAST_RESORT_LENDERS) : lastResort ]);

        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ]

        def req_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_one",
                tags: [ 'RS-LAST_RESORT' ]
        ];

        setHeaders(headers);

        def resp = doPost("${baseUrl}/rs/patronrequests".toString(), req_json);
        log.debug("Created PatronRequest 1: RESP: ${resp} ID: ${resp?.id}");

        waitForRequestState(tenantId, 20000, requestPatronId, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);

        def fresh = doGet("${baseUrl}rs/patronrequests/${resp.id}");
        def lastResorts = lastResort.split(',');

        then: "Ensure all last resort lenders added to the rota"
        assert (fresh.rota.findAll {it?.directoryId in lastResort.split(',')}).size() == lastResort.split(',').size()

        where:
        tenantId    | requestTitle              | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol | lastResort
        'RSInstOne' | 'This Is My Last Resort'  | 'Brokit, C.U.'| '4321-8765-1239-1234' | '1717-1717'       | 'ISIL:RST1'   | 'TLA:SNAFU,OMG:SCUBA'
    }


    void "Test to see if blank form requests are properly handled" (
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestPatronId,
            String requestSymbol) {

        when: "Post new blank form requests"
        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ];

        def request_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_one",
                tags: [ 'RS-BLANK-FORM-TEST-1']
        ];

        setHeaders(headers);

        def response = doPost("${baseUrl}/rs/patronrequests".toString(), request_json);

        waitForRequestState(tenantId, 20000, requestPatronId + "_one",
                Status.PATRON_REQUEST_BLANK_FORM_REVIEW);

        String jsonPayload = new File("src/integration-test/resources/scenarios/requesterCancel.json").text;
        log.debug("cancel request payload: ${jsonPayload}");
        String performActionUrl = "${baseUrl}/rs/patronrequests/${response?.id}/performAction".toString();
        log.debug("Posting requesterCencel payload to ${performActionUrl}");

        def actionResponse = doPost(performActionUrl, jsonPayload);

        waitForRequestState(tenantId, 20000, requestPatronId + "_one",
                Status.PATRON_REQUEST_CANCELLED);

        then: "Whatever"
        assert true;

        where:

        tenantId    | requestTitle          | requestAuthor | requestPatronId   | requestSymbol
        'RSInstOne' | 'Missing References'  | 'Dunno, Ivan' | '9977-2244'       | 'ISIL:RST1'

    }



    void "Attempt to retry a blank request after fixing"(

            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol) {

        when: "Post new blank form requests"
        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ];

        def request_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_two",
                tags: [ 'RS-BLANK-FORM-TEST-2']
        ];

        setHeaders(headers);

        def response = doPost("${baseUrl}/rs/patronrequests".toString(), request_json);

        waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                Status.PATRON_REQUEST_BLANK_FORM_REVIEW);

        def updated_request_json = [
                requestingInstitutionSymbol: requestSymbol,
                systemInstanceIdentifier: requestSystemId,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_two",

                tags: [ 'RS-BLANK-FORM-TEST-3']

        ];

        def put_response = doPut("${baseUrl}/rs/patronrequests/${response?.id}".toString(), updated_request_json);
        log.debug("got response from put request ${put_response}");

        String jsonPayload = new File("src/integration-test/resources/scenarios/requesterRetryRequest.json").text;
        log.debug("retryRequest payload: ${jsonPayload}");
        String performActionUrl = "${baseUrl}/rs/patronrequests/${response?.id}/performAction".toString();
        log.debug("Posting requesterRetryRequest payload to ${performActionUrl}");

        def actionResponse = doPost(performActionUrl, jsonPayload);

        waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                Status.PATRON_REQUEST_IDLE);


        then: "Whatever"
        assert true;

        where:

        tenantId    | requestTitle                 | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol
        'RSInstOne' | 'Believe in Second Chances'  | 'Ageen, Trey' | '5533-2233-6654-9191' | '8877-6644'       | 'ISIL:RST1'


    }

    void "Attempt to fix a blank request via retryValidation action"(

            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol) {

        when: "Post new blank form requests"
        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ];

        def request_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_five",
                tags: [ 'RS-BLANK-FORM-TEST-5']
        ];

        setHeaders(headers);

        def response = doPost("${baseUrl}/rs/patronrequests".toString(), request_json);

        waitForRequestState(tenantId, 20000, requestPatronId + "_five",
                Status.PATRON_REQUEST_BLANK_FORM_REVIEW);

        def jsonPayload = [
                action: "requesterRetryValidation",
                actionParams: [
                        requestingInstitutionSymbol: requestSymbol,
                        systemInstanceIdentifier: requestSystemId,
                        title: requestTitle,
                        author: requestAuthor,
                        patronIdentifier: requestPatronId,
                ]
        ];


        //String jsonPayload = new File("src/integration-test/resources/scenarios/requesterRetryRequest.json").text;
        log.debug("retryRequest payload: ${jsonPayload}");
        String performActionUrl = "${baseUrl}/rs/patronrequests/${response?.id}/performAction".toString();
        log.debug("Posting requesterRetryRequest payload to ${performActionUrl}");

        def actionResponse = doPost(performActionUrl, jsonPayload);

        waitForRequestState(tenantId, 20000, requestPatronId + "_five",
                Status.PATRON_REQUEST_IDLE);


        then: "Whatever"
        assert true;

        where:

        tenantId    | requestTitle                 | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol
        'RSInstOne' | 'Believe in Second Chances'  | 'Ageen, Trey' | '5533-2233-6654-9191' | '8177-6144'       | 'ISIL:RST1'


    }


    void "Attempt to retry a blank request without fixing"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestPatronId,
            String requestSymbol) {

        when: "Post new blank form requests"
        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ];

        def request_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_three",
                tags: [ 'RS-BLANK-FORM-TEST-2']
        ];

        setHeaders(headers);

        def response = doPost("${baseUrl}/rs/patronrequests".toString(), request_json);

        waitForRequestState(tenantId, 20000, requestPatronId + "_three",
                Status.PATRON_REQUEST_BLANK_FORM_REVIEW);


        String jsonPayload = new File("src/integration-test/resources/scenarios/requesterRetryRequest.json").text;
        log.debug("retryRequest payload: ${jsonPayload}");
        String performActionUrl = "${baseUrl}/rs/patronrequests/${response?.id}/performAction".toString();
        log.debug("Posting requesterRetryRequest payload to ${performActionUrl}");

        def actionResponse = doPost(performActionUrl, jsonPayload);

        waitForRequestState(tenantId, 20000, requestPatronId + "_three",
                Status.PATRON_REQUEST_BLANK_FORM_REVIEW);


        then: "Whatever"
        assert true;

        where:

        tenantId    | requestTitle      | requestAuthor | requestPatronId   | requestSymbol
        'RSInstOne' | 'How to be Lazy'  | 'Aroon, Lion' | '8577-6554'       | 'ISIL:RST1'


    }

    void "Attempt to send a blank request on to validated"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestPatronId,
            String requestSymbol) {

        when: "Post new blank form requests"
        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ];

        def request_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_four",
                tags: [ 'RS-BLANK-FORM-TEST-4']
        ];

        setHeaders(headers);

        def response = doPost("${baseUrl}/rs/patronrequests".toString(), request_json);

        waitForRequestState(tenantId, 20000, requestPatronId + "_four",
                Status.PATRON_REQUEST_BLANK_FORM_REVIEW);


        String jsonPayload = new File("src/integration-test/resources/scenarios/requesterForceValidate.json").text;
        log.debug("force validate payload: ${jsonPayload}");
        String performActionUrl = "${baseUrl}/rs/patronrequests/${response?.id}/performAction".toString();
        log.debug("Posting requesterRetryRequest payload to ${performActionUrl}");

        def actionResponse = doPost(performActionUrl, jsonPayload);

        waitForRequestState(tenantId, 20000, requestPatronId + "_four",
                Status.PATRON_REQUEST_VALIDATED);


        then: "Whatever"
        assert true;

        where:

        tenantId    | requestTitle          | requestAuthor  | requestPatronId   | requestSymbol
        'RSInstOne' | 'How NOT to be Lazy'  | 'Herd, Werkin' | '8577-6554'       | 'ISIL:RST1'


    }

    void "Attempt a request when we're over limit"(
            String tenantId,
            String requestPatronId,
            String requestSystemId,
            String requestSymbol,
            int requestLimit) {
        when: "Post requests over limit"

        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ];

        setHeaders(headers);

        for(int i=0 ; i < requestLimit ; i++) {
            def req = [
                    requestingInstitutionSymbol: requestSymbol,
                    title: randomCrap(25),
                    author: randomCrap(20),
                    systemInstanceIdentifier: requestSystemId,
                    patronIdentifier: requestPatronId,
                    isRequester: true,
                    patronReference: requestPatronId + "_" + i
            ];
            doPost("${baseUrl}/rs/patronrequests".toString(), req);
        }

        changeSettings(tenantId, [ (SettingsData.SETTING_MAX_REQUESTS) : requestLimit ]);

        def over_limit_req = [
                requestingInstitutionSymbol: requestSymbol,
                title: randomCrap(25),
                author: randomCrap(20),
                systemInstanceIdentifier: requestSystemId,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_over_limit"
        ];

        doPost("${baseUrl}/rs/patronrequests".toString(), over_limit_req);

        waitForRequestState(tenantId, 20000, requestPatronId + "_over_limit",
                Status.PATRON_REQUEST_IDLE);

        then:
        assert true;

        where:
        tenantId    | requestPatronId | requestSystemId       | requestSymbol | requestLimit
        'RSInstOne' | '4512-9034'     | '2199-7171-9343-6532' | 'ISIL:RST1'   | 3

    }

    void "Try to submit and fix a blank request on the nonreturnables statemodel"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol) {

        when: "Post new blank form requests"
        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ];

        def request_json = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_two",
                serviceType: "Copy",
                tags: [ 'RS-BLANK-FORM-TEST-2']
        ];

        setHeaders(headers);

        def response = doPost("${baseUrl}/rs/patronrequests".toString(), request_json);

        waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                Status.PATRON_REQUEST_BLANK_FORM_REVIEW);

        def updated_request_json = [
                requestingInstitutionSymbol: requestSymbol,
                systemInstanceIdentifier: requestSystemId,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: requestPatronId,
                isRequester: true,
                patronReference: requestPatronId + "_two",
                serviceType: "Copy",
                tags: [ 'RS-BLANK-FORM-TEST-3']

        ];

        def put_response = doPut("${baseUrl}/rs/patronrequests/${response?.id}".toString(), updated_request_json);
        log.debug("got response from put request ${put_response}");

        String jsonPayload = new File("src/integration-test/resources/scenarios/nrRequesterRetryRequest.json").text;
        log.debug("retryRequest payload: ${jsonPayload}");
        String performActionUrl = "${baseUrl}/rs/patronrequests/${response?.id}/performAction".toString();
        log.debug("Posting requesterRetryRequest payload to ${performActionUrl}");

        doPost(performActionUrl, jsonPayload);

        waitForRequestState(tenantId, 20000, requestPatronId + "_two",
                Status.PATRON_REQUEST_IDLE);

        Thread.sleep(500);

        then: "Whatever"
        assert true;

        where:
        tenantId    | requestTitle                 | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol
        'RSInstOne' | 'We Gotta Do it Over'        | 'Pete, Rea'   | '1533-2233-1654-9192' | '8887-6644'       | 'ISIL:RST1'
    }

    void "Test transmission of values to supplier"(
            String copyrightType, String publicationType, String patronIdentifier) {
        String requesterTenantId = "RSInstOne";
        String responderTenantId = "RSInstThree";
        String patronReference = 'ref-' + patronIdentifier;
        when: "We create a requester request with copyright and pub info"
        Map request = [
                patronReference: patronReference,
                title: 'Copyright Publication Type Test',
                author: 'UR MOM',
                requestingInstitutionSymbol: 'ISIL:RST1',
                systemInstanceIdentifier: '123-321',
                patronIdentifier: patronIdentifier,
                isRequester: true,
                serviceType: "Copy",
                deliveryMethod: "URL",
                publicationType: publicationType,
                copyrightType: copyrightType,
                serviceLevel: "Rush",
                maximumCostsMonetaryValue: "329.43",
                maximumCostsCurrencyCode: "AUD"
        ];


            setHeaders([ 'X-Okapi-Tenant': requesterTenantId ]);
            doPost("${baseUrl}/rs/patronrequests".toString(), request);

            // requester request sent to supplier?
            waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);

            // responder request created?
            String responderRequestId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_IDLE);
            def responderRequestData = doGet("${baseUrl}rs/patronrequests/${responderRequestId}");

        then: "Assert values"
            assert(responderRequestData.patronReference == patronReference);
            assert(responderRequestData.publicationType?.value == publicationType);
            assert(responderRequestData.copyrightType?.value == copyrightType);
            assert(responderRequestData.serviceLevel?.value == "rush");
            assert(responderRequestData.maximumCostsMonetaryValue == new BigDecimal("329.43"));
            assert(responderRequestData.maximumCostsCurrencyCode?.value == "aud");
            assert(true);

        where:
            copyrightType | publicationType | patronIdentifier
            'us-ccg'      | "book"          | "TRANS-VALS-TYPE-0001"
        // Create request on first tenant w/ copyright and pub info
        // Look for request to get to 'sent to supplier'
        // Look for responder request w/ patron reference
        // check for copyright and publication type in responder request
    }

    void "Test automatic rerequest to different cluster id after unfilled transfer"(
            String deliveryMethod,
            String serviceType,
            String actionFile
    ) {
        String patronIdentifier =  "ABCD-EFG-HIJK-0001";
        String requesterTenantId = "RSInstOne";
        String responderTenantId = "RSInstThree";
        String requestTitle = "YA Bad Book";
        String requestAuthor = "Mr. Boringman";
        String requestSymbol = "ISIL:RST1"
        String patronReference = "ref-" + patronIdentifier + randomCrap(6);
        String serviceLevel = "Express";
        String currencyCode = "USD";
        String monetaryValue = "25.55";

        when: "do the thing"

        changeSettings(requesterTenantId, [ (SettingsData.SETTING_CHECK_DUPLICATE_TIME) : 0 ]);

        Map request = [
                requestingInstitutionSymbol: requestSymbol,
                title: requestTitle,
                author: requestAuthor,
                patronIdentifier: patronIdentifier,
                isRequester: true,
                patronReference: patronReference,
                systemInstanceIdentifier: "123-456-789",
                oclcNumber: "1234312",
                deliveryMethod: deliveryMethod,
                serviceType: serviceType,
                serviceLevel: serviceLevel,
                maximumCostsMonetaryValue: monetaryValue,
                maximumCostsCurrencyCode: currencyCode,
                tags: ['RS-REREQUEST-TEST-1']
        ];

        changeSettings(requesterTenantId, [ (SettingsData.SETTING_SHARED_INDEX_INTEGRATION) : "mock" ]);

        setHeaders([ 'X-Okapi-Tenant': requesterTenantId ]);
        doPost("${baseUrl}/rs/patronrequests".toString(), request);

        String requesterRequestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);

        String responderRequestId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_IDLE);

        String jsonPayload = new File("src/integration-test/resources/scenarios/"+actionFile).text;
        String performActionUrl = "${baseUrl}/rs/patronrequests/${responderRequestId}/performAction".toString();
        log.debug("Posting cannot supply payload to ${performActionUrl}");
        setHeaders([ 'X-Okapi-Tenant': responderTenantId ]);
        doPost(performActionUrl, jsonPayload);

        waitForRequestStateById(responderTenantId, 10000, responderRequestId, Status.RESPONDER_UNFILLED);

        waitForRequestStateById(requesterTenantId, 10000, requesterRequestId, Status.PATRON_REQUEST_REREQUESTED);

        //get original request
        setHeaders([ 'X-Okapi-Tenant': requesterTenantId ]);
        def requesterRequestData = doGet("${baseUrl}rs/patronrequests/${requesterRequestId}");

        setHeaders([ 'X-Okapi-Tenant': responderTenantId ]);
        def responderRequestData = doGet("${baseUrl}rs/patronrequests/${responderRequestId}");

        String newRequesterRequestId = requesterRequestData.succeededBy.id;

        setHeaders([ 'X-Okapi-Tenant': requesterTenantId ]);
        def newRequesterRequestData = doGet("${baseUrl}rs/patronrequests/${newRequesterRequestId}");

        String newHrid = newRequesterRequestData.hrid;

        String newResponderRequestId = waitForRequestStateByHrid(responderTenantId, 10000, newHrid, Status.RESPONDER_IDLE);

        setHeaders([ 'X-Okapi-Tenant': responderTenantId ]);
        def newResponderRequestData = doGet("${baseUrl}rs/patronrequests/${newResponderRequestId}");

        assert(newResponderRequestData.precededBy?.id == responderRequestId)

        def updatedResponderRequestData = doGet("${baseUrl}rs/patronrequests/${responderRequestId}");

        then:
        assert(newRequesterRequestData.title == "Case study research : design and methods /");
        assert(newRequesterRequestData.serviceLevel?.value?.equals(serviceLevel.toLowerCase()));
        assert(newRequesterRequestData.maximumCostsCurrencyCode?.value?.equals(currencyCode.toLowerCase()));
        assert(newRequesterRequestData.maximumCostsMonetaryValue?.toString()?.equals(monetaryValue));
        assert(newResponderRequestData.precededBy?.id == responderRequestId);
        assert(updatedResponderRequestData.succeededBy?.id == newResponderRequestData.id);


        where:
        deliveryMethod | serviceType | actionFile
        "URL"         | "Copy"       | "nrSupplierCannotSupplyTransfer.json"
        null          | null         | "supplierCannotSupplyTransfer.json"

    }
    void "test resubmission of request after end-of-rota reviewed state"(
            String originalServiceType,
            String originalDeliveryType,
            String newServiceType,
            String newDeliveryType,
            String respondNoActionFile,
            String resubmitAction,
            String newStateModel
    ) {
        String patronIdentifier = "AAA-SSS-FFF-456";
        String requesterTenantId = "RSInstThree";
        String responderTenantId = "RSInstOne";
        String requestTitle = "Axe U Sumpfin";
        String requestAuthor = "Lemmy, A.";
        String requestSymbol = "ISIL:RST3";
        String patronReference = "ref-" + patronIdentifier + randomCrap(6);
        String systemInstanceIdentifier = "121-656-989";

        when: "Do it"

        Map request = [
                requestingInstitutionSymbol: requestSymbol,
                title                      : requestTitle,
                author                     : requestAuthor,
                patronIdentifier           : patronIdentifier,
                isRequester                : true,
                patronReference            : patronReference,
                systemInstanceIdentifier   : systemInstanceIdentifier,
                deliveryMethod             : originalDeliveryType,
                serviceType                : originalServiceType,
                tags                       : ['RS-RESUBMIT-TEST-1']
        ]

        setHeaders(['X-Okapi-Tenant': requesterTenantId]);
        doPost("${baseUrl}/rs/patronrequests".toString(), request);

        String requesterRequestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);
        log.debug("Requester request id is ${requesterRequestId}");

        String responderRequestId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_IDLE);
        log.debug("Responder request id is ${responderRequestId}");

        String jsonPayload = new File("src/integration-test/resources/scenarios/" + respondNoActionFile).text;
        String responderPerformActionUrl = "${baseUrl}/rs/patronrequests/${responderRequestId}/performAction".toString();
        log.debug("Posting cannot supply payload to ${responderPerformActionUrl}");
        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        doPost(responderPerformActionUrl, jsonPayload);

        waitForRequestStateById(responderTenantId, 10000, responderRequestId, Status.RESPONDER_UNFILLED);

        waitForRequestStateById(requesterTenantId, 10000, requesterRequestId, Status.PATRON_REQUEST_END_OF_ROTA);

        String newPatronReference = "rerequest" + patronIdentifier +randomCrap(6);
        Map rerequestParams = [
                "action" : resubmitAction,
                "actionParams" :
                        [
                            "patronReference" : newPatronReference,
                            "deliveryMethod" : newDeliveryType,
                            "serviceType" : newServiceType,
                            "isRequester" : true,
                            "title" : requestTitle,
                            "author" : requestAuthor,
                            "requestingInstitutionSymbol" : requestSymbol,
                            "systemInstanceIdentifier" : systemInstanceIdentifier
                        ]
        ];

        setHeaders(['X-Okapi-Tenant':requesterTenantId]);
        String requesterPerformActionUrl = "${baseUrl}/rs/patronrequests/${requesterRequestId}/performAction".toString();
        doPost(requesterPerformActionUrl, JsonOutput.toJson(rerequestParams));

        String newRequesterRequestId = waitForRequestState(requesterTenantId, 10000, newPatronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);

        def newRequesterRequestData = doGet("${baseUrl}rs/patronrequests/${newRequesterRequestId}");


        then:
        assert(newRequesterRequestData.stateModel.shortcode == newStateModel);

        where:
        originalServiceType | originalDeliveryType | newServiceType | newDeliveryType | respondNoActionFile           | resubmitAction           | newStateModel
        null                | null                 | "Copy"         | "URL"           | "supplierCannotSupply.json"   | "rerequest"              | StateModel.MODEL_NR_REQUESTER
        "Copy"              | "URL"                | null           | null            | "nrSupplierCannotSupply.json" | "rerequest"              | StateModel.MODEL_REQUESTER
        null                | null                 | null           | null            | "supplierCannotSupply.json"   | "rerequest"              | StateModel.MODEL_REQUESTER

    }

    void "test autoresponder for nonreturnables supplier"() {

        String patronIdentifier = "ABA-SJS-FJF-497";
        String requesterTenantId = "RSInstOne";
        String responderTenantId = "RSInstThree";
        String requestTitle = "Automating Your Workload";
        String requestAuthor = "Matton, Otto";
        String requestSymbol = "ISIL:RST1";
        String patronReference = "ref-" + patronIdentifier + randomCrap(6);
        String systemInstanceIdentifier = "141-636-919";

        when: "Do it"

        def changeSettingsResp = changeSettings(responderTenantId, [(SettingsData.SETTING_AUTO_RESPONDER_STATUS) : "on"]);
        log.debug("Results from changing settings: ${changeSettingsResp}");

        Map request = [
                requestingInstitutionSymbol: requestSymbol,
                title                      : requestTitle,
                author                     : requestAuthor,
                patronIdentifier           : patronIdentifier,
                isRequester                : true,
                patronReference            : patronReference,
                systemInstanceIdentifier   : systemInstanceIdentifier,
                deliveryMethod             : "URL",
                serviceType                : "Copy",
                tags                       : ['RS-COPY-AUTORESPOND-TEST-1']
        ];


        setHeaders(['X-Okapi-Tenant': requesterTenantId]);
        doPost("${baseUrl}/rs/patronrequests".toString(), request);

        String requesterRequestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY);
        log.debug("Requester request id is ${requesterRequestId}");

        String responderRequestId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_NEW_AWAIT_PULL_SLIP);

        then:
        assert(true);

    }

    @Ignore //Inconsistent
    void "test messaging between tenants"() {
        String patronIdentifier = "ABC-RJR-GGF-245";
        String requesterTenantId = "RSInstOne";
        String responderTenantId = "RSInstThree";
        String requestTitle = "Staying in Touch";
        String requestAuthor = "Mei, Karl";
        String requestSymbol = "ISIL:RST1";
        String patronReference = "ref-" + patronIdentifier + randomCrap(6);
        String systemInstanceIdentifier = "121-434-313";

        when: "Do the thing"

        def changeSettingsResp = changeSettings(responderTenantId, [(SettingsData.SETTING_AUTO_RESPONDER_STATUS) : "off"]);

        Map request = [
                requestingInstitutionSymbol: requestSymbol,
                title                      : requestTitle,
                author                     : requestAuthor,
                patronIdentifier           : patronIdentifier,
                isRequester                : true,
                patronReference            : patronReference,
                systemInstanceIdentifier   : systemInstanceIdentifier,
                tags                       : ['RS-MESSAGE-TEST-1']
        ];

        setHeaders(['X-Okapi-Tenant': requesterTenantId]);
        doPost("${baseUrl}/rs/patronrequests".toString(), request);

        String requesterRequestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);
        log.debug("Requester request id is ${requesterRequestId}");

        String responderRequestId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_IDLE);

        Map messageRequest = [
                action: "message",
                actionParams: [
                        note: "The secret code is 9871"
                ]
        ];

        String performActionUrl = "${baseUrl}/rs/patronrequests/${requesterRequestId}/performAction".toString();

        setHeaders(['X-Okapi-Tenant': requesterTenantId]);
        def messageActionResponse = doPost(performActionUrl, messageRequest);

        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def responderRequestDataMessageOne = doGet("${baseUrl}/rs/patronrequests/${responderRequestId}")


        boolean firstMessageFound = false;
        responderRequestDataMessageOne.notifications.each { notif ->
            if (notif.attachedAction == "Notification" && notif.isSender == false) {
                if (notif.messageContent == "The secret code is 9871") {
                    firstMessageFound = true;
                }
            }
        }

        //do a messages all seen action here

        Map messagesAllSeenRequest = [
                action: "messagesAllSeen",
                actionParams: [
                        seenStatus: true
                ]
        ];

        String responderPerformActionUrl = "${baseUrl}/rs/patronrequests/${responderRequestId}/performAction".toString();

        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def messagesAllSeenActionResponse = doPost(responderPerformActionUrl, messagesAllSeenRequest);


        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def responderRequestDataMessageOneAllRead = doGet("${baseUrl}/rs/patronrequests/${responderRequestId}");

        //Send another message

        Map newMessageRequest = [
                action: "message",
                actionParams: [
                        note: "The donkey flies at midnight"
                ]
        ];

        setHeaders(['X-Okapi-Tenant': requesterTenantId]);
        def newMessageActionResponse = doPost(performActionUrl, newMessageRequest);

        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def responderRequestDataMessageTwo = doGet("${baseUrl}/rs/patronrequests/${responderRequestId}")

        boolean secondMessageFound = false;
        String secondMessageId;
        responderRequestDataMessageTwo.notifications.each { notif ->
            if (notif.attachedAction == "Notification" && notif.isSender == false) {
                if (notif.messageContent == "The donkey flies at midnight") {
                    secondMessageFound = true;
                    secondMessageId = notif.id;
                }
            }
        }

        //Mark the second message read

        Map markMessageReadRequest = [
                action: "messageSeen",
                actionParams: [
                        id: secondMessageId,
                        seenStatus: true
                ]
        ];


        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def messageTwoSeenActionResponse;

        messageTwoSeenActionResponse = doPost(responderPerformActionUrl, markMessageReadRequest);

        Thread.sleep(1000);

        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def responderRequestDataMessageTwoRead = doGet("${baseUrl}/rs/patronrequests/${responderRequestId}");

        then:
        assert(firstMessageFound);
        assert(secondMessageFound);
        assert(responderRequestDataMessageOne.lastUpdated == responderRequestDataMessageOneAllRead.lastUpdated);
        assert(responderRequestDataMessageTwo.lastUpdated == responderRequestDataMessageTwoRead.lastUpdated);
    }

    void "test transition to terminal state after unfilled"(
            String serviceType,
            String deliveryMethod
    ) {
        String patronIdentifier = "ABJ-LJR-IGF-705";
        String requesterTenantId = "RSInstThree";
        String responderTenantId = "RSInstOne";
        String requestTitle = "Let It Die";
        String requestAuthor = "Dayzeez, Pop N.";
        String requestSymbol = "ISIL:RST3";
        String patronReference = "ref-" + patronIdentifier + randomCrap(6);
        String systemInstanceIdentifier = "131-484-333";

        when: "Do the thing"

        def changeSettingsResp = changeSettings(responderTenantId, [(SettingsData.SETTING_AUTO_RESPONDER_STATUS) : "off"]);

        Map request = [
                requestingInstitutionSymbol: requestSymbol,
                title                      : requestTitle,
                author                     : requestAuthor,
                patronIdentifier           : patronIdentifier,
                isRequester                : true,
                patronReference            : patronReference,
                systemInstanceIdentifier   : systemInstanceIdentifier,
                deliveryMethod             : deliveryMethod,
                serviceType                : serviceType,
                tags                       : ['RS-TERMINAL-TEST-1']
        ];

        setHeaders(['X-Okapi-Tenant': requesterTenantId]);
        doPost("${baseUrl}/rs/patronrequests".toString(), request);

        String requesterRequestId = waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);
        log.debug("Requester (terminal transition test) request id is ${requesterRequestId}");

        setHeaders(['X-Okapi-Tenant': requesterTenantId]);
        def requesterRequestData = doGet("${baseUrl}/rs/patronrequests/${requesterRequestId}");

        String responderRequestId = waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_IDLE);
        log.debug("Responder (terminal transition test) request id is ${responderRequestId}");

        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def responderRequestData = doGet("${baseUrl}/rs/patronrequests/${responderRequestId}");

        String responderPerformActionUrl = "${baseUrl}/rs/patronrequests/${responderRequestId}/performAction".toString();

        Map respondYesPayload = [
                action: "respondYes",
                "actionParams": [
                    "callnumber": "call number",
                    "pickLocation": "location",
                    "pickShelvingLocation": "shelving location",
                    "note": "Notes"
                ]
        ];

        log.debug("Posting 'respondYes' to responder at url ${responderPerformActionUrl}");
        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def respondYesActionResponse = doPost(responderPerformActionUrl, respondYesPayload);

        waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_NEW_AWAIT_PULL_SLIP);

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY);

        //Answer cannot supply
        Map cannotSupplyPayload = [
                action: "supplierCannotSupply",
                actionParams: [
                        reason: "missing",
                        note: "dog ate it"
                ]
        ];

        log.debug("Posting 'supplierCannotSupply' to responder at url ${responderPerformActionUrl}");
        setHeaders(['X-Okapi-Tenant': responderTenantId]);
        def cannotSupplyActionResponse = doPost(responderPerformActionUrl, cannotSupplyPayload);

        waitForRequestState(responderTenantId, 10000, patronReference, Status.RESPONDER_UNFILLED);

        waitForRequestState(requesterTenantId, 10000, patronReference, Status.PATRON_REQUEST_END_OF_ROTA);


        then:
        assert(true);

        where:
        deliveryMethod | serviceType
        "Copy"         | "URL"
        null           | null

    }

    @Ignore //Cannot get the creation of a past-dated request to work
    void "Test stale request timers"(
            String tenantId
    ) {
        when:
        final Duration durationOneWeek = new Duration(-1, 7, 0);
        DateTime createDate = (new DateTime(TimeZone.getTimeZone("UTC"), System.currentTimeMillis())).startOfDay();
        createDate = createDate.addDuration(durationOneWeek);
        String tenantString = tenantId.toLowerCase() + "_mod_rs";
        String requestId;
        Long dateStamp;
        //TimerCheckForStaleSupplierRequestsService timerService;
        String timerBeanName = "timer" + "CheckForStaleSupplierRequests" + "Service";
        def timerService = Holders.grailsApplication.mainContext.getBean(timerBeanName);
        changeSettings(tenantId, [(SettingsData.SETTING_STALE_REQUEST_1_ENABLED) : "yes"]);
        changeSettings(tenantId, [(SettingsData.SETTING_STALE_REQUEST_2_DAYS) : 3]);
        PatronRequest newRequest;
        Tenants.withId(tenantString) {

            autoTimestampEventListener.withoutDateCreated(PatronRequest, {
                newRequest = new PatronRequest(dateCreated: new Date(createDate.getTimestamp()));
                newRequest.state = Status.lookup(Status.RESPONDER_IDLE);
                newRequest.stateModel = StateModel.lookup(StateModel.MODEL_RESPONDER);
                newRequest.isRequester = false;
                newRequest.save(flush:true);
                requestId = newRequest.id;
            });
            //newRequest.dateCreated = new Date(createDate.getTimestamp());
            //newRequest.save(flush:true)

        }

        Tenants.withId(tenantString) {
            timerService.performTask(tenantId, null);
        }

        then:
        assert(requestId != null);
        assert(newRequest.dateCreated == new Date(createDate.getTimestamp()))
        assert(newRequest.state.getCode() == Status.RESPONDER_NOT_SUPPLIED);

        where:
        tenantId | _
        "RSInstThree" | _

    }
 }
