package org.olf.rs

import org.olf.rs.referenceData.ActionEventData;
import org.olf.rs.referenceData.ActionEventResultData;
import org.olf.rs.referenceData.AvailableActionData;
import org.olf.rs.referenceData.CounterData;
import org.olf.rs.referenceData.CustomTextProperties;
import org.olf.rs.referenceData.NamingAuthority;
import org.olf.rs.referenceData.RefdataValueData;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.referenceData.StatusData;
import org.olf.rs.referenceData.TemplateData;
import org.olf.rs.referenceData.TimerData;
import org.olf.rs.statemodel.Status;

import com.k_int.okapi.OkapiTenantAdminService

import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.Tenants

/**
 * This service works at the module level, it's often called without a tenant context.
 */
public class HousekeepingService {

  // This was DataSource but I think this is actually a HibernateDataSource
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

		// Load the Custom text properties
		CustomTextProperties.loadAll();

        // Load the reference data (needs to be done before settings
        RefdataValueData.loadAll();

		// Add the Settings, when I get an hour or 2 will convert the rest of this file
		SettingsData.loadAll();

		// Add the naming authorities
		NamingAuthority.loadAll();

		// The status data
		StatusData.loadAll();

        // Load the action event results data, must be loaded after the Status data
        ActionEventResultData.loadAll();

        // The ActionEvent data, must be loaded after ActionEventResultData
        ActionEventData.loadAll();

		// Load the Available actions, must be loaded after ActionEventResultData and ActionEventData
		AvailableActionData.loadAll();

		// Load the counter data
		CounterData.loadAll();

		// Load the counter data
		TimerData.loadAll();

        // The predefined templates
        TemplateData.loadAll();
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
