package mod.rs

import grails.events.EventPublisher
import grails.gorm.multitenancy.Tenants
import groovy.xml.StreamingMarkupBuilder
import org.olf.rs.ConfirmationMessageService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.StatisticsService;
import org.olf.rs.logging.ContextLogging;

import grails.converters.JSON;
import grails.core.GrailsApplication;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses
import org.olf.rs.logging.IIso18626LogDetails
import org.olf.rs.logging.Iso18626LogDetails
import org.olf.rs.logging.ProtocolAuditService
import org.olf.rs.statemodel.events.EventMessageRequestIndService
import org.xml.sax.SAXException;

/**
 * External Read-Only APIs for resource sharing network connectivity
 */
@Slf4j
@CurrentTenant
@Api(value = "/rs/externalApi", tags = ["External API Controller"], description = "API for external requests that do not require authentication")
class ExternalApiController implements EventPublisher {
  GrailsApplication grailsApplication
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  ConfirmationMessageService confirmationMessageService
  StatisticsService statisticsService
  ProtocolAuditService protocolAuditService

  // ToDo this method needs a TTL cache and some debounce mechanism as it increases in complexity
    @ApiOperation(
        value = "Return the statistics",
        nickname = "statistics",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
  def statistics() {
    ContextLogging.startTime();
    ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_STATISTICS);
    log.debug(ContextLogging.MESSAGE_ENTERING);

    Map result=[:]

    try {
      result = [
        asAt:new Date(),
        requestsByState: statisticsService.generateRequestsByState(),
        requestsByTag: statisticsService.generateRequestsByStateTag()
      ]
    }
    catch ( Exception e ) {
      log.error("Error in statistics",e);
      result.error=e?.toString()
    }

    render result as JSON

    // Record how long it took
    ContextLogging.duration();
    log.debug(ContextLogging.MESSAGE_EXITING);
  }

