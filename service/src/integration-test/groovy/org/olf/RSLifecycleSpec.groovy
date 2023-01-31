package org.olf

import java.text.SimpleDateFormat;

import javax.sql.DataSource

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.grails.orm.hibernate.HibernateDatastore
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.rs.EmailService
import org.olf.rs.EventPublicationService
import org.olf.rs.HostLMSLocation
import org.olf.rs.HostLMSLocationService
import org.olf.rs.HostLMSService
import org.olf.rs.HostLMSShelvingLocation
import org.olf.rs.PatronRequest
import org.olf.rs.Z3950Service
import org.olf.rs.dynamic.DynamicGroovyService;
import org.olf.rs.lms.HostLMSActions
import org.olf.rs.routing.RankedSupplier
import org.olf.rs.routing.StaticRouterService
import org.olf.rs.statemodel.Status;

import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.web.databinding.GrailsWebDataBinder
import grails.web.http.HttpHeaders;
import groovy.util.logging.Slf4j
import spock.lang.*

@Slf4j
@Integration
@Stepwise
class RSLifecycleSpec extends TestBase {

    // The scenario details that are maintained between tests
    private static final String SCENARIO_PATRON_REFERENCE = "scenario-patronReference";
    private static final String SCENARIO_REQUESTER_ID = "scenario-requesterId";
    private static final String SCENARIO_RESPONDER_ID = "scenario-responderId";

  private static String LONG_300_CHAR_TITLE = '123456789A123456789B123456789C123456789D123456789E123456789F123456789G123456789H123456789I123456789J123456789k123456789l123456789m123456789n123456789o123456789p123456789q123456789r123456789s123456789t123456789U123456789V123456789W123456789Y123456789Y12345XXXXX'
  private SimpleDateFormat scenarioDateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS");

  // Warning: You will notice that these directory entries carry and additional customProperty: AdditionalHeaders
  // When okapi fronts the /rs/externalApi/iso18626 endpoint it does so through a root path like
  // _/invoke/tenant/TENANT_ID/rs/externalApi/iso18626 - it then calls the relevant path with the TENANT_ID as a header
  // Because we want our tests to run without an OKAPI, we need to supply the tenant-id that OKAPI normally would and that
  // is the function of the AdditionalHeaders custom property here
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

  def grailsApplication
  DynamicGroovyService dynamicGroovyService;
  EventPublicationService eventPublicationService
  GrailsWebDataBinder grailsWebDataBinder
  HibernateDatastore hibernateDatastore
  DataSource dataSource
  EmailService emailService
  HostLMSService hostLMSService
  HostLMSLocationService hostLMSLocationService
  StaticRouterService staticRouterService
  Z3950Service z3950Service

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

  def cleanup() {
  }

