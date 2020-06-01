package org.olf.rs;

import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestRota
import org.olf.rs.PatronRequestNotification
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.StateModel
import org.olf.okapi.modules.directory.Symbol;
import groovy.json.JsonOutput;
import java.time.LocalDateTime;
import org.olf.okapi.modules.directory.DirectoryEntry
import java.time.Instant;
import org.olf.rs.lms.HostLMSActions;
import com.k_int.web.toolkit.settings.AppSetting;


/**
 * Handle user events.
 *
 * wheras ReshareApplicationEventHandlerService is about detecting and handling system generated events - incoming protocol messages etc
 * this class is the home for user triggered activities - checking an item into reshare, marking the pull slip as printed etc.
 */
public class ReshareActionService {

  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  ProtocolMessageService protocolMessageService
  ProtocolMessageBuildingService protocolMessageBuildingService
  HostLMSService hostLMSService
  StatisticsService statisticsService

  public boolean checkInToReshare(PatronRequest pr, Map actionParams) {
    log.debug("checkInToReshare(${pr})");
    boolean result = false;

    if ( actionParams?.itemBarcode != null ) {
      if ( pr.state.code=='RES_AWAIT_PICKING' || pr.state.code=='RES_AWAIT_PROXY_BORROWER') {

        pr.selectedItemBarcode = actionParams?.itemBarcode;

        HostLMSActions host_lms = hostLMSService.getHostLMSActions();
        if ( host_lms ) {
          // Call the host lms to check the item out of the host system and in to reshare
          def checkout_result = host_lms.checkoutItem(pr.hrid,
                                                      actionParams?.itemBarcode, 
                                                      pr.patronIdentifier,
                                                      pr.resolvedRequester)
          // If the host_lms adapter gave us a specific status to transition to, use it
          if ( checkout_result?.status ) {
            // the host lms service gave us a specific status to change to
            Status s = Status.lookup('Responder', checkout_result?.status);
            auditEntry(pr, pr.state, s, 'HOST LMS Integraiton Check In to Reshare Failed - Manual checkout needed', null);
            pr.state = s;
            pr.save(flush:true, failOnError:true);
          }
          else {
            // Otherwise, if the checkout succeeded or failed, set appropriately
            Status s = null;
            if ( checkout_result.result == true ) {
              statisticsService.incrementCounter('/activeLoans');
              pr.activeLoan=true
              s = Status.lookup('Responder', 'RES_CHECKED_IN_TO_RESHARE');
              auditEntry(pr, pr.state, s, 'HOST LMS Integraiton Check In to Reshare completed', null);
            }
            else {
              s = Status.lookup('Responder', 'RES_AWAIT_LMS_CHECKOUT');
              auditEntry(pr, pr.state, s, 'NCIP problem in HOST LMS Integraiton. Check In to Reshare Failed - Manual checkout needed. '+checkout_result.problems?.toString(), null);
            }
            pr.state = s;
            pr.save(flush:true, failOnError:true);
          }
        }
        else {
          Status s = Status.lookup('Responder', 'RES_AWAIT_LMS_CHECKOUT');
          auditEntry(pr, pr.state, s, 'HOST LMS Integration not configured. Manual checkout needed');
          pr.state = s;
          pr.save(flush:true, failOnError:true);
        }

        result = true;
      }
      else {
        log.warn("Unable to locate RES_CHECKED_IN_TO_RESHARE OR request not currently RES_AWAIT_PICKING(${pr.state.code})");
      }
    }

    return result;
  }

  public boolean supplierCannotSupply(PatronRequest pr, Map actionParams) {
    boolean result = false;
    log.debug("supplierCannotSupply(${pr})");
    return result;
  }

  public Map notiftyPullSlipPrinted(PatronRequest pr) {
    log.debug("notiftyPullSlipPrinted(${pr})");
    Map result = [status:false];

    Status s = Status.lookup('Responder', 'RES_AWAIT_PICKING');
    if ( s && pr.state.code=='RES_NEW_AWAIT_PULL_SLIP') {
      pr.state = s;
      pr.save(flush:true, failOnError:true);
      result.status = true;
    }
    else {
      log.warn("Unable to locate RES_CHECKED_IN_TO_RESHARE OR request not currently RES_AWAIT_PICKING(${pr.state.code})");
      result.code=-1; // Wrong state
      result.message="Unable to locate RES_CHECKED_IN_TO_RESHARE OR request not currently RES_AWAIT_PICKING(${pr?.state?.code})"
    }

    return result;
  }

