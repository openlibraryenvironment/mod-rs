package org.olf.rs

import grails.testing.mixin.integration.Integration
import grails.transaction.*
import static grails.web.http.HttpHeaders.*
import static org.springframework.http.HttpStatus.*
import spock.lang.*
import geb.spock.*
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.util.logging.Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.k_int.okapi.OkapiHeaders
import spock.lang.Shared
import grails.gorm.multitenancy.Tenants

import grails.databinding.SimpleMapDataBindingSource
import grails.web.databinding.GrailsWebDataBinder
import org.olf.okapi.modules.directory.DirectoryEntry
import grails.gorm.multitenancy.Tenants
import javax.sql.DataSource
import org.grails.orm.hibernate.HibernateDatastore

@Slf4j
@Integration
@Stepwise
class ProtocolPeerSpec extends GebSpec {

  @Shared
  private Map test_info = [:]

  // Auto injected by spring
  def grailsApplication
  EventPublicationService eventPublicationService
  GrailsWebDataBinder grailsWebDataBinder
  HibernateDatastore hibernateDatastore
  DataSource dataSource
  GlobalConfigService globalConfigService

  static Map request_data = [:];

  final Closure authHeaders = {
    header OkapiHeaders.TOKEN, 'dummy'
    header OkapiHeaders.USER_ID, 'dummy'
    header OkapiHeaders.PERMISSIONS, '[ "rs.admin", "rs.user", "rs.own.read", "rs.any.read"]'
  }

  final static Logger logger = LoggerFactory.getLogger(RSLifecycleSpec.class);

  def setup() {
  }

  def cleanup() {
  }

  // Set up a new tenant called RSTestTenantA
  void "Set up test tenants "(tenantid, name) {
    when:"We post a new tenant request to the OKAPI controller"

    logger.debug("Post new tenant request for ${tenantid} to ${baseUrl}_/tenant");

    def resp = restBuilder().post("${baseUrl}_/tenant") {
      header 'X-Okapi-Tenant', tenantid
      authHeaders.rehydrate(delegate, owner, thisObject)()
    }

    then:"The response is correct"
    resp.status == CREATED.value()
    logger.debug("Post new tenant request for ${tenantid} to ${baseUrl}_/tenant completed");

    where:
    tenantid | name
    'PPTestTenantG' | 'PPTestTenantG'
    'PPTestTenantH' | 'PPTestTenantH'
  }

  /**
   * We want our integration tests to work without recourse to any other module - so we directly install
   * the test data needed here. 
   */
  void "Bootstrap directory data for integration tests"(tenant_id, entry) {
    when:"Load the default directory"

    Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {
      logger.debug("Sync directory entry ${entry}")
      def SimpleMapDataBindingSource source = new SimpleMapDataBindingSource(entry)
      DirectoryEntry de = new DirectoryEntry()
      grailsWebDataBinder.bind(de, source)
      de.save(flush:true, failOnError:true)
      logger.debug("Result of bind: ${de} ${de.id}");
    }

    then:"Test directory entries are present"
    1==1

    where:
    tenant_id | entry
    'PPTestTenantG' | [ id:'RS-T-D-0001', name: 'Allegheny College', slug:'Allegheny_College',
      symbols: [
        [ authority:'OCLC', symbol:'AVL', priority:'a'] ] //,
      // services:[
      //   [
      //     service:[ "name":"ReShare ISO18626 Service", "address":"https://localhost/reshare/iso18626", "type":"ISO18626", "businessFunction":"ILL" ],
      //     customProperties:[ "ILLPreferredNamespaces":["RESHARE", "PALCI", "IDS"] ]
      //   ]
      // ]
    ]
    'PPTestTenantG' | [ id:'RS-T-D-0002', name: 'The New School', slug:'THE_NEW_SCHOOL', symbols: [[ authority:'OCLC', symbol:'PPPA', priority:'a'] ]]
  }



  void "set Up Shared Data"(symbol, tenant_id) {

    logger.debug("Set up shared data");

    when:"We register the data mapping symbols to tenants"
     globalConfigService.registerSymbolForTenant(symbol, tenant_id);
      
    then:"We are able to resolve which tenant a symbol should be routed to"
      assert tenant_id == globalConfigService.getTenantForSymbol(symbol)

    where:
      symbol|tenant_id
      'OCLC:PPPA'|'PPTestTenantH'
      'OCLC:PPPB'|'PPTestTenantG'
  }

  void "Delete the tenants"(tenant_id, note) {

    expect:"post delete request to the OKAPI controller for "+tenant_id+" results in OK and deleted tennant"
    // Snooze
    try {
      Thread.sleep(1000);
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

    def resp = restBuilder().delete("$baseUrl/_/tenant") {
      header 'X-Okapi-Tenant', tenant_id
      authHeaders.rehydrate(delegate, owner, thisObject)()
    }

    logger.debug("completed DELETE request on ${tenant_id}");
    resp.status == NO_CONTENT.value()

    where:
    tenant_id | note
    'PPTestTenantG' | 'note'
    'PPTestTenantH' | 'note'
  }

  RestBuilder restBuilder() {
    new RestBuilder()
  }

}

