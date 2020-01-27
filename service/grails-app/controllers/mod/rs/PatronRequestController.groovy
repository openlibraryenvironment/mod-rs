package mod.rs

import org.olf.rs.PatronRequest

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;
import grails.converters.JSON
import org.olf.rs.statemodel.StateTransition
import org.olf.rs.ReshareActionService;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.lms.ItemLocation;


@Slf4j
@CurrentTenant
class PatronRequestController extends OkapiTenantAwareController<PatronRequest>  {

  ReshareActionService reshareActionService
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService

  PatronRequestController() {
    super(PatronRequest)
  }

  /**
   *  Controller action that takes a POST containing a json payload with the following parameters
   *   {
   *     action:"StartRota",
   *     actionParams:{}
   *   }
   */
  def performAction() {

    def result = [:]

    if ( request.method=='POST' ) {
      log.debug("PatronRequestController::performAction(${request.JSON})");
      if ( params.patronRequestId ) {
        def patron_request = PatronRequest.get(params.patronRequestId)
        if ( patron_request ) {
          log.debug("Apply action ${request.JSON.action} to ${patron_request}");
          switch ( request.JSON.action ) {
            case 'supplierPrintPullSlip':
              result.status = reshareActionService.notiftyPullSlipPrinted(patron_request);
              break;
            case 'supplierCheckInToReshare':
              result.status = reshareActionService.checkInToReshare(patron_request, request.JSON.actionParams);
              break;
            case 'message':
              result.status = reshareActionService.sendMessage(patron_request, request.JSON.actionParams);
              break;
            case 'messageSeen':
              result.status = reshareActionService.changeMessageSeenState(patron_request, request.JSON.actionParams);
              break;
            case 'respondYes':
              if ( request.JSON.actionParams.pickLocation != null ) {
                ItemLocation location = new ItemLocation( location: request.JSON.actionParams.pickLocation, 
                                                          shelvingLocation: request.JSON.actionParams.pickShelvingLocation,
                                                          callNumber: request.JSON.actionParams.callnumber)

                if ( reshareApplicationEventHandlerService.routeRequestToLocation(patron_request, location) ) {
                  reshareApplicationEventHandlerService.sendResponse(patron_request, 'ExpectToSupply')
                }
                else {
                  response.status = 400;
                  result.code=-2; // No location specified
                  result.message='Failed to route request to given location'
                }
              }
              else {
                response.status = 400;
                result.code=-1; // No location specified
                result.message='No pick location specified. Unable to continue'
              }
              break;
            case 'supplierCannotSupply':
              reshareApplicationEventHandlerService.sendResponse(patron_request, 'Unfilled', 'No copy');
              reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                    reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_IDLE'), 
                                    reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_UNFILLED'), 
                                    'Request manually flagged unable to supply', null);
              patron_request.state=reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_UNFILLED')
              patron_request.save(flush:true, failOnError:true);
              break;
            case 'supplierMarkShipped':
              reshareApplicationEventHandlerService.sendResponse(patron_request, 'Loaned');
              reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                    patron_request.state,
                                    reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_ITEM_SHIPPED'), 
                                    'Shipped', null);
              patron_request.state=reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_ITEM_SHIPPED')
              patron_request.save(flush:true, failOnError:true);
              break;
            case 'itemReturned':
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'Responder', 'REQ_AWAITING_RETURN_SHIPPING');
              break;
            case 'supplierManualCheckout':
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams,'Responder',  'RES_AWAIT_SHIP');
              break;
            case 'supplierCheckOutOfReshare':
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams,'Responder',  'RES_COMPLETE');
              break;
            case 'cancel':
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_CANCELLED');
              break;
            case 'requesterReceived':
              reshareApplicationEventHandlerService.sendRequesterReceived(patron_request, request.JSON.actionParams);
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_BORROWING_LIBRARY_RECEIVED');
              break;
            case 'requesterManualCheckIn':
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_CHECKED_IN');
              break;
            case 'patronReturnedItem':
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_AWAITING_RETURN_SHIPPING');
              break;
            case 'shippedReturn':
              reshareApplicationEventHandlerService.sendRequesterShippedReturn(patron_request, request.JSON.actionParams)
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_SHIPPED_TO_SUPPLIER');
              break;
            default:
              log.warn("unhandled patron request action: ${request.JSON.action}");
              break;
          }
        }
      }
    }
    render result as JSON;
  }

  /**
   *  Controller action that takes a POST containing a json payload with the following parameters
   *   {
   *     target:{
   *       requestList:[uuid-123,uuid-456,uuid-788], // NOT IMPLEMENTED YET
   *       query:"title=%"  // NOT IMPLEMENTED YET
   *     }
   *     action:"StartRota",
   *     actionParams:{}
   *   }
   */
  def bulkAction() {
    def result = [:]
    render result as JSON;
  }

  /**
   * list the valid actions for this request
   */
  def validActions() {
    log.debug("PatronRequestController::validActions() ${params}");
    def result = [:];

    if ( params.patronRequestId ) {
      def patron_request = PatronRequest.get(params.patronRequestId)

      if (  patron_request != null ) {
        result.actions=patron_request.getValidActions();
      }
      else {
        result.actions=[];
        result.message="Unable to locate request for ID ${params.patronRequestId}";
      }
    }
    else {
      result.actions=[];
      result.message="No ID provided in call to validActions";
    }

    render result as JSON
  }
}

