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
import grails.plugins.rest.client.RestBuilder

@Slf4j
@Integration
@Stepwise
class RSNotSuppliedSpec extends GebSpec{

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

  final static Logger logger = LoggerFactory.getLogger(RSNotSuppliedSpec.class);

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
    'RSNotSuppTenantA' | 'RSNotSuppTenantA'
  }
  
  
  void "set Up Shared Data"(symbol, tenant_id) {
    
        logger.debug("Set up shared data");
    
        when:"We register the data mapping symbols to tenants"
         globalConfigService.registerSymbolForTenant(symbol, tenant_id);
          
        then:"We are able to resolve which tenant a symbol should be routed to"
          assert tenant_id == globalConfigService.getTenantForSymbol(symbol)
    
        where:
          symbol|tenant_id
          'RESHARE:RSA'|'RSNotSuppTenantA'
          'RESHARE:RSB'|'RSNotSuppTenantA'
      }
  
  
  void "Create a new request with a single Rota entry"(tenant_id, p_title, p_patron_id) {
    when:"post new request"
    logger.debug("Create a new request ${tenant_id} ${p_title} ${p_patron_id}");

    def req_json_data = [
      title: p_title,
      requestingInstitutionSymbol:'RESHARE:RSB',
      isRequester:true,
      patronReference:p_patron_id,
      rota:[[directoryId:'RESHARE:RSA', rotaPosition:"0"]],
      tags: [ 'RS-TESTCASE-2' ]
    ]

    String json_payload = new groovy.json.JsonBuilder(req_json_data).toString()

    def resp = restBuilder().post("${baseUrl}/rs/patronrequests") {
      header 'X-Okapi-Tenant', tenant_id
      contentType 'application/json; charset=UTF-8'
      accept 'application/json; charset=UTF-8'
      authHeaders.rehydrate(delegate, owner, thisObject)()
      json json_payload
    }
    logger.debug("Response: RESP:${resp} JSON:${resp.json}");
    // Stash the ID
    this.request_data['not supplied test case 1'] = resp.json.id
    logger.debug("${request_data['not supplied test case 1']}")


    then:"Check the return value"
    resp.status == CREATED.value()
    assert request_data['not supplied test case 1'] != null;

    where:
    tenant_id | p_title | p_patron_id
    'RSNotSuppTenantA' | 'Algebraic Topology' | '9876-5432'
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
      'RSNotSuppTenantA' | 'note'
  }
    
  RestBuilder restBuilder() {
    new RestBuilder()
  }
  
  
}
