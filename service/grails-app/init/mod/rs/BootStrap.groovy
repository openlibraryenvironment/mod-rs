package mod.rs

import com.k_int.okapi.OkapiTenantAdminService

class BootStrap {

  def grailsApplication
  def housekeepingService
  OkapiTenantAdminService okapiTenantAdminService
  
  def init = { servletContext ->
    
    housekeepingService.ensureSharedSchema();
    okapiTenantAdminService.freshenAllTenantSchemas()
    
    //housekeepingService.ensureSharedConfig();

  }

  def destroy = {
  }
}
