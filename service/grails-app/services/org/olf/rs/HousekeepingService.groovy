package org.olf.rs

import java.sql.ResultSet

import javax.sql.DataSource

import org.grails.datastore.mapping.core.exceptions.ConfigurationException
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StateTransition;

import grails.core.GrailsApplication
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import com.k_int.okapi.OkapiTenantAdminService



/**
 * This service works at the module level, it's often called without a tenant context.
 */
public class HousekeepingService {

  // This was DataSource but I think this is actually a HibernateDataSource
        GrailsApplication grailsApplication
  OkapiTenantAdminService okapiTenantAdminService

  /**
   * This is called by the eventing mechanism - There is no web request context
   * this method is called after the schema for a tenant is updated.
   */
  @Subscriber('okapi:schema_update')
  public void onSchemaUpdate(tn, tid) {
    log.debug("HousekeepingService::onSchemaUpdate(${tn},${tid})")
    setupData(tn, tid);
  }

  /**
   * Put calls to estabish any required reference data in here. This method MUST be communtative - IE repeated calls must leave the 
   * system in the same state. It will be called regularly throughout the lifecycle of a project. It is common to see calls to
   * lookupOrCreate, or "upsert" type functions in here."
   */
  private void setupData(tenantName, tenantId) {
    log.info("HousekeepingService::setupData(${tenantName},${tenantId})");
    // Establish a database session in the context of the activated tenant. You can use GORM domain classes inside the closure
    Tenants.withId(tenantId) {
      Status.withNewTransaction { status ->
        // Status.lookupOrCreate('PatronRequest', 'IDLE');
        // Status.lookupOrCreate('PatronRequest', 'VALIDATED');
        // Status.lookupOrCreate('PatronRequest', 'SOURCING_ITEM')
        // Status.lookupOrCreate('PatronRequest', 'SUPPLIER_IDENTIFIED')
        // Status.lookupOrCreate('PatronRequest', 'RESPONDER_ERROR')            // Unspecified error from responder
        // Status.lookupOrCreate('PatronRequest', 'RESPONDER_NOT_SUPPLIED')     // Responder won't supply
        // Status.lookupOrCreate('PatronRequest', 'REQUEST_SENT_TO_SUPPLIER')
        // Status.lookupOrCreate('PatronRequest', 'ITEM_SHIPPED')
        // Status.lookupOrCreate('PatronRequest', 'BORROWING_LIBRARY_RECEIVED')
        // Status.lookupOrCreate('PatronRequest', 'AWAITING_RETURN_SHIPPING')
        // Status.lookupOrCreate('PatronRequest', 'BORROWER_RETURNED')
        // Status.lookupOrCreate('PatronRequest', 'REQUEST_COMPLETE')
        // Status.lookupOrCreate('PatronRequest', 'PENDING');
        // Status.lookupOrCreate('PatronRequest', 'WILL_SUPPLY');
        // Status.lookupOrCreate('PatronRequest', 'END_OF_ROTA');

        // Requester / Borrower State Model
        Status.lookupOrCreate('PatronRequest', 'REQ_IDLE', '0005', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_VALIDATED', '0010', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_INVALID_PATRON', '0011', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_SOURCING_ITEM', '0015', true)
        Status.lookupOrCreate('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED', '0020', true)
        Status.lookupOrCreate('PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER', '0025', true)
        Status.lookupOrCreate('PatronRequest', 'REQ_UNABLE_TO_CONTACT_SUPPLIER')
        Status.lookupOrCreate('PatronRequest', 'REQ_ITEM_SHIPPED', '0035', true)
        Status.lookupOrCreate('PatronRequest', 'REQ_BORROWING_LIBRARY_RECEIVED', '0040', true)
        Status.lookupOrCreate('PatronRequest', 'REQ_AWAITING_RETURN_SHIPPING', '0045', true)
        Status.lookupOrCreate('PatronRequest', 'REQ_BORROWER_RETURNED', '0050', true)
        Status.lookupOrCreate('PatronRequest', 'REQ_REQUEST_COMPLETE', '0055', true)
        Status.lookupOrCreate('PatronRequest', 'REQ_PENDING', '0060', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_WILL_SUPPLY', '0065', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_EXPECTS_TO_SUPPLY', '0070', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_UNFILLED', '0075', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_SHIPPED', '0076', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_END_OF_ROTA', '0080', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_ERROR', '9999', true);

        // Responder / Lender State Model
        Status.lookupOrCreate('Responder', 'RES_IDLE', '0005', true);
        // RequestAction.lookupOrCreate('Responder', 'RES_IDLE', 'Shipped');

        Status.lookupOrCreate('Responder', 'RES_NEW_AWAIT_PULL_SLIP', '0010', true);
        Status.lookupOrCreate('Responder', 'RES_AWAIT_PICKING', '0015', true);
        Status.lookupOrCreate('Responder', 'RES_CHECKED_IN_TO_RESHARE', '0020', true);
        Status.lookupOrCreate('Responder', 'RES_AWAIT_SHIP', '0021', true);
        Status.lookupOrCreate('Responder', 'RES_HOLD_PLACED', '0025', true);
        Status.lookupOrCreate('Responder', 'RES_UNFILLED', '0030', true);
        Status.lookupOrCreate('Responder', 'RES_NOT_SUPPLIED', '0035', true);
        Status.lookupOrCreate('Responder', 'RES_ITEM_SHIPPED', '0040', true);
        Status.lookupOrCreate('Responder', 'RES_ERROR', '9999', true);


        StateTransition.ensure( 'Responder', 'RES_NEW_AWAIT_PULL_SLIP', 'supplierPrintPullSlip')
        StateTransition.ensure( 'Responder', 'RES_NEW_AWAIT_PULL_SLIP', 'message')
        StateTransition.ensure( 'Responder', 'RES_AWAIT_PICKING', 'supplierCheckInToReshare')
        StateTransition.ensure( 'Responder', 'RES_AWAIT_PICKING', 'message')
        StateTransition.ensure( 'Responder', 'RES_CHECKED_IN_TO_RESHARE', 'supplierShip')
        StateTransition.ensure( 'Responder', 'RES_CHECKED_IN_TO_RESHARE', 'message')

        StateTransition.ensure( 'PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER', 'message')
        StateTransition.ensure( 'PatronRequest', 'REQ_SHIPPED', 'responderReceived')
      }

    }
  }


  /**
   *  Mod-RS needs some shared data to be able to route incoming messages to the appropriate tenant.
   *  This funcion creates a special shared schema that all tenants have access to. It is the place
   *  we register symbol -> tenant mappings.
   */
  public synchronized void ensureSharedSchema() {
    log.debug("make sure __global tenant is present");
    okapiTenantAdminService.enableTenant('__global',[:])
    log.debug("ensureSharedSchema completed");
  }

  public void ensureSharedConfig() {
  }


}
