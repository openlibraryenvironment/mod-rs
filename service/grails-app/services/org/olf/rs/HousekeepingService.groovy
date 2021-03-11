package org.olf.rs

import java.sql.ResultSet

import javax.sql.DataSource

import org.grails.datastore.mapping.core.exceptions.ConfigurationException
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
import org.olf.rs.statemodel.Status;
import org.olf.rs.Counter;
import org.olf.rs.statemodel.StateTransition;
import org.olf.rs.statemodel.AvailableAction;

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

  private static String CANCEL_ACTION_CLOSURE = '{ ga, ras, pr, aa -> ras.requestCancel(pr) }'

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
        Status.lookupOrCreate('PatronRequest', 'REQ_IDLE', '0005', true, true);
        Status.lookupOrCreate('PatronRequest', 'REQ_VALIDATED', '0010', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_INVALID_PATRON', '0011', true, true);
        Status.lookupOrCreate('PatronRequest', 'REQ_SOURCING_ITEM', '0015', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED', '0020', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER', '0025', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_CONDITIONAL_ANSWER_RECEIVED', '0026', true, true);
        Status.lookupOrCreate('PatronRequest', 'REQ_CANCEL_PENDING', '0027', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_CANCELLED_WITH_SUPPLIER', '0028', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_UNABLE_TO_CONTACT_SUPPLIER');
        Status.lookupOrCreate('PatronRequest', 'REQ_OVERDUE', '0036', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_RECALLED', '0037', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_BORROWING_LIBRARY_RECEIVED', '0040', true, true);
        Status.lookupOrCreate('PatronRequest', 'REQ_AWAITING_RETURN_SHIPPING', '0045', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_SHIPPED_TO_SUPPLIER', '0046', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_BORROWER_RETURNED', '0050', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_REQUEST_COMPLETE', '0055', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_PENDING', '0060', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_WILL_SUPPLY', '0065', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_EXPECTS_TO_SUPPLY', '0070', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_UNFILLED', '0075', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_SHIPPED', '0076', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_CHECKED_IN', '0077', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_LOCAL_REVIEW', '0079', true);
        // This one doesn't appear to be in use
        // Status.lookupOrCreate('PatronRequest', 'REQ_AWAIT_RETURN_SHIPPING', '0078', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_END_OF_ROTA', '0080', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_CANCELLED', '9998', true);
        Status.lookupOrCreate('PatronRequest', 'REQ_ERROR', '9999', true, true);

        // Responder / Lender State Model
        Status.lookupOrCreate('Responder', 'RES_IDLE', '0005', true);
        Status.lookupOrCreate('Responder', 'RES_PENDING_CONDITIONAL_ANSWER', '0006', true);
        Status.lookupOrCreate('Responder', 'RES_NEW_AWAIT_PULL_SLIP', '0010', true);
        Status.lookupOrCreate('Responder', 'RES_AWAIT_PICKING', '0015', true);
        Status.lookupOrCreate('Responder', 'RES_AWAIT_PROXY_BORROWER', '0016', true, true);
        Status.lookupOrCreate('Responder', 'RES_CHECKED_IN_TO_RESHARE', '0020', true);
        Status.lookupOrCreate('Responder', 'RES_AWAIT_SHIP', '0021', true);
        Status.lookupOrCreate('Responder', 'RES_HOLD_PLACED', '0025', true);
        Status.lookupOrCreate('Responder', 'RES_UNFILLED', '0030', true);
        Status.lookupOrCreate('Responder', 'RES_NOT_SUPPLIED', '0035', true);
        Status.lookupOrCreate('Responder', 'RES_ITEM_SHIPPED', '0040', true);
        Status.lookupOrCreate('Responder', 'RES_ITEM_RETURNED', '0040', true);
        Status.lookupOrCreate('Responder', 'RES_COMPLETE', '0040', true);
        Status.lookupOrCreate('Responder', 'RES_CANCEL_REQUEST_RECEIVED', '9998', true, true);
        Status.lookupOrCreate('Responder', 'RES_CANCELLED', '9999', true);
        Status.lookupOrCreate('Responder', 'RES_ERROR', '9999', true, true);
        Status.lookupOrCreate('Responder', 'RES_OVERDUE', '9997', true);

        AvailableAction.ensure( 'Responder', 'RES_AWAIT_PROXY_BORROWER', 'message', 'M')
        AvailableAction.ensure( 'Responder', 'RES_AWAIT_PROXY_BORROWER', 'supplierAddCondition', 'M')

        AvailableAction.ensure( 'Responder', 'RES_CHECKED_IN_TO_RESHARE', 'supplierMarkShipped', 'M')
        AvailableAction.ensure( 'Responder', 'RES_CHECKED_IN_TO_RESHARE', 'message', 'M')
        AvailableAction.ensure( 'Responder', 'RES_CHECKED_IN_TO_RESHARE', 'supplierAddCondition', 'M')

        AvailableAction.ensure( 'Responder', 'RES_AWAIT_SHIP', 'supplierMarkShipped', 'M')
        AvailableAction.ensure( 'Responder', 'RES_AWAIT_SHIP', 'message', 'M')
        AvailableAction.ensure( 'Responder', 'RES_AWAIT_SHIP', 'supplierAddCondition', 'M')

        AvailableAction.ensure( 'Responder', 'RES_IDLE', 'message', 'M')
        AvailableAction.ensure( 'Responder', 'RES_IDLE', 'respondYes', 'M')
        AvailableAction.ensure( 'Responder', 'RES_IDLE', 'supplierCannotSupply', 'M')
        AvailableAction.ensure( 'Responder', 'RES_IDLE', 'supplierConditionalSupply', 'M')
        //AvailableAction.ensure( 'Responder', 'RES_IDLE', 'dummyAction', 'S')

        AvailableAction.ensure( 'Responder', 'RES_PENDING_CONDITIONAL_ANSWER', 'supplierMarkConditionsAgreed', 'M')
        AvailableAction.ensure( 'Responder', 'RES_PENDING_CONDITIONAL_ANSWER', 'supplierCannotSupply', 'M')
        AvailableAction.ensure( 'Responder', 'RES_PENDING_CONDITIONAL_ANSWER', 'message', 'M')

        AvailableAction.ensure( 'Responder', 'RES_CANCEL_REQUEST_RECEIVED', 'message', 'M')
        AvailableAction.ensure( 'Responder', 'RES_CANCEL_REQUEST_RECEIVED', 'supplierRespondToCancel', 'M')

        AvailableAction.ensure( 'Responder', 'RES_NEW_AWAIT_PULL_SLIP', 'supplierPrintPullSlip', 'M')
        AvailableAction.ensure( 'Responder', 'RES_NEW_AWAIT_PULL_SLIP', 'supplierAddCondition', 'M')
        AvailableAction.ensure( 'Responder', 'RES_NEW_AWAIT_PULL_SLIP', 'supplierCannotSupply', 'M')
        AvailableAction.ensure( 'Responder', 'RES_NEW_AWAIT_PULL_SLIP', 'message', 'M')

        AvailableAction.ensure( 'Responder', 'RES_AWAIT_PICKING', 'supplierCheckInToReshare', 'M')
        AvailableAction.ensure( 'Responder', 'RES_AWAIT_PICKING', 'supplierCannotSupply', 'M')
        AvailableAction.ensure( 'Responder', 'RES_AWAIT_PICKING', 'message', 'M')
        AvailableAction.ensure( 'Responder', 'RES_AWAIT_PICKING', 'supplierAddCondition', 'M')

        AvailableAction.ensure( 'Responder', 'RES_ITEM_SHIPPED', 'message', 'M')

        AvailableAction.ensure( 'Responder', 'RES_ITEM_RETURNED', 'supplierCheckOutOfReshare', 'M')
        AvailableAction.ensure( 'Responder', 'RES_ITEM_RETURNED', 'message', 'M')

        AvailableAction.ensure( 'Responder', 'RES_COMPLETE', 'message', 'M')
        AvailableAction.ensure( 'Responder', 'RES_OVERDUE', 'supplierCheckOutOfReshare', 'M')


        AvailableAction.ensure( 'PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER', 'message', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER', 'requesterCancel', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_CONDITIONAL_ANSWER_RECEIVED', 'message', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_CONDITIONAL_ANSWER_RECEIVED', 'requesterAgreeConditions', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_CONDITIONAL_ANSWER_RECEIVED', 'requesterRejectConditions', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_CONDITIONAL_ANSWER_RECEIVED', 'requesterCancel', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_IDLE', 'requesterCancel', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_IDLE', 'borrowerCheck', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_INVALID_PATRON', 'requesterCancel', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_INVALID_PATRON', 'borrowerCheck', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_INVALID_PATRON', 'borrowerCheckOverride', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_CANCEL_PENDING', 'message', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_VALIDATED', 'requesterCancel', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_SOURCING_ITEM', 'requesterCancel', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_SUPPLIER_IDENTIFIED', 'requesterCancel', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_EXPECTS_TO_SUPPLY', 'message', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_EXPECTS_TO_SUPPLY', 'requesterCancel', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_SHIPPED', 'message', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_SHIPPED', 'requesterReceived', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_BORROWING_LIBRARY_RECEIVED', 'requesterManualCheckIn', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_BORROWING_LIBRARY_RECEIVED', 'message', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_CHECKED_IN', 'patronReturnedItem', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_CHECKED_IN', 'message', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_AWAITING_RETURN_SHIPPING', 'shippedReturn', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_AWAITING_RETURN_SHIPPING', 'message', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_SHIPPED_TO_SUPPLIER', 'message', 'M')

        AvailableAction.ensure( 'PatronRequest', 'REQ_REQUEST_COMPLETE', 'message', 'M')
        
        AvailableAction.ensure( 'PatronRequest', 'REQ_OVERDUE', 'patronReturnedItem', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_OVERDUE', 'shippedReturn', 'M')
        
        AvailableAction.ensure( 'PatronRequest', 'REQ_LOCAL_REVIEW', 'fillLocally', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_LOCAL_REVIEW', 'requesterCancel', 'M')
        AvailableAction.ensure( 'PatronRequest', 'REQ_LOCAL_REVIEW', 'localSupplierCannotSupply', 'M')

        def alc = Counter.findByContext('/activeLoans') ?: new Counter(context:'/activeLoans', value:0, description:'Current (Aggregate) Lending Level').save(flush:true, failOnError:true)
        def abc = Counter.findByContext('/activeBorrowing') ?: new Counter(context:'/activeBorrowing', value:0, description:'Current (Aggregate) Borrowing Level').save(flush:true, failOnError:true)
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
