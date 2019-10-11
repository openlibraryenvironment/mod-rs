package mod.rs

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.GlobalConfigService
import org.olf.rs.BackgroundTaskService;

class iso18626Controller {

  GrailsApplication grailsApplication
  GlobalConfigService globalConfigService

  def index() {
    def result=[status:'ok']
    log.debug("iso18626Controller::index(${params})");

    try {
      log.debug("XML: ${request.XML}");
      def iso18626_msg = request.XML;
      String recipient;
      String tenant;

      if ( iso18626_msg.request != null ) {
        log.debug("Process inbound request message");
        // Look in request.header.supplyingAgencyId for the intended recipient
        recipient = getSymbolFor(iso18626_msg.request.header.supplyingAgencyId);
        tenant = globalConfigService.getTenantForSymbol(recipient);
        if ( tenant ) {
          log.debug("incoming request for ${tenant}");
        }
      }
      else if ( iso18626_msg.supplyingAgencyMessage != null ) {
        log.debug("Process inbound supplyingAgencyMessage message");
        // Look in request.header.requestingAgencyId for the intended recipient
        recipient = getSymbolFor(iso18626_msg.request.header.requestingAgencyId);
        tenant = globalConfigService.getTenantForSymbol(recipient);
        if ( tenant ) {
          log.debug("incoming request for ${tenant}");
        }
      }
      else if ( iso10626_msg.requestingAgencyMessageConfirmation != null ) {
        log.debug("Process inbound requestingAgencyMessage message");
        // Look in request.header.supplyingAgencyId for the intended recipient
        recipient = getSymbolFor(iso18626_msg.request.header.supplyingAgencyId);
        tenant = globalConfigService.getTenantForSymbol(recipient);
        if ( tenant ) {
          log.debug("incoming request for ${tenant}");
        }
      }

    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

    render result as JSON
  }

  def symbol() {
    def result=[status:'ok']
    log.debug("iso18626Controller::symbol(${params})");
    render result as JSON
  }

  private String getSymbolFor(path) {
    String result = null;
    if ( path.agencyIdType != null && path.agencyIdValue != null ) {
      result = "${path.agencyIdType.text()}:${path.agencyIdValue.text()}".toString()
    }
    else {
      log.error("Missing agency id type or value");
    }
    log.debug("Returning symbol : ${result}");
    return result;
  }
}
