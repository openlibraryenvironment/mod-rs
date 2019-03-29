package org.olf.rs

import grails.gorm.multitenancy.Tenants
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional
import org.olf.rs.workflow.Action;
import javax.sql.DataSource
import groovy.sql.Sql
import grails.core.GrailsApplication
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.datastore.mapping.core.exceptions.ConfigurationException



/**
 * This service works at the module level, it's often called without a tenant context.
 */
@Transactional
public class HousekeepingService {

  HibernateDatastore hibernateDatastore
  DataSource dataSource
  GrailsApplication grailsApplication

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
      // Create the Actions / status and transitions for the core state model 
      Action.CreateDefault();
    }
  }


  /**
   *  Mod-RS needs some shared data to be able to route incoming messages to the appropriate tenant.
   *  This funcion creates a special shared schema that all tenants have access to. It is the place
   *  we register symbol -> tenant mappings.
   */
  public synchronized void ensureSharedSchema() {
    try {
      log.debug("See if we already have a datastore for __rs_shared")
      hibernateDatastore.getDatastoreForConnection('__rs_shared');
      log.debug("__rs_shared found. all is well");
    }
    catch ( ConfigurationException ce ) {
      log.debug("register schema __rs_shared");
      createAccountSchema('__rs_shared');
    }
    log.debug("ensureSharedSchema completed");
  }

  private synchronized void createAccountSchema(String schema_name) {
    Sql sql = null
    try {
      sql = new Sql(dataSource as DataSource)
      sql.withTransaction {
        log.debug("Execute -- create schema ${schema_name}");
        sql.execute("create schema ${schema_name}" as String)
      }
    } finally {
        sql?.close()
    }
  }


}
