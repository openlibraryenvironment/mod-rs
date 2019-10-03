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
  }

  def destroy = {
  }
}
