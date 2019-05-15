package mod.rs

import com.k_int.okapi.OkapiTenantAdminService

class BootStrap {

  def grailsApplication
  def housekeepingService
  OkapiTenantAdminService okapiTenantAdminService
  
  def init = { servletContext ->

    okapiTenantAdminService.freshenAllTenantSchemas()
    //housekeepingService.ensureSharedSchema();
    //housekeepingService.ensureSharedConfig();

    if ( grailsApplication.config.rabbitmq?.enabled ) {
      log.debug("mod-rs starting, with rabbitmq enabled");
    }
    else {
      log.debug("mod-rs starting, with rabbitmq disabled");
    }
  }

  def destroy = {
  }
}
