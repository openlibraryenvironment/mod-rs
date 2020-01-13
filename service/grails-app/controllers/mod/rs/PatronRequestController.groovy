package mod.rs

import org.olf.rs.PatronRequest

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;
import grails.converters.JSON
import org.olf.rs.statemodel.StateTransition
import org.olf.rs.ReshareActionService;

@Slf4j
@CurrentTenant
class PatronRequestController extends OkapiTenantAwareController<PatronRequest>  {

  ReshareActionService reshareActionService

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
            case 'supplierCannotSupply':
              result.status = reshareActionService.supplierCannotSupply(patron_request, request.JSON.actionParams);
              break;
            case 'message':
              result.status = reshareActionService.sendMessage(patron_request, request.JSON.actionParams);
              break;
            case 'respondYes'
              break;
            case 'supplierCannotSupply'
              break;
            case 'supplierShip'
              break;
            case 'itemReturned'
              break;
            case 'supplierCheckOutOfReshare'
              break;
            case 'cancel'
              break;
            case 'requesterReceived'
              break;
            case 'patronReturnedItem'
              break;
            case 'shippedReturn'
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

