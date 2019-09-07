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
    def result = [:];

    String tenant_header = request.getHeader('X-OKAPI-TENANT')
    log.debug("ReshareSettingsController::tenantSymbols(${params}) ${request.method} ${tenant_header}");

    if ( ( tenant_header != null ) &&
         ( tenant_header.length() > 0 ) ) {
      switch ( request.method ) {
        case 'GET':
          result.symbols=globalConfigService.getSymbolsForTenant(tenant_header)
          result.status='ok'
          break;
        case 'POST':
          log.debug("Request to register symbol for tenant ${tenant_header} : ${request.JSON}");
          if ( ( request.JSON.symbol != null ) && 
               ( request.JSON.symbol.length() > 0 ) ) {
            globalConfigService.registerSymbolForTenant(request.JSON.symbol, tenant_header)
            result.status='ok'
          }
          break;
        default:
          result.status='fail'
      }
    }

    render result as JSON
  }
}
