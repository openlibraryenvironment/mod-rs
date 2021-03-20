package mod.rs

import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestRota

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.okapi.modules.directory.DirectoryEntry
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
      log.debug("PatronRequestController::performAction(${request.JSON})...");
      if ( params.patronRequestId ) {
        def patron_request = PatronRequest.get(params.patronRequestId)
        if ( patron_request ) {
          log.debug("Apply action ${request.JSON.action} to ${patron_request}");
          switch ( request.JSON.action ) {
            case 'supplierPrintPullSlip':
              result = reshareActionService.notiftyPullSlipPrinted(patron_request);
              break;
            case 'supplierCheckInToReshare':
              result.status = reshareActionService.checkInToReshare(patron_request, request.JSON.actionParams);
              if (result.status == false) {
                response.status = 400;
                  result.code=-3; // NCIP action failed
                  result.message='NCIP CheckoutItem call failed.'
              }
              break;
            case 'message':
              result.status = reshareActionService.sendMessage(patron_request, request.JSON.actionParams);
              patron_request.save(flush:true, failOnError:true);
              break;
            case 'messageSeen':
              result.status = reshareActionService.changeMessageSeenState(patron_request, request.JSON.actionParams);
              break;
            case 'messagesAllSeen':
            result.status = reshareActionService.markAllMessagesReadStatus(patron_request, request.JSON.actionParams);
            break;
            case 'respondYes':
              if ( request.JSON.actionParams.pickLocation != null ) {
                ItemLocation location = new ItemLocation( location: request.JSON.actionParams.pickLocation, 
                                                          shelvingLocation: request.JSON.actionParams.pickShelvingLocation,
                                                          callNumber: request.JSON.actionParams.callnumber)

                if ( reshareApplicationEventHandlerService.routeRequestToLocation(patron_request, location) ) {
                  reshareActionService.sendResponse(patron_request, 'ExpectToSupply', request.JSON.actionParams)
                  patron_request.save(flush:true, failOnError:true);
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
              reshareActionService.sendResponse(patron_request, 'Unfilled', request.JSON.actionParams);
              reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                    reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_IDLE'), 
                                    reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_UNFILLED'), 
                                    'Request manually flagged unable to supply', null);
              patron_request.state=reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_UNFILLED')
              patron_request.save(flush:true, failOnError:true);
              break;
            case 'requesterAgreeConditions':
              reshareActionService.sendLoanConditionResponse(patron_request, request.JSON.actionParams)
              reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                    reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_CONDITIONAL_ANSWER_RECEIVED'), 
                                    reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_EXPECTS_TO_SUPPLY'), 
                                    'Agreed to loan conditions', null);
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_EXPECTS_TO_SUPPLY');
              break;
            case 'requesterRejectConditions':
              patron_request.previousStates['REQ_CANCEL_PENDING'] = patron_request.state.code;
              reshareActionService.sendCancel(patron_request, request.JSON.action, request.JSON.actionParams)
              reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                    reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_CONDITIONAL_ANSWER_RECEIVED'), 
                                    reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_CANCEL_PENDING'), 
                                    'Rejected loan conditions', null);
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_CANCEL_PENDING');
              break;
            case 'requesterCancel':
              patron_request.previousStates['REQ_CANCEL_PENDING'] = patron_request.state.code;
              reshareActionService.sendCancel(patron_request, request.JSON.action, request.JSON.actionParams)
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_CANCEL_PENDING');
              break;
            case 'supplierConditionalSupply':
              if ( request.JSON.actionParams.pickLocation != null ) {
                ItemLocation location = new ItemLocation( location: request.JSON.actionParams.pickLocation, 
                                                          shelvingLocation: request.JSON.actionParams.pickShelvingLocation,
                                                          callNumber: request.JSON.actionParams.callnumber)

                if ( reshareApplicationEventHandlerService.routeRequestToLocation(patron_request, location) ) {
                  
                  reshareActionService.sendResponse(patron_request, 'ExpectToSupply', request.JSON.actionParams);
                  reshareActionService.sendSupplierConditionalWarning(patron_request, request.JSON.actionParams);

                  if (request.JSON.actionParams.isNull('holdingState') || request.JSON.actionParams.holdingState == "no") {
                    // The supplying agency wants to continue with the request
                    reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                        reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_IDLE'), 
                                        reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_NEW_AWAIT_PULL_SLIP'), 
                                        'Request responded to conditionally, request continuing', null);
                    patron_request.state=reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_NEW_AWAIT_PULL_SLIP')
                  } else {
                    // The supplying agency wants to go into a holding state

                    // In this case we want to "pretend" the previous state was actually the next one, for later when it looks up the previous state
                    patron_request.previousStates.put('RES_PENDING_CONDITIONAL_ANSWER', 'RES_NEW_AWAIT_PULL_SLIP')
                    reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                        reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_IDLE'), 
                                        reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_PENDING_CONDITIONAL_ANSWER'), 
                                        'Request responded to conditionally, placed in hold state', null);
                    patron_request.state=reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_PENDING_CONDITIONAL_ANSWER')
                  }
                  
                  patron_request.save(flush:true, failOnError:true);
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
            case 'supplierAddCondition':
              reshareActionService.addCondition(patron_request, request.JSON.actionParams);
              reshareActionService.sendSupplierConditionalWarning(patron_request, request.JSON.actionParams);
              if (request.JSON.actionParams.isNull('holdingState') || request.JSON.actionParams.holdingState == "no") {
                    // The supplying agency wants to continue with the request
                    reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                        patron_request.state, 
                                        patron_request.state, 
                                        'Added loan condition to request, request continuing', null);
                  } else {
                    // The supplying agency wants to go into a holding state
                    patron_request.previousStates.put('RES_PENDING_CONDITIONAL_ANSWER', patron_request.state.code);
                    reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                        patron_request.state, 
                                        reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_PENDING_CONDITIONAL_ANSWER'), 
                                        'Condition added to request, placed in hold state', null);
                    patron_request.state=reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_PENDING_CONDITIONAL_ANSWER')
                  }
                  patron_request.save(flush:true, failOnError:true);
              break;
            case 'supplierMarkShipped':
              reshareActionService.sendResponse(patron_request, 'Loaned', request.JSON.actionParams);
              reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                    patron_request.state,
                                    reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_ITEM_SHIPPED'), 
                                    'Shipped', null);
              patron_request.state=reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_ITEM_SHIPPED')
              patron_request.save(flush:true, failOnError:true);
              break;
            case 'supplierMarkConditionsAgreed':
              def s = reshareApplicationEventHandlerService.lookupStatus('Responder', patron_request.previousStates['RES_PENDING_CONDITIONAL_ANSWER']);
              reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                    reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_PENDING_CONDITIONAL_ANSWER'),
                                    s,
                                    'Conditions manually marked as agreed', null);
              patron_request.state=s;
              reshareApplicationEventHandlerService
              // Mark all conditions as accepted
              reshareApplicationEventHandlerService.markAllLoanConditionsAccepted(patron_request)
              patron_request.previousStates['RES_PENDING_CONDITIONAL_ANSWER'] = null;
              patron_request.save(flush:true, failOnError:true);
              break;
            case 'supplierRespondToCancel':
              reshareActionService.sendSupplierCancelResponse(patron_request, request.JSON.actionParams)
              // If the cancellation is denied, switch the cancel flag back to false, otherwise send request to complete
              if (request.JSON?.actionParams?.cancelResponse == "no") {
                patron_request.requesterRequestedCancellation = false;
                def s = reshareApplicationEventHandlerService.lookupStatus('Responder', patron_request.previousStates[patron_request.state.code]);
                reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                        patron_request.state, s, 'Cancellation denied', null);
                patron_request.previousStates[patron_request.state.code] = null
                patron_request.state = s;
              } else {
                patron_request.state=reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_UNFILLED')
                reshareApplicationEventHandlerService.auditEntry(patron_request, 
                                        patron_request.state, 
                                        reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_UNFILLED'), 
                                        'Cancellation accepted', null);
                patron_request.requesterRequestedCancellation = false;
              }

              patron_request.save(flush:true, failOnError:true);
              break;
            case 'itemReturned':
              reshareActionService.handleItemReturned(patron_request, request.JSON.actionParams);
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'Responder', 'REQ_AWAITING_RETURN_SHIPPING');
              break;
            case 'supplierManualCheckout':
              // reshareActionService.checkOutOfReshare(patron_request, request.JSON.actionParams);
              // SimpleTransition does a save
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams,'Responder',  'RES_AWAIT_SHIP');
              break;
            case 'supplierCheckOutOfReshare':
              // SimpleTransition does a save
              log.debug("supplierCheckOutOfReshare::send status change");
              reshareActionService.sendStatusChange(patron_request, "LoanCompleted", request.JSON.actionParams?.note)

              log.debug("supplierCheckOutOfReshare::check out of reshare");
              if(!reshareActionService.checkOutOfReshare(patron_request, request.JSON.actionParams)) {
                response.status = 400;
                result.code=-3; // NCIP action failed
                result.message='NCIP CheckinItem call failed'
                patron_request.save(flush:true, failOnError:true);
              } else {
                log.debug("supplierCheckOutOfReshare::transition");
                result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams,'Responder',  'RES_COMPLETE');
              }
              break;
            case 'requesterReceived':
              // This will trigger an NCIP acceptItem as well
              if(!reshareActionService.sendRequesterReceived(patron_request, request.JSON.actionParams)) {
                response.status = 400;
                  result.code=-3; // NCIP action failed
                  result.message='NCIP AcceptItem call failed'
              };
              patron_request.save(flush:true, failOnError:true);
              break;
            case 'requesterManualCheckIn':
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_CHECKED_IN');
              break;
            case 'patronReturnedItem':
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_AWAITING_RETURN_SHIPPING');
              break;
            case 'shippedReturn':
              reshareActionService.sendRequesterShippedReturn(patron_request, request.JSON.actionParams)
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_SHIPPED_TO_SUPPLIER');
              break;
            case 'borrowerCheck':
              Map borrower_check = reshareActionService.lookupPatron(patron_request, request.JSON.actionParams)
              result.status = borrower_check?.callSuccess && borrower_check?.patronValid
              if (result.status) {
                reshareActionService.simpleTransition(patron_request, request.JSON.actionParams, 'PatronRequest', 'REQ_VALIDATED');
              } else if (!borrower_check?.callSuccess) {
                // The Host LMS check call has failed, stay in current state
                patron_request.needsAttention=true;
                String message = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. '+borrower_check?.problems
                reshareApplicationEventHandlerService.auditEntry(patron_request, patron_request.state, patron_request.state, message, null);
              } else {
                // The call succeeded but patron is invalid
                def invalid_patron_state = reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_INVALID_PATRON')
                String message = "Failed to validate patron with id: \"${patron_request.patronIdentifier}\".${borrower_check?.status != null ? " (Patron state=${borrower_check.status})" : ""}".toString()
                reshareApplicationEventHandlerService.auditEntry(patron_request, patron_request.state, invalid_patron_state, message, null);
                patron_request.state = invalid_patron_state;
                patron_request.needsAttention=true;
              }
              patron_request.save(flush: true, failOnError: true)
              break;
            case 'borrowerCheckOverride':
              Map actionParams = request.JSON.actionParams
              actionParams.override = true

              Map borrower_check = reshareActionService.lookupPatron(patron_request, actionParams)
              // borrower_check.patronValid should ALWAYS be true in this action
              result.status = borrower_check?.callSuccess
              if (result.status) {
                reshareActionService.simpleTransition(patron_request, actionParams, 'PatronRequest', 'REQ_VALIDATED');
              } else {
                // The Host LMS check call has failed, stay in current state
                patron_request.needsAttention=true;
                String message = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. '+borrower_check?.problems
                reshareApplicationEventHandlerService.auditEntry(patron_request, patron_request.state, patron_request.state, message, null);
              }
              patron_request.save(flush: true, failOnError: true)
              break;
            case 'localSupplierCannotSupply':
              PatronRequestRota prr = patron_request.rota.find( { it.rotaPosition == patron_request.rotaPosition } );       
              if(prr) {
                def rota_state = reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_NOT_SUPPLIED');
                prr.state = rota_state;
                prr.save(flush:true, failOnError: true);
              }
              def unfilled_state = reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_UNFILLED');     
              reshareApplicationEventHandlerService.auditEntry(patron_request,
                patron_request.state, unfilled_state, "Request locally flagged as unable to supply", null);
              patron_request.state = unfilled_state;
              patron_request.save(flush:true, failOnError:true);
              break;
            case 'fillLocally':
              log.debug("Fill request locally, considered complete for ReShare");
              result.status = reshareActionService.simpleTransition(patron_request, request.JSON.actionParams,'Responder',  'RES_COMPLETE');
              break;
            default:
              log.warn("unhandled patron request action: ${request.JSON.action}");
              response.status = 422;
              break;
          }
        }
      }
    }
    log.debug("PatronRequestController::performAction exiting");
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

