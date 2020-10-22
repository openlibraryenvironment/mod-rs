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
import spock.lang.Shared
import grails.gorm.multitenancy.Tenants
import org.olf.okapi.modules.directory.DirectoryEntry
import com.k_int.web.toolkit.testing.HttpSpec
import grails.databinding.SimpleMapDataBindingSource
import grails.web.databinding.GrailsWebDataBinder
import org.olf.rs.EventPublicationService
import org.grails.orm.hibernate.HibernateDatastore
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.olf.rs.EmailService

@Slf4j
@Integration
@Stepwise
class RSLifecycleSpec extends HttpSpec {
  

  // ToDo: **/address needs to have ${baseUrl} replaced with the actual value
  @Shared
  private static List<Map> DIRECTORY_INFO = [
    [ id:'RS-T-D-0001', name: 'RSInstOne', slug:'RS_INST_ONE',     symbols: [[ authority:'ISIL', symbol:'RST1', priority:'a'] ] 
    ],
    [ id:'RS-T-D-0002', name: 'RSInstTwo', slug:'RS_INST_TWO',     symbols: [[ authority:'ISIL', symbol:'RST2', priority:'a'] ] 
    ],
    [ id:'RS-T-D-0003', name: 'RSInstThree', slug:'RS_INST_THREE', symbols: [[ authority:'ISIL', symbol:'RST3', priority:'a'] ],
      services:[
        [
          slug:'RSInstThree_ISO18626',
          service:[ 'name':'ReShare ISO18626 Service', 'address':'${baseUrl}/rs/iso18626', 'type':'ISO18626', 'businessFunction':'ILL' ],
          customProperties:[ 'ILLPreferredNamespaces':['ISIL', 'RESHARE', 'PALCI', 'IDS'] ]
        ]
      ]
    ]
  ]

  @Shared
  private static Map testctx = [:]

  def grailsApplication
  EventPublicationService eventPublicationService
  GrailsWebDataBinder grailsWebDataBinder
  HibernateDatastore hibernateDatastore
  DataSource dataSource
  EmailService emailService

  @Value('${local.server.port}')
  Integer serverPort


  Closure authHeaders = {
    header OkapiHeaders.TOKEN, 'dummy'
    header OkapiHeaders.USER_ID, 'dummy'
    header OkapiHeaders.PERMISSIONS, '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
  }

  def setupSpec() {
    httpClientConfig = {
      client.clientCustomizer { HttpURLConnection conn ->
        conn.connectTimeout = 5000
        conn.readTimeout = 20000
      }
    }

  }

  def setup() {
    if ( testctx.initialised == null ) {
      log.debug("Inject actual runtime port number (${serverPort}) into directory entries (${baseUrl}) ");
      for ( Map entry: DIRECTORY_INFO ) {
        if ( entry.services != null ) {
          for ( Map svc: entry.services ) {
            svc.service.address = "${baseUrl}/rs/iso18626".toString()
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
      try {
        setHeaders(['X-Okapi-Tenant': tenantid])
        def resp = doDelete("${baseUrl}_/tenant".toString(),null)
      }
      catch ( Exception e ) {
        // If there is no TestTenantG we'll get an exception here, it's fine
      }

    then:"Any old tenant removed"
      1==1

    where:
      tenantid | name
      'RSInstOne' | 'RSInstOne'
      'RSInstTwo' | 'RSInstTwo'
      'RSInstThree' | 'RSInstThree'
  }

  // Set up a new tenant called RSTestTenantA
  void "Set up test tenants "(tenantid, name) {
    when:"We post a new tenant request to the OKAPI controller"

      log.debug("Post new tenant request for ${tenantid} to ${baseUrl}_/tenant");

      setHeaders(['X-Okapi-Tenant': tenantid])
      def resp = doPost("${baseUrl}_/tenant") {
        // header 'X-Okapi-Tenant', tenantid
        authHeaders.rehydrate(delegate, owner, thisObject)()
      }

    log.debug("Got response: ${resp}");
    then:"The response is correct"
      resp != null;

    where:
      tenantid | name
      'RSInstOne' | 'RSInstOne'
      'RSInstTwo' | 'RSInstTwo'
      'RSInstThree' | 'RSInstThree'
  }


  void "Bootstrap directory data for integration tests"(String tenant_id, List<Map> dirents) {
    when:"Load the default directory (test url is ${baseUrl})"

    Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {
      dirents.each { entry ->
        log.debug("Sync directory entry ${entry} - Detected runtime port is ${serverPort}")
        def SimpleMapDataBindingSource source = new SimpleMapDataBindingSource(entry)
        DirectoryEntry de = new DirectoryEntry()
        grailsWebDataBinder.bind(de, source)

        log.debug("Before save, ${de}, services:${de.services}");
        de.save(flush:true, failOnError:true)
        log.debug("Result of bind: ${de} ${de.id}");
      }
    }

    then:"Test directory entries are present"
    1==1

    where:
    tenant_id | dirents
    'RSInstOne' | DIRECTORY_INFO
    'RSInstTwo' | DIRECTORY_INFO
    'RSInstThree' | DIRECTORY_INFO
  }

}
