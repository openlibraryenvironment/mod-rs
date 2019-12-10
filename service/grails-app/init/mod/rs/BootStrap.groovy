package mod.rs

import com.k_int.okapi.OkapiTenantAdminService

class BootStrap {

  def grailsApplication
  def housekeepingService
  OkapiTenantAdminService okapiTenantAdminService
  
  def init = { servletContext ->

    Thread.sleep(2000);
    
    housekeepingService.ensureSharedSchema();
    okapiTenantAdminService.freshenAllTenantSchemas()
    String iso_override = grailsApplication.config.getProperty('isoOverRide')
    if ( iso_override ) {
      log.warn("isoOverRide IS SET ${iso_override}");
    }
    else {
      log.debug("isoOverRide is not set");
    }


  }

  def destroy = {
  }
}
