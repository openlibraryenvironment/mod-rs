package org.olf.rs

import grails.gorm.multitenancy.Tenants
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional
import javax.sql.DataSource
import groovy.sql.Sql
import grails.core.GrailsApplication
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.datastore.mapping.core.exceptions.ConfigurationException
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase

import org.olf.rs.shared.TenantSymbolMapping;
import org.olf.rs.statemodel.Status;



/**
 * This service works at the module level, it's often called without a tenant context.
 */
@Transactional
public class HousekeepingService {

  HibernateDatastore hibernateDatastore
  DataSource dataSource
  GrailsApplication grailsApplication

  private static final SHARED_SCHEMA_NAME='__shared_ill_mappings';


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
    try {
      log.debug("See if we already have a datastore for ${SHARED_SCHEMA_NAME}")
      hibernateDatastore.getDatastoreForConnection(SHARED_SCHEMA_NAME);
      log.debug("${SHARED_SCHEMA_NAME} found. all is well");
    }
    catch ( ConfigurationException ce ) {
      log.debug("Shared schema not located - create it now");
      createAccountSchema(SHARED_SCHEMA_NAME);
    }

    // Now run any migrations to the schema that have not been completed yet
    updateAccountSchema(SHARED_SCHEMA_NAME,'system-level-changelog.groovy');

    log.debug("ensureSharedSchema completed");
  }

  public void ensureSharedConfig() {
    // This is for DEVELOPMENT ONLY - Need to find a sysadmin way to do this
    registerTenantSymbolMapping('RESHARE:DIKUA', 'diku');
    registerTenantSymbolMapping('RESHARE:DIKUB', 'diku');
    registerTenantSymbolMapping('RESHARE:DIKUC', 'diku');
    registerTenantSymbolMapping('RESHARE:ACMAIN', 'diku');
  }

  public void registerTenantSymbolMapping(String symbol, String tenant) {
    Tenants.withId(SHARED_SCHEMA_NAME) {
      TenantSymbolMapping.withNewTransaction {
        TenantSymbolMapping.findBySymbol(symbol) ?: new TenantSymbolMapping(
                                                                symbol:symbol,
                                                                tenant:tenant).save(flush:true, failOnError:true);
      }
    }
  }

  public String findTenantForSymbol(String symbol) {
    String result = null;
    //Tenants.withId(SHARED_SCHEMA_NAME) {
    //  TenantSymbolMapping.withNewTransaction {
    //    def mapping = TenantSymbolMapping.findBySymbol(symbol);
    //    if ( mapping ) {
    //      result = mapping.tenant;
    //    }
    //  }
    //}
    return 'diku';
  }

  /**
   * Create a schema in the supplied DB
   */
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


  /**
   * Synchronize a DB schema with a liquibase defintion.
   * this function is inspired by the grails-okapi module grails-app/services/com/k_int/okapi/OkapiTenantAdminService.groovy
   * It's job is to run a liquibase migration file against a given schema. 
   */
  void updateAccountSchema(String schema_name, String migration_file) {

    log.debug("updateAccountSchema(${schema_name},${migration_file})")
    // Now try create the tables for the schema
    try {
      GrailsLiquibase gl = new GrailsLiquibase(grailsApplication.mainContext)
      gl.dataSource = dataSource
      gl.dropFirst = false
      gl.changeLog = migration_file; // 'module-tenant-changelog.groovy'
      gl.contexts = []
      gl.labels = []
      gl.defaultSchema = schema_name
      gl.databaseChangeLogTableName = 'tenant_changelog'
      gl.databaseChangeLogLockTableName = 'tenant_changelog_lock'
      gl.afterPropertiesSet() // this runs the update command
    } catch (Exception e) {
      log.error("Exception trying to create new account schema tables for $schema_name", e)
      throw e
    }
    finally {
      log.debug("Database migration completed")
    }

    // This function actually adds the schema into the hibernate list of known schemas
    // without it withTenant(x) won't work.
    try {
      log.debug("adding tenant for ${schema_name}")
      hibernateDatastore.addTenantForSchema(schema_name)
    } catch (Exception e) {
      log.error("Exception adding tenant schema for ${schema_name}", e)
      throw e
    }
    finally {
      log.debug("added schema")
    }
  }


}
