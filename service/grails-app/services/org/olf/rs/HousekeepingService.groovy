package org.olf.rs

import java.sql.ResultSet

import javax.sql.DataSource

import org.grails.datastore.mapping.core.exceptions.ConfigurationException
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
import org.olf.rs.statemodel.Status;

import grails.core.GrailsApplication
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import com.k_int.okapi.OkapiTenantAdminService



/**
 * This service works at the module level, it's often called without a tenant context.
 */
@Transactional
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
      Status.lookupOrCreate('PatronRequest', 'IDLE');
      Status.lookupOrCreate('PatronRequest', 'VALIDATED');
      Status.lookupOrCreate('PatronRequest', 'SOURCING_ITEM')
      Status.lookupOrCreate('PatronRequest', 'SUPPLIER_IDENTIFIED')
      Status.lookupOrCreate('PatronRequest', 'REQUEST_SENT_TO_SUPPLIER')
      Status.lookupOrCreate('PatronRequest', 'ITEM_SHIPPED')
      Status.lookupOrCreate('PatronRequest', 'BORROWING_LIBRARY_RECEIVED')
      Status.lookupOrCreate('PatronRequest', 'AWAITING_RETURN_SHIPPING')
      Status.lookupOrCreate('PatronRequest', 'BORROWER_RETURNED')
      Status.lookupOrCreate('PatronRequest', 'REQUEST_COMPLETE')
      Status.lookupOrCreate('PatronRequest', 'PENDING');
      Status.lookupOrCreate('PatronRequest', 'WILL_SUPPLY');
      Status.lookupOrCreate('PatronRequest', 'END_OF_ROTA');
    }
  }


  /**
   *  Mod-RS needs some shared data to be able to route incoming messages to the appropriate tenant.
   *  This funcion creates a special shared schema that all tenants have access to. It is the place
   *  we register symbol -> tenant mappings.
   */
  public synchronized void ensureSharedSchema() {
    log.debug("ensureSharedSchema completed");
    okapiTenantAdminService.enableTenant('__global',[:])
  }

  public void ensureSharedConfig() {
  }


}
