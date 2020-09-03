package mod.rs

import org.olf.okapi.modules.directory.DirectoryEntry

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.GlobalConfigService
import org.olf.rs.BackgroundTaskService;
import org.olf.rs.ReshareApplicationEventHandlerService
import org.olf.rs.ConfirmationMessageService
import grails.gorm.multitenancy.Tenants
import java.text.SimpleDateFormat
import groovy.xml.StreamingMarkupBuilder
import grails.gorm.multitenancy.Tenants
import groovy.util.logging.Slf4j
import org.olf.rs.Counter
import grails.gorm.multitenancy.CurrentTenant

/**
 * External Read-Only APIs for resource sharing network connectivity
 */
@Slf4j
@CurrentTenant
class ExternalApiController {

  GrailsApplication grailsApplication
  GlobalConfigService globalConfigService
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  ConfirmationMessageService confirmationMessageService

  def index() {
  }

  def statistics() {

    def result=[
    ]
   
    try {
      result = [
        asAt:new Date(),
        current:Counter.list().collect { [ context:it.context, value:it.value, description:it.description ] }
      ]
    }
    catch ( Exception e ) {
      result.error=e.message;
    }

    render result as JSON
  }


  def iso18626() {
    def result=[status:'ok']
    log.debug("externalApiController::index(${params})");

    try {
      if ( request.XML != null ) {
        org.grails.databinding.xml.GPathResultMap iso18626_msg = new org.grails.databinding.xml.GPathResultMap(request.XML);
        log.debug("GPATH MESSAGE: ${iso18626_msg}")

        // TODO This string is purely used for logging messages atm--decide if necessary
        String recipient;

        if ( iso18626_msg.request != null ) {
          log.debug("Process inbound request message");
  
          def mr = iso18626_msg.request;

          // Look in request.header.supplyingAgencyId for the intended recipient
          recipient = mr.header.supplyingAgencyId
          log.debug("incoming request message for ${recipient} ${getSymbolFor(recipient)}");

          req_result = reshareApplicationEventHandlerService.handleRequestMessage(mr);
          log.debug("result of req_request ${req_result}");
  
          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(delegate, req_result)
          StringWriter sw = new StringWriter();
          sw << new StreamingMarkupBuilder().bind (confirmationMessage)
          String message = sw.toString();
  
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")
  
          render( contentType:"text/xml" ) {
            confirmationMessage
          }
        }
        else if ( iso18626_msg.supplyingAgencyMessage != null ) {
          log.debug("Process inbound supplyingAgencyMessage message");
  
          def msam = iso18626_msg.supplyingAgencyMessage;
  
          // Look in request.header.requestingAgencyId for the intended recipient
          recipient = msam.header.requestingAgencyId
          log.debug("incoming supplying agency message for ${recipient} ${getSymbolFor(recipient)}");
  
          req_result = reshareApplicationEventHandlerService.handleSupplyingAgencyMessage(msam);
          log.debug("result of req_request ${req_result}");
  
          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(delegate, req_result)
          StringWriter sw = new StringWriter();
          sw << new StreamingMarkupBuilder().bind (confirmationMessage)
          String message = sw.toString();
  
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")
  
          render( contentType:"text/xml" ) {
            confirmationMessage
          }
        }
        else if ( iso18626_msg.requestingAgencyMessage != null ) {
          log.debug("Process inbound requestingAgencyMessage message");
  
          def mram = iso18626_msg.requestingAgencyMessage;

          // Look in request.header.supplyingAgencyId for the intended recipient
          recipient = mram.header.supplyingAgencyId;
          log.debug("incoming requesting agency message for ${recipient} ${getSymbolFor(recipient)}");

          req_result = reshareApplicationEventHandlerService.handleRequestingAgencyMessage(mram);
          log.debug("result of req_request ${req_result}");
  
          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(delegate, req_result)
          StringWriter sw = new StringWriter();
          sw << new StreamingMarkupBuilder().bind (confirmationMessage)
          String message = sw.toString();
  
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")
  
          render( contentType:"text/xml" ) {
            confirmationMessage
          }
        }
        else {
          render(status: 400, text: 'The sent request is not valid')
        }
      }
      else {
        log.error("NO XML Supplied in request. Unable to proceed");
        render(status: 400, text: 'The sent request is not valid')
      }
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

  }

  def symbol() {
    def result=[status:'ok']
    log.debug("externalApiController::symbol(${params})");
    render result as JSON
  }

  private String getSymbolFor(path) {
    String result = null;
    if ( path.agencyIdType != null && path.agencyIdValue != null ) {
      result = "${path.agencyIdType.toString()}:${path.agencyIdValue.toString()}".toString()
    }
    else {
      log.error("Missing agency id type or value");
    }
    log.debug("Returning symbol : ${result}");
    return result;
  }
}
