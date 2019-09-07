package mod.rs

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.GlobalConfigService

class ReshareSettingsController {

  GrailsApplication grailsApplication
  GrailsPluginManager pluginManager
  GlobalConfigService globalConfigService

  def tenantSymbols() {
    def result = null;

    String tenant_header = request.getHeader('X-OKAPI-TENANT')
    log.debug("ReshareSettingsController::tenantSymbols(${params}) ${request.method} ${tenant_header}");

    if ( ( tenant_header != null ) &&
         ( tenant_header.length() > 0 ) ) {
      switch ( request.method ) {
        case 'GET':
          result = [ symbols: globalConfigService.getSymbolsForTenant(tenant_header) ]
          break;
        case 'POST':
          break;
      }
    }

    render result as JSON
  }
}
