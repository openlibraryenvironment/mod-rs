package org.olf

import grails.testing.mixin.integration.Integration
import grails.transaction.*
import static grails.web.http.HttpHeaders.*
import static org.springframework.http.HttpStatus.*
import spock.lang.*
import geb.spock.*
import groovy.util.logging.Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.k_int.okapi.OkapiHeaders
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import spock.lang.Shared
import grails.gorm.multitenancy.Tenants
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.NamingAuthority
import com.k_int.web.toolkit.testing.HttpSpec
import grails.databinding.SimpleMapDataBindingSource
import grails.web.databinding.GrailsWebDataBinder
import org.olf.rs.EventPublicationService
import org.grails.orm.hibernate.HibernateDatastore
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import org.olf.rs.EmailService
import org.olf.rs.HostLMSService
import org.olf.rs.HostLMSLocation
import org.olf.rs.HostLMSShelvingLocation
import org.olf.rs.lms.HostLMSActions
import org.olf.rs.PatronRequest
import org.olf.rs.routing.StaticRouterService
import org.olf.rs.routing.RankedSupplier
import org.olf.rs.Z3950Service

/**
 * This sequence of tests centre around libraries who are members of a fictional resource sharing consortia called DCBNET.
 * the institution codes are DST1, DST2 and DST3
 */
@Slf4j
@Integration
@Stepwise
class DCBLifecycleSpec extends HttpSpec {