  public boolean notifySupplierShip(PatronRequest pr) {
    log.debug("notifySupplierShip(${pr})");
    boolean result = false;
    Status s = Status.lookup('Responder', 'RES_ITEM_SHIPPED');
    if ( s && pr.state.code=='RES_AWAIT_SHIP') {
      pr.state = s;
      pr.save(flush:true, failOnError:true);
      result = true;
    }
    else {
      log.warn("Unable to locate RES_AWAIT_SHIP OR request not currently RES_AWAIT_SHIP(${pr.state.code})");
    }

    return result;
  }

  public boolean sendMessage(PatronRequest pr, Object actionParams) {
    log.debug("actionMessage(${pr})");
    boolean result = false;
    // Sending a message does not change the state of a request

    // If the actionParams does not contain a note then this method should do nothing
    if (!actionParams.isNull("note")) {
      Map eventData = [header:[]];

      String message_sender_symbol = "unassigned_message_sender_symbol";
      String peer_symbol = "unassigned_peer_symbol"


      // This is for sending a REQUESTING AGENCY message to the SUPPLYING AGENCY
      if (pr.isRequester == true) {
        result = sendRequestingAgencyMessage(pr, "Notification", actionParams)

      } // This is for sending a SUPPLYING AGENCY message to the REQUESTING AGENCY
      else {
        result = sendSupplyingAgencyMessage(pr, "Notification", null, actionParams)
      }

      if ( result == true) {
        log.warn("Unable to send protocol notification message");
      }
    }

    return result;
  }

  public boolean sendLoanConditionResponse(PatronRequest pr, Object actionParams) {
    /* This method will send a specialised notification message containing some unique human readable key at the beginning
     * This will indicate an agreement to the loan conditions.
    */
    
    log.debug("actionConditionResponse(${pr})");
    boolean result = false;
    String responseKey = "#ReShareLoanConditionAgreeResponse#"

    if (actionParams.isNull("note")) {
      actionParams.note = responseKey
    } else {
      actionParams.note = "${responseKey} ${actionParams.note}"
    }
    
    // Only the requester should ever be able to send one of these messages, otherwise something has gone wrong.
    if (pr.isRequester == true) {
      result = sendRequestingAgencyMessage(pr, "Notification", actionParams)
    } else {
      log.warn("The supplying agency should not be able to call sendLoanConditionResponse.");
    }
    return result;
  }

  public boolean sendSupplierConditionalWarning(PatronRequest pr, Object actionParams) {
    /* This method will send a specialised notification message either warning the requesting agency that their request is in statis until confirmation
     * is received that the loan conditions are agreed to, or warning that the conditions are assumed to be agreed to by default.
    */
    
    log.debug("supplierConditionalNotification(${pr})");
    boolean result = false;

    Map warningParams = [:]

    if (actionParams.isNull("holdingState") || actionParams.holdingState == 'no') {
      warningParams.note = "#ReShareSupplierConditionsAssumedAgreed#"
    } else {
      warningParams.note = "#ReShareSupplierAwaitingConditionConfirmation#"
    }
    
    // Only the supplier should ever be able to send one of these messages, otherwise something has gone wrong.
    if (pr.isRequester == false) {
      result = sendSupplyingAgencyMessage(pr, "Notification", null, warningParams)
    } else {
      log.warn("The requesting agency should not be able to call sendSupplierConditionalWarning.");
    }
    return result;
  }

  public boolean sendSupplierCancelResponse(PatronRequest pr, Map actionParams) {
    /* This method will send a cancellation response iso18626 message */
    
    log.debug("sendSupplierCancelResponse(${pr})");
    boolean result = false;
    String status;

     if (!actionParams.get('cancelResponse') != null ) {

        switch (actionParams.cancelResponse) {
          case 'yes':
            status = "Cancelled"
            break;
          case 'no':
            break;
          default:
            log.warn("sendSupplierCancelResponse received unexpected cancelResponse: ${actionParams.cancelResponse}")
            break;
        }

        // Only the supplier should ever be able to send one of these messages, otherwise something has gone wrong.
        if (pr.isRequester == false) {
          result = sendSupplyingAgencyMessage(pr, "CancelResponse", status, actionParams)
        } else {
          log.warn("The requesting agency should not be able to call sendSupplierConditionalWarning.");
        }
     } else {
      log.error("sendSupplierCancelResponse expected to receive a cancelResponse")
     }

    return result;
  }

