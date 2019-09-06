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
import java.time.LocalDateTime

@Integration
@Rollback
@Slf4j
@Stepwise
class ShipmentSpec extends GebSpec {


  static Map request_data = [:];

  final Closure authHeaders = {
    header OkapiHeaders.TOKEN, 'dummy'
    header OkapiHeaders.USER_ID, 'dummy'
    header OkapiHeaders.PERMISSIONS, '[ "rs.admin", "rs.user", "rs.own.read", "rs.any.read"]'
  }

  final static Logger logger = LoggerFactory.getLogger(ShipmentSpec.class);

  def setup() {
  }

  def cleanup() {
  }



  //Set up a tenant to run shipping tests on
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
    'RSShipTenantA' | 'RSShipTenantA'
  }


  void "Create a new request to test shipping on"(tenant_id, p_title, p_patron_id) {
    when:"post new request"
    logger.debug("Create a new request ${tenant_id} ${p_title} ${p_patron_id}");

    def req_json_data = [
      title: p_title,
      patronReference:'SHIP-TESTCASE-1',
      patronIdentifier:p_patron_id,
      isRequester:true,
      tags: ['SHIP-TESTCASE-1' ]]

    String json_payload = new groovy.json.JsonBuilder(req_json_data).toString()

    RestResponse resp = restBuilder().post("${baseUrl}/rs/patronrequests") {
      header 'X-Okapi-Tenant', tenant_id
      contentType 'application/json; charset=UTF-8'
      accept 'application/json; charset=UTF-8'
      authHeaders.rehydrate(delegate, owner, thisObject)()
      json json_payload
    }
    logger.debug("CreateReqForShipTest1 -- Response: RESP:${resp} JSON:${resp.json.id}");

    // Stash the ID
    this.request_data['shipping test case 1'] = resp.json.id
    logger.debug("Created new patron request for shipping test case 1. ID is : ${request_data['shipping test case 1']}")


    then:"Check the return value"
    resp.status == CREATED.value()
    assert request_data['shipping test case 1'] != null;

    where:
    tenant_id | p_title | p_patron_id
    'RSShipTenantA' | 'The Arithmetic of Elliptic Curves' | '0123-9999'
  }




  void "Test creation of shipping item for request through domain classes"(tenant_id, p_ref) {
    def currentTime = LocalDateTime.now()

    when:
    logger.debug("Creating a new shipment for tenant:${tenant_id}, patron request: ${p_ref}");

    Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {

      def s = new Shipment(
          shipDate: currentTime
          ).save(flush:true, failOnError:true)
      logger.debug("New shipment created");

      logger.debug("Creating a new shipment item for patron request: ${p_ref}");
      def si = new ShipmentItem(
          isReturning: false,
          patronRequest: PatronRequest.findByPatronReference('SHIP-TESTCASE-1'),
          shipment: s
          ).save(flush: true, failOnError:true)
      logger.debug("New shipment item created");
    }

    then:
      Tenants.withId(tenant_id.toLowerCase()+'_mod_rs') {
        logger.debug("Check that that actually created items")
        logger.debug("There are ${ShipmentItem.count()} shipment items in the system.")
        ShipmentItem.count() == 1
        logger.debug("There are ${Shipment.count()} shipments in the system.")
        Shipment.count() == 1
      }


    where:
    tenant_id | p_ref
    'RSShipTenantA' | 'SHIP-TESTCASE-1'
  }


  
  void "Create a new shipment with some shipment items through HTML requests"(tenant_id) {

    def currentTime = LocalDateTime.now()

    when:"post new request"
      logger.debug("Create a new shipment ${tenant_id}");
      String str_current_time = '2019-01-01'

      def ship_json_data = [
        shipDate: str_current_time
      ]

      String json_payload = new groovy.json.JsonBuilder(ship_json_data).toString()

      def resp = restBuilder().post("${baseUrl}/rs/shipments") {
        header 'X-Okapi-Tenant', tenant_id
        contentType 'application/json; charset=UTF-8'
        accept 'application/json; charset=UTF-8'
        authHeaders.rehydrate(delegate, owner, thisObject)()
        json json_payload
      }
      logger.debug("Response: RESP:${resp} JSON:${resp.json}");
      // Stash the ID
      this.request_data['shipping test case 2'] = resp.json.id
      logger.debug("Created new shipment for tenant: ${tenant_id}. ID is ${request_data['shipping test case 2']}")


    then:"Check the return value"
      logger.debug("Checking the shipment made it to the database")
      resp.status == CREATED.value()
      /* logger.debug("resp status: ${resp.status}")
      logger.debug("CREATED value: ${CREATED.value()}")
      1==1 */


      
      assert request_data['shipping test case 2'] != null;

    where:
      tenant_id = 'RSShipTenantA'
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
    'RSShipTenantA' | 'note'
  }

  RestBuilder restBuilder() {
    new RestBuilder()
  }

}