  // Warning: You will notice that these directory entries carry and additional customProperty: AdditionalHeaders
  // When okapi fronts the /rs/externalApi/iso18626 endpoint it does so through a root path like 
  // _/invoke/tenant/TENANT_ID/rs/externalApi/iso18626 - it then calls the relevant path with the TENANT_ID as a header
  // Because we want our tests to run without an OKAPI, we need to supply the tenant-id that OKAPI normally would and that
  // is the function of the AdditionalHeaders custom property here
  @Shared
  private static List<Map> DIRECTORY_INFO = [
    [ id:'DCB-T-D-0001', name: 'DCBInstOne', slug:'DCB_INST_ONE',     symbols: [[ authority:'ISIL', symbol:'DST1', priority:'a'] ],
      services:[
        [
          slug:'DCBInstOne_ISO18626',
          service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
          customProperties:[ 
            'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
            'AdditionalHeaders':['X-Okapi-Tenant:DCBInstOne'],
            'RSContextPreference': [ 'DCBNET' ]
          ],
          status: 'managed'
        ]
      ]
    ],
    [ id:'DCB-T-D-0002', name: 'DCBInstTwo', slug:'DCB_INST_TWO',     symbols: [[ authority:'ISIL', symbol:'DST2', priority:'a'] ],
      services:[
        [
          slug:'DCBInstTwo_ISO18626',
          service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
          customProperties:[ 
            'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
            'AdditionalHeaders':['X-Okapi-Tenant:DCBInstTwo'],
            'RSContextPreference': [ 'DCBNET' ]
          ]
        ]
      ]
    ],
    [ id:'DCB-T-D-0003', name: 'DCBInstThree', slug:'DCB_INST_THREE', symbols: [[ authority:'ISIL', symbol:'DST3', priority:'a'] ],
      services:[
        [
          slug:'DCBInstThree_ISO18626',
          service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/externalApi/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
          customProperties:[ 
            'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'],
            'AdditionalHeaders':['X-Okapi-Tenant:DCBInstThree'],
            'RSContextPreference': [ 'DCBNET' ]
          ]
        ]
      ]
    ]
  ]

  @Shared
  private static Map testctx = [
    request_data:[:]
  ]

  def grailsApplication
  EventPublicationService eventPublicationService
  GrailsWebDataBinder grailsWebDataBinder
  HibernateDatastore hibernateDatastore
  DataSource dataSource
  EmailService emailService
  HostLMSService hostLMSService
  StaticRouterService staticRouterService
  Z3950Service z3950Service

  @Value('${local.server.port}')
  Integer serverPort


  def setupSpec() {
    httpClientConfig = {
      client.clientCustomizer { HttpURLConnection conn ->
        conn.connectTimeout = 5000
        conn.readTimeout = 25000
      }
    }

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

  void "Attempt to delete any old tenants"(tenantid, name) {
    when:"We post a delete request"
      try {
        setHeaders(['X-Okapi-Tenant': tenantid, 'accept': 'application/json; charset=UTF-8'])
        def resp = doDelete("${baseUrl}_/tenant".toString(),null)
      }
      catch ( Exception e ) {
        // If there is no TestTenantG we'll get an exception here, it's fine
      }

    then:"Any old tenant removed"
      1==1

    where:
      tenantid | name
      'DCBInstOne' | 'DCBInstOne'
      'DCBInstTwo' | 'DCBInstTwo'
      'DCBInstThree' | 'DCBInstThree'
  }

  void "Set up test tenants "(tenantid, name) {
    when:"We post a new tenant request to the OKAPI controller"

      log.debug("Post new tenant request for ${tenantid} to ${baseUrl}_/tenant");

      setHeaders([
                   'X-Okapi-Tenant': tenantid,
                   'X-Okapi-Token': 'dummy',
                   'X-Okapi-User-Id': 'dummy',
                   'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
                 ])
      // post to tenant endpoint
      // doPost(url,jsondata,params,closure)
      def resp = doPost("${baseUrl}_/tenant".toString(), ['parameters':[[key:'loadSample', value:'true'],[key:'loadReference',value:'true']]]);

      // Give the various jobs time to finish their work.
      Thread.sleep(5000)
    log.debug("Got response for new tenant: ${resp}");
    then:"The response is correct"
      resp != null;

    where:
      tenantid | name
      'DCBInstOne'   | 'DCBInstOne'
      'DCBInstTwo'   | 'DCBInstTwo'
      'DCBInstThree' | 'DCBInstThree'
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
    tenant_id | dirents
    'DCBInstOne' | DIRECTORY_INFO
    'DCBInstTwo' | DIRECTORY_INFO
    'DCBInstThree' | DIRECTORY_INFO
  }

  /**
   * Set up a resource sharing context called DCBNET
   */ 
  void "test API for creating resource sharing contexts #tenant_id"(String tenant_id) {
    when:"We post to the shelvingLocations endpoint for tenant"
      setHeaders([
                   'X-Okapi-Tenant': tenant_id
                 ])
      def resp = doPost("${baseUrl}rs/contexts".toString(),
                        [
                          context:'DCBNET', 
                          sharedIndexType:'ReshareDCB',
                          protocol:'ISO18626'
                        ])
    then:"Created"
      resp != null;
      log.debug("Got create resource sharing context response: ${resp}");
    where:
      tenant_id | _
      'DCBInstOne' | _
  }

  /** Grab the settings for each tenant so we can modify them as needeed and send back,
   *  then work through the list posting back any changes needed for that particular tenant in this testing setup
   *  for now, disable all auto responders
   *  N.B. Unlike RS Lifecycle test, here we are going to rely upon the RSContextPreference value from the directory to force us into
   *  a different implementation of a shared index. In TEST the ReShareDCBSI will be replaced with a MOCK implementation that returns
   *  static values which reflect the use case here.
   */
  void "Configure Tenants for Mock Lending"(String tenant_id, Map changes_needed) {
    when:"We fetch the existing settings for ${tenant_id}"
      println("Post settings here");
      // RequestRouter = Static
      setHeaders([
                   'X-Okapi-Tenant': tenant_id,
                   'X-Okapi-Token': 'dummy',
                   'X-Okapi-User-Id': 'dummy',
                   'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
                ])
      def resp = doGet("${baseUrl}rs/settings/appSettings", [ 'max':'100', 'offset':'0'])
      if ( changes_needed != null ) {
        resp.each { setting ->
          // log.debug("Considering updating setting ${setting.id}, ${setting.section} ${setting.key} (currently = ${setting.value})");
          if ( changes_needed.containsKey(setting.key) ) {
            def new_value = changes_needed[setting.key];
            // log.debug("Post update to ${setting} ==> ${new_value}");
            setting.value = new_value;
            def update_setting_result = doPut("${baseUrl}rs/settings/appSettings/${setting.id}".toString(), setting);
            log.debug("Result of settings update: ${update_setting_result}");
          }
        }
      }

    then:"Tenant is configured"
      1==1

    where:
      tenant_id       | changes_needed
      'DCBInstOne'    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'reshareDcb'] // Will be replaced with ReshareDcbRouter
      'DCBInstTwo'    | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'reshareDcb']
      'DCBInstThree'  | [ 'auto_responder_status':'off', 'auto_responder_cancel': 'off', 'routing_adapter':'reshareDcb']

  }

  void "Send request "(String tenant_id,
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

      // This will fail initially until we have done the necessary work to find shared index config from the directory
      String peer_request = waitForRequestState(peer_tenant, 10000, p_patron_reference, 'RES_IDLE')
      log.debug("Created new request for with-rota test case 1. REQUESTER ID is : ${this.testctx.request_data[p_patron_reference]}")
      log.debug("                                               RESPONDER ID is : ${peer_request}");


    then:"Check the return value"
      assert this.testctx.request_data[p_patron_reference] != null;
      assert peer_request != null

    where:
      tenant_id    | peer_tenant  | p_title               | p_author         | p_systemInstanceIdentifier | p_patron_id | p_patron_reference         | requesting_symbol | tags
      'DCBInstOne' | 'DCBInstTwo' | 'Platform For Change' | 'Beer, Stafford' | '1234-5678-9123-4577'      | '1234-5679' | 'DCB-LIFECYCLE-TEST-00002' | 'ISIL:DST1'       | [ 'DCB-TESTCASE-2' ]
  }

}
