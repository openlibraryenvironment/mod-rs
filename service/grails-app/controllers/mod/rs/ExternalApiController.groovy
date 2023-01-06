package mod.rs

import org.olf.rs.ConfirmationMessageService
import org.olf.rs.Counter
import org.olf.rs.PatronRequest
import org.olf.rs.ReshareApplicationEventHandlerService

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j

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

  // ToDo this method needs a TTL cache and some debounce mechanism as it increases in complexity
  def statistics() {

    Map result=[:]

    try {
      result = [
        asAt:new Date(),
        current:Counter.list().collect { [ context:it.context, value:it.value, description:it.description ] },
        requestsByState: generateRequestsByState(),
        requestsByTag: generateRequestsByStateTag()
      ]
    }
    catch ( Exception e ) {
      log.error("Error in statistics",e);
      result.error=e?.toString()
    }

    render result as JSON
  }

  private Map generateRequestsByState() {
    Map result = [:]
    PatronRequest.executeQuery('select pr.stateModel.shortcode, pr.state.code, count(pr.id) from PatronRequest as pr group by pr.stateModel.shortcode, pr.state.code').each { sl ->
      result[sl[0]+':'+sl[1]] = sl[2]
    }
    return result;
  }

  private Map generateRequestsByStateTag() {
    Map result = [:]
    PatronRequest.executeQuery('select tag.value, count(pr.id) from PatronRequest as pr join pr.state.tags as tag group by tag.value').each { sl ->
      result[sl[0]] = sl[1]
    }
    return result;
  }


  def iso18626() {
    log.debug("externalApiController::index(${params})");

    try {
      if ( request.XML != null ) {
        org.grails.databinding.xml.GPathResultMap iso18626_msg = new org.grails.databinding.xml.GPathResultMap(request.XML);
        log.debug("GPATH MESSAGE: ${iso18626_msg}")

        // TODO This string is purely used for logging messages atm--decide if necessary
        String recipient;

        if ( iso18626_msg.request != null ) {

          def mr = iso18626_msg.request;
          log.debug("Process inbound request message. supplying agency id is ${mr?.header?.supplyingAgencyId}");

          // Look in request.header.supplyingAgencyId for the intended recipient
          recipient = getSymbolFor(mr.header.supplyingAgencyId)
          log.debug("incoming request message for ${recipient}");

          def req_result = reshareApplicationEventHandlerService.handleRequestMessage(mr);
          log.debug("result of req_request ${req_result}");

          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage, false)
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")

          render(text: message, contentType: "application/xml", encoding: "UTF-8")
          // render( contentType:"text/xml" ) { confirmationMessage }
        }
        else if ( iso18626_msg.supplyingAgencyMessage != null ) {

          def msam = iso18626_msg.supplyingAgencyMessage;
          log.debug("Process inbound supplyingAgencyMessage message. requestingAgencyId is ${msam?.header?.requestingAgencyId}");

          // Look in request.header.requestingAgencyId for the intended recipient
          recipient = getSymbolFor(msam.header.requestingAgencyId)
          log.debug("incoming supplying agency message for ${recipient}");

          def req_result = reshareApplicationEventHandlerService.handleSupplyingAgencyMessage(msam);
          log.debug("result of req_request ${req_result}");

          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage, false)
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")

          render(text: message, contentType: "application/xml", encoding: "UTF-8")
          // render( contentType:"text/xml" ) { confirmationMessage }
        }
        else if ( iso18626_msg.requestingAgencyMessage != null ) {

          def mram = iso18626_msg.requestingAgencyMessage;
          log.debug("Process inbound requestingAgencyMessage message. SupplyingAgencyId is ${mram?.header?.supplyingAgencyId}");

          // Look in request.header.supplyingAgencyId for the intended recipient
          recipient = getSymbolFor(mram.header.supplyingAgencyId);
          log.debug("incoming requesting agency message for ${recipient}");

          def req_result = reshareApplicationEventHandlerService.handleRequestingAgencyMessage(mram);
          log.debug("result of req_request ${req_result}");

          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage, false)
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")

          render(text: message, contentType: "application/xml", encoding: "UTF-8")
          // render( contentType:"text/xml" ) { confirmationMessage }
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
      log.error("Exception receiving ISO message",e);
      e.printStackTrace()
    }
    finally {
      log.debug("ExternalApiController::iso18626 exiting cleanly");
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