    @ApiOperation(
        value = "Receives an ISO18626 xml message",
        nickname = "iso18626",
        httpMethod = "POST",
        produces = "application/xml"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Created"),
        @ApiResponse(code = 400, message = "Bad Request")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            paramType = "body",
            required = true,
            allowMultiple = false,
            value = "The xml that contains the ISO18626 message",
            defaultValue = "{}",
            dataType = "string"
        )
    ])
  def iso18626() {
    ContextLogging.startTime();
    ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_ISO18626);
    ContextLogging.setValue(ContextLogging.FIELD_XML, request.XML);
    log.debug(ContextLogging.MESSAGE_ENTERING);

    try {
      if ( request.XML != null ) {
        IIso18626LogDetails iso18626LogDetails = protocolAuditService.getIso18626LogDetails()
        log.debug("Incoming ISO message logging enabled: ${iso18626LogDetails instanceof Iso18626LogDetails}")
        def xmlString = new StreamingMarkupBuilder().bind { mkp.yield request.XML }
        iso18626LogDetails.request(request.servletPath, xmlString.toString())
        String requestId = null

        org.grails.databinding.xml.GPathResultMap iso18626_msg = new org.grails.databinding.xml.GPathResultMap(request.XML);
        log.debug("GPATH MESSAGE: ${iso18626_msg}")

        // This string is purely used for logging messages
        String recipient;

        if ( iso18626_msg.request != null ) {

          def mr = iso18626_msg.request;
          log.debug("Process inbound request message. supplying agency id is ${mr?.header?.supplyingAgencyId}");

          // Look in request.header.supplyingAgencyId for the intended recipient
          recipient = getSymbolFor(mr.header.supplyingAgencyId)
          log.debug("incoming request message for ${recipient}");

          Map newMap = EventMessageRequestIndService.createNewCustomIdentifiers(request, mr)

          def req_result = reshareApplicationEventHandlerService.handleRequestMessage(newMap);
          log.debug("result of req_request ${req_result}");

          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage)
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")
          iso18626LogDetails.response("200", message)
          requestId = req_result.newRequestId

          render(text: message, contentType: "application/xml", encoding: "UTF-8")
        } else if ( iso18626_msg.supplyingAgencyMessage != null ) {

          def msam = iso18626_msg.supplyingAgencyMessage;
          log.debug("Process inbound supplyingAgencyMessage message. requestingAgencyId is ${msam?.header?.requestingAgencyId}");

          // Look in request.header.requestingAgencyId for the intended recipient
          recipient = getSymbolFor(msam.header.requestingAgencyId)
          log.debug("incoming supplying agency message for ${recipient}");

          def req_result = reshareApplicationEventHandlerService.handleSupplyingAgencyMessage(msam);
          log.debug("result of req_request ${req_result}");

          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage)
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")
          iso18626LogDetails.response("200", message)
          requestId = req_result.requestId

          render(text: message, contentType: "application/xml", encoding: "UTF-8")
        } else if ( iso18626_msg.requestingAgencyMessage != null ) {

          def mram = iso18626_msg.requestingAgencyMessage;
          log.debug("Process inbound requestingAgencyMessage message. SupplyingAgencyId is ${mram?.header?.supplyingAgencyId}");

          // Look in request.header.supplyingAgencyId for the intended recipient
          recipient = getSymbolFor(mram.header.supplyingAgencyId);
          log.debug("incoming requesting agency message for ${recipient}");

          def req_result = reshareApplicationEventHandlerService.handleRequestingAgencyMessage(mram);
          log.debug("result of req_request ${req_result}");

          def confirmationMessage = confirmationMessageService.makeConfirmationMessage(req_result)
          String message = confirmationMessageService.confirmationMessageReadable(confirmationMessage)
          log.debug("CONFIRMATION MESSAGE TO RETURN: ${message}")
          iso18626LogDetails.response("200", message)
          requestId = req_result.requestId

          render(text: message, contentType: "application/xml", encoding: "UTF-8")
        } else {
          render(status: 400, text: 'The sent request is not valid')
        }

        if (requestId) {
          log.debug("Saving protocol log for request ${requestId}")
          notify("ProtocolAuditService.saveSubscriber", Tenants.currentId(), requestId, iso18626LogDetails)
        }
      } else {
        log.error("NO XML Supplied in request. Unable to proceed");
        render(status: 400, text: 'The sent request is not valid')
      }
    } catch (SAXException e){
      return render(status: 400, text: "Response validation failed: ${e.message}")
    } catch ( Exception e ) {
      log.error("Exception while handling incoming ISO message",e);
      return render(status: 500, text: "Error: ${e.getLocalizedMessage()}");
    } finally {
      log.debug("ExternalApiController::iso18626 exiting cleanly");
    }

    // Record how long it took
    ContextLogging.duration();
    log.debug(ContextLogging.MESSAGE_EXITING);
  }

  private String getSymbolFor(path) {

    String result = null;
    if ( path?.agencyIdType != null && path?.agencyIdValue != null ) { // supplyingAgencyId is missing in request so we need it null safe
      result = "${path.agencyIdType.toString()}:${path.agencyIdValue.toString()}".toString()
    }
    else {
      log.warn("Missing agency id type or value");
    }
    log.debug("Returning symbol : ${result}");
    return result;
  }

    @ApiOperation(
        value = "Generates a brief status report about the service",
        nickname = "statusReport",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
  def statusReport() {
    ContextLogging.startTime();
    ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_STATUS_REPORT);
    log.debug(ContextLogging.MESSAGE_ENTERING);

    Map result = ['summary':[]]
    PatronRequest.executeQuery('select pr.state.code, count(*) from PatronRequest as pr group by pr.state.code').each { sg ->
      result.summary.add(['state':sg[0], 'count':sg[1]]);
    }
    render result as JSON

    // Record how long it took
    ContextLogging.duration();
    log.debug(ContextLogging.MESSAGE_EXITING);
  }
}