  // For the given tenant, block up to timeout ms until the given request is found in the given state
  private String waitForRequestState(String tenant, long timeout, String patron_reference, String required_state) {
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
                         'match':'patronReference',
                         'term':patron_reference
                       ])
      if ( resp?.size() == 1 ) {
        request_id = resp[0].id
        request_state = resp[0].state?.code
      }

      if ( required_state != request_state ) {
        // Request not found OR not yet in required state
        log.debug("Not yet found.. sleeping");
        Thread.sleep(1000);
      }
      elapsed = System.currentTimeMillis() - start_time
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
      name      | should_be_found
      'alma'    | true
      'aleph'   | true
      'wms'     | true
      'default' | true
      'manual'  | true
      'folio'   | true
      'symphony'| true
      'sierra'  | true
      'wibble'  | false
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

  /** Grab the settings for each tenant so we can modify them as needeed and send back,
   *  then work through the list posting back any changes needed for that particular tenant in this testing setup
   *  for now, disable all auto responders
   *  N.B. that the test "Send request using static router" below RELIES upon the static routes assigned to RSInstOne.
   *  changing this data may well break that test.
   */
  void "Configure Tenants for Mock Lending"(String tenant_id, Map changes_needed) {
    when:"We fetch the existing settings for ${tenant_id}"
        changeSettings(tenant_id, changes_needed);

    then:"Tenant is configured"
      1==1

    where:
      tenant_id      | changes_needed
      'RSInstOne'    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST3,SYMBOL:ISIL:RST2' ]
      'RSInstTwo'    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST1,SYMBOL:ISIL:RST3' ]
      'RSInstThree'  | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'static', 'static_routes':'SYMBOL:ISIL:RST1' ]

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

  void "test determineBestLocation for LMS adapters"() {
    when:"We mock z39 and run determineBestLocation"
      z3950Service.metaClass.query = { String query, int max = 3, String schema = null -> new XmlSlurper().parseText(new File("src/test/resources/zresponsexml/${zResponseFile}").text) };
      def result = [:];
      Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {
        // perhaps generalise this to set preferences per test-case, for now we're just using it to see a temporaryLocation respected
        def nonlending = hostLMSLocationService.ensureActive('BASS, Lower Level, 24-Hour Reserve','');
        nonlending.setSupplyPreference(-1);

        def actions = hostLMSService.getHostLMSActionsFor(lms);
        def pr = new PatronRequest(supplierUniqueRecordId: '123');
        result['viaId'] = actions.determineBestLocation(pr);
        pr = new PatronRequest(isbn: '123');
        result['viaPrefix'] = actions.determineBestLocation(pr);
        result['location'] = HostLMSLocation.findByCode(location);
        result['shelvingLocation'] = HostLMSShelvingLocation.findByCode(shelvingLocation);
      }

    then:"Confirm location and shelving location were created and properly returned"
      result?.viaId?.location == location;
      result?.viaPrefix?.location == location;
      result?.location?.code == location;
      result?.shelvingLocation?.code == shelvingLocation;

    where:
      tenant_id | lms | zResponseFile | location | shelvingLocation | _
      'RSInstThree' | 'alma' | 'alma-princeton.xml' | 'Firestone Library' | 'stacks: Firestone Library' | _
      'RSInstThree' | 'alma' | 'alma-princeton-notfound.xml' | null | null | _
      'RSInstThree' | 'alma' | 'alma-dickinson-multiple.xml' | null | null | _
      'RSInstThree' | 'horizon' | 'horizon-jhu.xml' | 'Eisenhower' | null | _
      'RSInstThree' | 'symphony' | 'symphony-stanford.xml' | 'SAL3' | 'STACKS' | _
      'RSInstThree' | 'voyager' | 'voyager-temp.xml' | null | null | _
  }

    /**
     * Important note for the scenario test case, as we are relying on the routing and directory entries that have been setup earlier
     * so if the scenario test is moved out we will also need to setup the directories and settings in that spec file as well
     */
    private void createScenarioRequest(String requesterTenantId, int scenarioNo, String patronIdentifier = null) {
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

        // Execute the action
        def actionResponse = doPost("${baseUrl}/rs/patronrequests/${actionRequestId}/performAction".toString(), jsonAction);
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
        String patronIdentifier = null
    ) {
        String actionResponse = null;

        try {
            log.debug("Performing action for scenario " + scenario + " using file " + actionFile + ", expected requester status " + requesterStatus + ", expected responder status " + responderStatus);
            // Are we creating a fresh request
            if (responderTenantId == null) {
                // So we need to create  new request
                createScenarioRequest(requesterTenantId, scenario, patronIdentifier);
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
            log.error("Exceptione Performing action for scenario " + scenario + " using file " + actionFile + ", expected requester status " + requesterStatus + ", expected responder status " + responderStatus, e);
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
        String expectedActionResponse
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
                newResponderStatus
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
            requesterTenantId | responderTenantId | scenario | isRequesterAction | actionFile                          | requesterStatus                                   | responderStatus                             | newResponderTenant | newResponderStatus    | expectedActionResponse
            "RSInstOne"       | null              | 1        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null
            "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierConditionalSupply.json"    | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | true              | "requesterAgreeConditions.json"     | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | true              | "requesterCancel.json"              | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierRespondToCancelNo.json"    | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierAddCondition.json"         | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierMarkConditionsAgreed.json" | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierPrintPullSlip.json"        | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_AWAIT_PICKING              | null               | null                  | "{status=true}"
            "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierCheckInToReshare.json"     | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_AWAIT_SHIP                 | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierMarkShipped.json"          | Status.PATRON_REQUEST_SHIPPED                     | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | true              | "requesterReceived.json"            | Status.PATRON_REQUEST_CHECKED_IN                  | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 1        | true              | "patronReturnedItem.json"           | Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING    | Status.RESPONDER_ITEM_SHIPPED               | null               | null                  | "{status=true}"
            "RSInstOne"       | "RSInstThree"     | 1        | true              | "shippedReturn.json"                | Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER         | Status.RESPONDER_ITEM_RETURNED              | null               | null                  | "{status=true}"
            "RSInstOne"       | "RSInstThree"     | 1        | false             | "supplierCheckOutOfReshare.json"    | Status.PATRON_REQUEST_REQUEST_COMPLETE            | Status.RESPONDER_COMPLETE                   | null               | null                  | "{status=true}"
            "RSInstOne"       | null              | 2        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null
            "RSInstOne"       | "RSInstThree"     | 2        | false             | "supplierAnswerYes.json"            | Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY           | Status.RESPONDER_NEW_AWAIT_PULL_SLIP        | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 2        | false             | "supplierCannotSupply.json"         | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | Status.RESPONDER_UNFILLED                   | "RSInstTwo"        | Status.RESPONDER_IDLE | "{}"
            "RSInstOne"       | null              | 3        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null
            "RSInstOne"       | "RSInstThree"     | 3        | false             | "supplierConditionalSupply.json"    | Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED | Status.RESPONDER_PENDING_CONDITIONAL_ANSWER | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 3        | true              | "requesterRejectConditions.json"    | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 3        | false             | "supplierRespondToCancelYes.json"   | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | Status.RESPONDER_CANCELLED                  | "RSInstTwo"        | Status.RESPONDER_IDLE | "{}"
            "RSInstOne"       | null              | 4        | true              | null                                | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | null                                        | "RSInstThree"      | Status.RESPONDER_IDLE | null
            "RSInstOne"       | "RSInstThree"     | 4        | true              | "requesterCancel.json"              | Status.PATRON_REQUEST_CANCEL_PENDING              | Status.RESPONDER_CANCEL_REQUEST_RECEIVED    | null               | null                  | "{}"
            "RSInstOne"       | "RSInstThree"     | 4        | false             | "supplierRespondToCancelYes.json"   | Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER    | Status.RESPONDER_CANCELLED                  | null               | null                  | null
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
            changeSettings(tenantId, [ state_model_responder : stateModel ], true);

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

    void "Configure Tenants for reporting"(String tenant_id, String key, String value) {
        when:"We fetch the existing settings for ${tenant_id}"
            List settings = changeSettings(tenant_id, [ (key) : value ]);
            def newSetting = settings.find { setting -> setting.key.equals(key) };
            log.debug("set key: " + key + ", to value: " + value);
            log.debug("Setting: " + settings.toString());
            log.debug("new setting: " + newSetting.toString());

        then:"Tenant is configured"
            assert(newSetting != null);
            assert(newSetting.value.equals(value));

        where:
            tenant_id     | key             | value
            'RSInstThree' | "S3SecretKey"   | "RESHARE_AGG_SECRET_KEY"
            'RSInstThree' | "S3Endpoint"    | "http://127.0.0.1:9010"
            'RSInstThree' | "S3AccessKey"   | "RESHARE_AGG_ACCESS_KEY"
            'RSInstThree' | "S3BucketName"  | "reshare-general"
            'RSInstThree' | "storageEngine" | "S3"

    }

    void "Upload_Download_File"(String tenantId, String fileContents, String fileContentType, String filename) {
        when:"We upload and then download a file"
            // Post to the upload end point
            // Build the http entity
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntityBuilder.addBinaryBody("file", fileContents.getBytes(), ContentType.create(fileContentType), filename);
            multipartEntityBuilder.addTextBody("fileType", "REPORT_DEFINITION");
            multipartEntityBuilder.addTextBody("description", "A simple test of file upload");
            HttpEntity httpEntity = multipartEntityBuilder.build();
            InputStream entityInputStream = httpEntity.getContent();
            String body = httpEntity.getContent().text;
            entityInputStream.close();

            // Now set the headers
            setHeaders([
                'X-Okapi-Tenant': tenantId,
                (HttpHeaders.ACCEPT) : 'application/json',
                (HttpHeaders.CONTENT_TYPE) : httpEntity.getContentType().value,
                (HttpHeaders.CONTENT_LENGTH) : httpEntity.getContentLength().toString()
            ]);
            def uploadResponse = doPost("${baseUrl}/rs/fileDefinition/fileUpload".toString(), null, null, {
                // Note: request is of type groovyx.net.http.HttpConfigs$BasicRequest
                request.setBody(body);
            });
            log.debug("Response from posting attachment: " + uploadResponse.toString());

            // If we successfully uploaded then attempt to download
            String dowloadedText = null;
            if (uploadResponse?.id != null) {
                // Fetch from the download end point
                setHeaders([
                    'X-Okapi-Tenant': tenantId,
                    (HttpHeaders.ACCEPT) : 'application/octet-stream',
                ]);
                def downloadResponse = doGet("${baseUrl}rs/fileDefinition/fileDownload/${uploadResponse.id}");
                if (downloadResponse instanceof String) {
                    // That is good we have the response we expected
                    dowloadedText = downloadResponse;
                }
//                log.debug("Download response is of type: " + downloadResponse.getClass().toString());
//                log.debug("Download response: " + downloadResponse);
            }

        then:"Check file has been uploaded correctly"
            assert(uploadResponse?.id != null);
            assert(fileContents.equals(dowloadedText));

        where:
            tenantId      | fileContents                                | fileContentType  | filename
            'RSInstThree' | "Will this manage to get uploaded properly" | "text/plain"     | "test.txt"
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
}
