package org.olf

import grails.testing.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*
import geb.spock.*
import groovy.util.logging.Slf4j
import spock.lang.Shared
import grails.gorm.multitenancy.Tenants
import org.springframework.beans.factory.annotation.Value
import org.olf.rs.EmailService
import org.olf.rs.sharedindex.JiscDiscoverSharedIndexService;
import com.k_int.web.toolkit.testing.HttpSpec

@Slf4j
@Integration
@Stepwise
class JiscDiscoverLifecycleSpec extends HttpSpec {
  
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
  JiscDiscoverSharedIndexService jiscDiscoverSharedIndexService;


  @Shared
  private static Map testctx = [
    request_data:[:]
  ]

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
      'RSInstFour' | 'RSInstFour'
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
      'RSInstFour' | 'RSInstFour'
  }



  void "Test Library Hub Discover Lookup"() {
    when: "we try to look up an item by id"
      List lookup_result = null;
      Tenants.withId('RSInstFour_mod_rs'.toLowerCase()) {
        // In a test profile, this will invoke the mock provider and give back static data - check that works first
        lookup_result = jiscDiscoverSharedIndexService.findAppropriateCopies([systemInstanceIdentifier:'3568439'])
      }

    then: "service returns an appropriate record"  
      log.debug("Lookup result: ${lookup_result}");
      lookup_result.size() == 33
  }

  void "Test record attachment"() {
    when: "we try to look up an item by id"
      List lookup_result = null;
      Tenants.withId('RSInstFour_mod_rs'.toLowerCase()) {
        // In a test profile, this will invoke the mock provider and give back static data - check that works first
        lookup_result = jiscDiscoverSharedIndexService.fetchSharedIndexRecords([systemInstanceIdentifier:'3568439'])
      }

    then: "service returns an appropriate record"  
      log.debug("Lookup result: ${lookup_result}");
      lookup_result.size() == 1
  }



}
