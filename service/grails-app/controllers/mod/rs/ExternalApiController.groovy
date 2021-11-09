package mod.rs

import org.olf.okapi.modules.directory.DirectoryEntry

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.BackgroundTaskService;
import org.olf.rs.ReshareApplicationEventHandlerService
import org.olf.rs.ConfirmationMessageService
import groovy.util.logging.Slf4j
import org.olf.rs.Counter
import grails.gorm.multitenancy.CurrentTenant
import org.olf.rs.PatronRequest

/**
 * External Read-Only APIs for resource sharing network connectivity
 */
@Slf4j
@CurrentTenant
class ExternalApiController {

  GrailsApplication grailsApplication
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
  
          def mr = iso18626_msg.request;
          log.debug("Process inbound request message. supplying agency id is ${mr.header.supplyingAgencyId}");

          // Look in request.header.supplyingAgencyId for the intended recipient
          recipient = getSymbolFor(mr.header.supplyingAgencyId)
          log.debug("incoming request message for ${recipient}");

          def req_result = reshareApplicationEventHandlerService.handleRequestMessage(mr);
          log.debug("result of req_request ${req_result}");
  
          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage)
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")
  
          render( contentType:"text/xml" ) {
            confirmationMessage
          }
        }
        else if ( iso18626_msg.supplyingAgencyMessage != null ) {
  
          def msam = iso18626_msg.supplyingAgencyMessage;
          log.debug("Process inbound supplyingAgencyMessage message. requestingAgencyId is ${msam.header.requestingAgencyId}");
  
          // Look in request.header.requestingAgencyId for the intended recipient
          recipient = getSymbolFor(msam.header.requestingAgencyId)
          log.debug("incoming supplying agency message for ${recipient}");
  
          def req_result = reshareApplicationEventHandlerService.handleSupplyingAgencyMessage(msam);
          log.debug("result of req_request ${req_result}");
  
          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage)
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")
  
          render( contentType:"text/xml" ) {
            confirmationMessage
          }
        }
        else if ( iso18626_msg.requestingAgencyMessage != null ) {
  
          def mram = iso18626_msg.requestingAgencyMessage;
          log.debug("Process inbound requestingAgencyMessage message. SupplyingAgencyId is ${mram.header.supplyingAgencyId}");

          // Look in request.header.supplyingAgencyId for the intended recipient
          recipient = getSymbolFor(mram.header.supplyingAgencyId);
          log.debug("incoming requesting agency message for ${recipient}");

          def req_result = reshareApplicationEventHandlerService.handleRequestingAgencyMessage(mram);
          log.debug("result of req_request ${req_result}");
  
          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage)
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

  // ToDo this method is only used for logging purposes--consider removal
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

  def statusReport() {
    Map result = ['summary':[]]
    PatronRequest.executeQuery('select pr.state.code, count(*) from PatronRequest as pr group by pr.state.code').each { sg ->
      result.summary.add(['state':sg[0], 'count':sg[1]]);
    }
    render result as JSON
  }
}
