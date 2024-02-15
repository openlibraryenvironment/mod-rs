package org.olf

import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.web.databinding.GrailsWebDataBinder
import groovy.util.logging.Slf4j
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.rs.PatronRequest
import org.olf.rs.routing.RankedSupplier
import org.olf.rs.routing.StaticRouterService
import org.olf.rs.statemodel.StateModel
import org.olf.rs.statemodel.Status
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

    then:"Then expect result is returned"
      resolved_rota.size() == 2;
  }

    void "Test SLNP StateModel requester state transition by executed action"(
            String tenantId,
            String requestTitle,
            String requestAuthor,
            String requestSystemId,
            String requestPatronId,
            String requestSymbol,
            String initialState,
            String resultState) {
        when: "When initial state transitions to action result state"

        // Define headers
        def headers = [
                'X-Okapi-Tenant': tenantId,
                'X-Okapi-Token': 'dummy',
                'X-Okapi-User-Id': 'dummy',
                'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
        ]

        setHeaders(headers);

        // Create mock SLNP patron request
        PatronRequest slnpPatronRequest = new PatronRequest();

        // Set isRequester to true
        slnpPatronRequest.isRequester = true;

        // Create SLNP Requester State Model
        StateModel stateModel = new StateModel();
        stateModel.name = StateModel.MODEL_SLNP_REQUESTER;

        // Create Status with initial state and assign to StateModel
        Status initialStatus = new Status();
        initialStatus.code = initialState;
        stateModel.initialState = initialStatus

        // Set SLNP Requester StateModel and initial state
        slnpPatronRequest.stateModel = stateModel
        slnpPatronRequest.state = initialStatus;

        // Save the SLNP Patron request
        slnpPatronRequest = slnpPatronRequest.save(flush: true, failOnError: true);

        log.debug("Mocked SNLP Patron Request response: ${slnpPatronRequest} ID: ${slnpPatronRequest?.id}");

        // Validate initial state is correct
        RSLifecycleSpec.waitForRequestState(tenantId, 20000, requestPatronId, initialState);

        // Sleep
        Thread.sleep(2000);

        String jsonPayload = new File("src/integration-test/resources/scenarios/slnpRequesterSlnpISO18626Aborted.json").text;

        log.debug("jsonPayload: ${jsonPayload}");
        String performActionURL = "${baseUrl}/rs/patronrequests/${slnpPatronRequest.id}/performAction".toString();
        log.debug("Posting to performAction at $performActionURL");

        // Execute action
        doPost(performActionURL, jsonPayload);

        // Validate state transition
        RSLifecycleSpec.waitForRequestState(tenantId, 20000, requestPatronId, resultState);

        then: "Check values"
        assert true;

        where:
        tenantId    | requestTitle                      | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol | initialState    | resultState
        'RSInstOne' | 'Excellent Argument, However...'  | 'Mom, U.R.'   | '1234-5678-9123-4567' | '9876-1234'       | 'ISIL:RST1'   | 'SLNP_REQ_IDLE' | 'SLNP_REQ_ABORTED'
    }
}