  public boolean changeMessageSeenState(PatronRequest pr, Object actionParams) {
    log.debug("actionMessage(${pr})");
    boolean result = false;

    if (actionParams.isNull("id")){
      return result
    }
    if (actionParams.isNull("seenStatus")){
      log.warn("No seen status was sent to changeMessageSeenState")
      return result
    }

    def id = actionParams.id
    PatronRequestNotification message = PatronRequestNotification.findById(id)
    if ( message == null ) {
      log.warn("Unable to locate PatronRequestNotification corresponding to ID or hrid in requestingAgencyRequestId \"${id}\"");
      return result
    }

    message.setSeen(actionParams.seenStatus)
    message.save(flush:true, failOnError:true)

    result = true;
    return result;
  }

  private void markAsReadLogic(PatronRequestNotification message, String valueKey, boolean seenStatus) {
    switch (valueKey) {
      case 'on':
        message.setSeen(seenStatus)
        break;
      case 'on_(excluding_action_messages)':
        if (message.attachedAction == 'Notification') {
          message.setSeen(seenStatus)
        }
        break;
      case 'off':
        log.debug("chat setting off")
        break;
      default:
        // This shouldn't ever be reached
        log.error("Something went wrong determining auto mark as read setting")
    }
  }

  public boolean markAllMessagesReadStatus(PatronRequest pr, Object actionParams = {}) {
    log.debug("markAllAsRead(${pr})");
    boolean result = false;
    boolean excluding = false;
    if (actionParams.isNull("seenStatus")){
      return result
    }

    if (actionParams.excludes) {
      excluding = actionParams.excludes;
    }

    def messages = pr.notifications
    messages.each{message -> 
    // Firstly we only want to be setting messages as read/unread that aren't already, and that we didn't send
      if (message.seen != actionParams.seenStatus && !message.isSender) {
        // Next we check if we care about the user defined settings
        if (excluding) {
          // Find the chat_auto_read AppSetting
          AppSetting chat_auto_read = AppSetting.findByKey('chat_auto_read')?: null;

          // If the setting does not exist then assume we want to mark all as read
          if (!chat_auto_read) {
            log.warn("Couldn't find chat auto mark as read setting, assuming needs to mark all as read")
            message.setSeen(actionParams.seenStatus)
          } else {
            if (chat_auto_read.value) {
              markAsReadLogic(message, chat_auto_read.value, actionParams.seenStatus)
            } else {
              markAsReadLogic(message, chat_auto_read.defValue, actionParams.seenStatus)
            }
          }
        } else {
          // Sometimes we want to just mark all as read without caring about the user defined setting
          message.setSeen(actionParams.seenStatus)
        }
      }
    }

    pr.save(flush:true, failOnError:true)
    result = true;
    return result;
  }

  private void auditEntry(PatronRequest pr, Status from, Status to, String message, Map data) {

    String json_data = ( data != null ) ? JsonOutput.toJson(data).toString() : null;
    LocalDateTime ts = LocalDateTime.now();
    log.debug("add audit entry at ${ts}");

    pr.addToAudit( new PatronRequestAudit(
      patronRequest: pr,
      dateCreated:ts,
      fromStatus:from,
      toStatus:to,
      duration:null,
      message: message,
      auditData: json_data))
  }

  private Symbol resolveSymbol(String authorty, String symbol) {
    Symbol result = null;
    List<Symbol> symbol_list = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol = :authority and s.symbol = :symbol',
                                                   [authority:authorty?.toUpperCase(), symbol:symbol?.toUpperCase()]);
    if ( symbol_list.size() == 1 ) {
      result = symbol_list.get(0);
    }

