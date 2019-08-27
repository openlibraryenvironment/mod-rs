package org.olf.rs

import grails.testing.mixin.integration.Integration
import grails.transaction.*
import static grails.web.http.HttpHeaders.*
import static org.springframework.http.HttpStatus.*
import spock.lang.*
import geb.spock.*
import grails.plugins.rest.client.RestBuilder
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

// Added for conditions.eventually
// def conditions = new PollingConditions(timeout: 30)
// conditions.eventually { assert c1 == c2 }
import spock.util.concurrent.PollingConditions



@Slf4j
@Integration
@Stepwise
class RSLifecycleSpec extends GebSpec {

  @Shared
  private Map test_info = [:]

  // Auto injected by spring
  def grailsApplication
  EventPublicationService eventPublicationService
  GrailsWebDataBinder grailsWebDataBinder

  String created_request_id = null;

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
      'TestTenantG' | 'TestTenantG'
  }

  void "Test eventing"(tenant_id, entry_id, entry_uri) {
    when:"We emit a kafka event"
      logger.debug("Publish ${entry_uri}");
      eventPublicationService.publishAsJSON('modDirectory-entryChange-'+tenant_id, 
                                            java.util.UUID.randomUUID().toString(), 
                                            [ 'test': 'test' ] )

    then:"The response is correct"

    where:
      tenant_id | entry_id | entry_uri
      'TestTenantG' | 'TNS' | 'https://raw.githubusercontent.com/openlibraryenvironment/mod-directory/master/seed_data/TheNewSchool.json'
      'TestTenantG' | 'AC' | 'https://raw.githubusercontent.com/openlibraryenvironment/mod-directory/master/seed_data/AlleghenyCollege.json'
      'TestTenantG' | 'DIKU' | 'https://raw.githubusercontent.com/openlibraryenvironment/mod-directory/master/seed_data/DIKU.json'
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
      'TestTenantG' | [ id:'RS-T-D-0001', name: 'A Test entry' ]
  }

  void "Create a new request"(tenant_id, p_title, p_patron_id) {
    when:"post new request"
      logger.debug("Create a new request ${tenant_id} ${p_title} ${p_patron_id}");
      def resp = restBuilder().post("${baseUrl}/rs/patronrequests") {
        header 'X-Okapi-Tenant', tenant_id
        contentType 'application/json; charset=UTF-8'
        accept 'application/json; charset=UTF-8'
        authHeaders.rehydrate(delegate, owner, thisObject)()
        json {
          title=p_title
          patronReference=p_patron_id
        }
      }
      // logger.debug("Response: RESP:${resp} JSON:${resp.json}");
      // Stash the ID
      this.created_request_id = resp.json.id
      

    then:"Check the return value"
      resp.status == CREATED.value()
      created_request_id != null;
   
    where:
      tenant_id | p_title | p_patron_id
      'TestTenantG' | 'Brain of the firm' | '1234-5678'
  }

  void "Wait for the new request to become validated"(tenant_id, ref) {

    boolean completed = false;

    when:"post new request"
      Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {

        int i = 0;
        while ( i < 5 ) {
          Thread.sleep(1000);
          def r = PatronRequest.executeQuery('select pr.id, pr.title, pr.state.code from PatronRequest as pr');
          logger.debug("Current requests ${r}");
          i++;
          completed=true;
        }
      }

    then:"Check the return value"
      completed == true

    where:
      tenant_id|ref
      'TestTenantG' | 'RS-T-D-0001'
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
      'TestTenantG' | 'note'
  }

   RestBuilder restBuilder() {
        new RestBuilder()
    }

}