    return result;
  }

  private Symbol resolveCombinedSymbol(String combinedString) {
    Symbol result = null;
    if ( combinedString != null ) {
      String[] name_components = combinedString.split(':');
      if ( name_components.length == 2 ) {
        result = resolveSymbol(name_components[0], name_components[1]);
      }
    }
    return result;
  }

  public simpleTransition(PatronRequest pr, Map params, String state_model, String target_status, String p_message=null) {

    log.debug("request to transition ${pr} to ${target_status}");
    def new_state = reshareApplicationEventHandlerService.lookupStatus(state_model, target_status);

    if ( ( pr != null ) && ( new_state != null ) ) {
      String message = p_message ?: "Simple Transition ${pr.state?.code} to ${new_state.code}".toString()

      reshareApplicationEventHandlerService.auditEntry(pr,
                                      pr.state,
                                      new_state,
                                      message, null);
      pr.state=new_state;
      pr.save(flush:true, failOnError:true);
      log.debug("Saved new state ${new_state.code} for pr ${pr.id}");
    }
  }


  public void sendRequesterReceived(PatronRequest pr, Object actionParams) {

    // Check the item in to the local LMS
    HostLMSActions host_lms = hostLMSService.getHostLMSActions();
    if ( host_lms ) {
      try {
        // Call the host lms to check the item out of the host system and in to reshare
        Map accept_result = host_lms.acceptItem(pr.hrid, // Item Barcode - using Request human readable ID for now
                                                pr.hrid,
                                                pr.patronIdentifier, // user_idA
                                                pr.author, // author,
                                                pr.title, // title,
                                                pr.isbn, // isbn,
                                                pr.localCallNumber, // call_number,
                                                pr.pickupLocation, // pickup_location,
                                                null) // requested_action

        if ( accept_result?.result == true ) {
          // Mark item as awaiting circ
          def new_state = reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_CHECKED_IN');
          String message = 'NCIP acceptItem completed'

          reshareApplicationEventHandlerService.auditEntry(pr,
                                          pr.state,
                                          new_state,
                                          message, 
                                          null);
          pr.state=new_state;
          pr.save(flush:true, failOnError:true);
          log.debug("Saved new state ${new_state.code} for pr ${pr.id}");
        }
        else {
          // PR-658 wants us to set some state here but doesn't say what that state is. Currently we leave the state as is
          reshareApplicationEventHandlerService.auditEntry(pr,
                                          pr.state,
                                          pr.state,
                                          'NCIP accept item failed. Please recheck and try again: '+accept_result?.problem, 
                                          null);
          pr.save(flush:true, failOnError:true);
        }
      }
      catch ( Exception e ) {
        log.error("NCIP Problem",e);
      }
    }
   
    sendRequestingAgencyMessage(pr, 'Received', actionParams);
  }


  /** 
   * At the end of the process, check the item back into the HOST lms
   */
  public boolean checkOutOfReshare(PatronRequest patron_request, Map params) {
    boolean result = true;
    log.debug("checkOutOfReshare(${patron_request?.id}, ${params}");
    try {
      // Call the host lms to check the item out of the host system and in to reshare
      // def accept_result = host_lms.checkInItem(patron_request.hrid)
      HostLMSActions host_lms = hostLMSService.getHostLMSActions();
      def check_in_result = host_lms.checkInItem(patron_request.selectedItemBarcode)
      statisticsService.decrementCounter('/activeLoans');
      patron_request.activeLoan=false
    }
    catch ( Exception e ) {
      log.error("NCIP Problem",e);
      reshareApplicationEventHandlerService.auditEntry(patron_request,
                                          patron_request.state,
                                          patron_request.state,
                                          'host LMS Integration - checkInItem failed. Please retry. Message:'+e.message,
                                          null);
      result = false;
    }
    return result;
  }

  public void handleItemReturned(PatronRequest patron_request, Map params) {
    log.debug("handleItemReturned(${patron_request?.id}, ${params}");
  }

  public void sendRequesterShippedReturn(PatronRequest pr, Object actionParams) {
    log.debug("sendRequestingAgencyMessage(${pr?.id}, ${actionParams}");
    sendRequestingAgencyMessage(pr, 'ShippedReturn', actionParams);
  }

  public boolean sendCancel(PatronRequest pr, String action, Object actionParams) {
    pr.previousState = pr.state.code
    switch (action) {
      case 'requesterRejectedConditions':
        pr.requestToContinue = true;
        break;
      case 'requesterCancel':
        pr.requestToContinue = false;
        break;
      default:
        log.error("Action ${action} should not be able to send a cancel message")
        break;
    }
    pr.save(flush:true, failOnError:true);
    
    sendRequestingAgencyMessage(pr, 'Cancel', actionParams)
  }

  public boolean sendRequestingAgencyMessage(PatronRequest pr, String action, Map messageParams) {
    String note = messageParams?.note
    boolean result = false;

    Long rotaPosition = pr.rotaPosition;
    // We check that it is sensible to send a message, ie that we have a non-empty rota and are pointing at an entry in that.
    if (pr.rota.isEmpty()) {
      log.error("sendRequestingAgencyMessage has been given an empty rota")
      return;
    }
    if (rotaPosition == null) {
      log.error("sendRequestingAgencyMessage could not find current rota postition")
      return;
    } else if (pr.rota.empty()) {
      log.error("sendRequestingAgencyMessage has been handed an empty rota")
      return;
    }

    String message_sender_symbol = pr.requestingInstitutionSymbol;

    log.debug("ROTA: ${pr.rota}")
    log.debug("ROTA TYPE: ${pr.rota.getClass()}")
    PatronRequestRota prr = pr.rota.find({it.rotaPosition == rotaPosition})
    log.debug("ROTA at position ${pr.rotaPosition}: ${prr}")
    String peer_symbol = "${prr.peerSymbol.authority.symbol}:${prr.peerSymbol.symbol}"

    Map eventData = protocolMessageBuildingService.buildRequestingAgencyMessage(pr, message_sender_symbol, peer_symbol, action, note)

    def send_result = protocolMessageService.sendProtocolMessage(message_sender_symbol, peer_symbol, eventData);
    if ( send_result.status=='SENT') {
      result = true;
    }
    else {
      log.warn("Unable to send protocol message");
    }
    return result;
  }


  public void sendResponse(PatronRequest pr,
                            String status,
                            Map responseParams) {
    sendSupplyingAgencyMessage(pr, 'RequestResponse', status, responseParams);
  }

  public void addCondition(PatronRequest pr, Map responseParams) {
    Map conditionParams = responseParams
    log.debug("addCondition::(${pr})")

    if (!responseParams.isNull("note")){
      conditionParams.note = "#ReShareAddLoanCondition# ${responseParams.note}"
    } else {
      conditionParams.note = "#ReShareAddLoanCondition#"
    }

    if (!conditionParams.isNull("loanCondition")) {
      sendMessage(pr, conditionParams);
    } else {
      log.warn("addCondition not handed any conditions")
    }
  }

  public void sendStatusChange(PatronRequest pr,
                            String status,
                            String note = null) {
    Map params = [:]
    if (note) {
      params = [note: note]
    }
    
    sendSupplyingAgencyMessage(pr, 'StatusChange', status, params);
  }

  // see http://biblstandard.dk/ill/dk/examples/request-without-additional-information.xml
  // http://biblstandard.dk/ill/dk/examples/supplying-agency-message-delivery-next-day.xml
  // RequestReceived, ExpectToSupply, WillSupply, Loaned, Overdue, Recalled, RetryPossible,
  // Unfilled, CopyCompleted, LoanCompleted, CompletedWithoutReturn, Cancelled
  public boolean sendSupplyingAgencyMessage(PatronRequest pr, 
                                         String reason_for_message,
                                         String status,
                                         Map messageParams) {

    log.debug("sendResponse(....)");
    boolean result = false;

    // pr.supplyingInstitutionSymbol
    // pr.peerRequestIdentifier
    if ( ( pr.resolvedSupplier != null ) && 
         ( pr.resolvedRequester != null ) ) {

      Map supplying_message_request = protocolMessageBuildingService.buildSupplyingAgencyMessage(pr, reason_for_message, status, messageParams)

      log.debug("calling protocolMessageService.sendProtocolMessage(${pr.supplyingInstitutionSymbol},${pr.requestingInstitutionSymbol},${supplying_message_request})");
      def send_result = protocolMessageService.sendProtocolMessage(pr.supplyingInstitutionSymbol,
                                                                   pr.requestingInstitutionSymbol, 
                                                                   supplying_message_request);
      if ( send_result.status=='SENT') {
        result = true;
      }
      else {
        log.warn("Unable to send protocol message");
      }
    }
    else {
      log.error("Unable to send protocol message - supplier(${pr.resolvedSupplier}) or requester(${pr.resolvedRequester}) is missing in PatronRequest ${pr.id}Returned");
    }

    return result;
  }

  
  public void outgoingNotificationEntry(PatronRequest pr, String note, Map actionMap, Symbol message_sender, Symbol message_receiver, Boolean isRequester) {

    String attachedAction = actionMap.action
    String actionStatus = actionMap.status
    String actionData = actionMap.data

    def outboundMessage = new PatronRequestNotification()
    outboundMessage.setPatronRequest(pr)
    outboundMessage.setTimestamp(Instant.now())
    outboundMessage.setMessageSender(message_sender)
    outboundMessage.setMessageReceiver(message_receiver)
    outboundMessage.setIsSender(true)

    outboundMessage.setAttachedAction(attachedAction)
    outboundMessage.setActionStatus(actionStatus)
    outboundMessage.setActionData(actionData)

    outboundMessage.setMessageContent(note)
    
    log.debug("Outbound Message: ${outboundMessage.messageContent}")
    pr.addToNotifications(outboundMessage)
    //outboundMessage.save(flush:true, failOnError:true)
  }
}
