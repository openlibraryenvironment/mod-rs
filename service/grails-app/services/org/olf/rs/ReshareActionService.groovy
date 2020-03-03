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

  public boolean checkInToReshare(PatronRequest pr, Map actionParams) {
    log.debug("checkInToReshare(${pr})");
    boolean result = false;

    if ( actionParams?.itemBarcode != null ) {
      if ( pr.state.code=='RES_AWAIT_PICKING' || pr.state.code=='RES_AWAIT_PROXY_BORROWER') {

        pr.selectedItemBarcode = actionParams?.itemBarcode;

        HostLMSActions host_lms = hostLMSService.getHostLMSActions();
        if ( host_lms ) {
          // Call the host lms to check the item out of the host system and in to reshare
          def checkout_result = host_lms.checkoutItem(actionParams?.itemBarcode, 
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
              s = Status.lookup('Responder', 'RES_CHECKED_IN_TO_RESHARE');
              auditEntry(pr, pr.state, s, 'HOST LMS Integraiton Check In to Reshare completed', null);
            }
            else {
              s = Status.lookup('Responder', 'RES_AWAIT_LMS_CHECKOUT');
              auditEntry(pr, pr.state, s, 'HOST LMS Integraiton Check In to Reshare Failed - Manual checkout needed', null);
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
    if (actionParams.isNull("note")) {
      return false;
    }


    Map eventData = [header:[]];

    String message_sender_symbol = "unassigned_message_sender_symbol";
    String peer_symbol = "unassigned_peer_symbol"


    def send_result
    // This is for sending a REQUESTING AGENCY message to the SUPPLYING AGENCY
    if (pr.isRequester == true) {
      result = sendRequestingAgencyMessage(pr, "Notification", actionParams.note)

    } // This is for sending a SUPPLYING AGENCY message to the REQUESTING AGENCY
    else {
      result = sendSupplyingAgencyMessage(pr, "Notification", null, null, actionParams.note)
    }

    if ( result == true) {
      log.warn("Unable to send protocol notification message");
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

    // ToDo: understand why sendRequestingAgencyMessage(pr, 'Received', actionParams.note) is not sufficient for both cases?
    if (!actionParams.isNull("note")) {
      sendRequestingAgencyMessage(pr, 'Received', actionParams.note);
    } else {
      sendRequestingAgencyMessage(pr, 'Received', null);
    }
  }

  public void sendRequesterShippedReturn(PatronRequest pr, Object actionParams) {
    log.debug("sendRequestingAgencyMessage(${pr?.id}, ${actionParams}");
    sendRequestingAgencyMessage(pr, 'ShippedReturn', actionParams?.note);
  }

  public boolean sendRequestingAgencyMessage(PatronRequest pr, String action, String note = null) {
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
                            String reasonUnfilled = null,
                            String note = null) {
    sendSupplyingAgencyMessage(pr, 'RequestResponse', status, reasonUnfilled, note);
  }

  public void sendStatusChange(PatronRequest pr,
                            String status,
                            String note = null) {
    sendSupplyingAgencyMessage(pr, 'StatusChange', status, null, note);
  }

  // see http://biblstandard.dk/ill/dk/examples/request-without-additional-information.xml
  // http://biblstandard.dk/ill/dk/examples/supplying-agency-message-delivery-next-day.xml
  // RequestReceived, ExpectToSupply, WillSupply, Loaned, Overdue, Recalled, RetryPossible,
  // Unfilled, CopyCompleted, LoanCompleted, CompletedWithoutReturn, Cancelled
  public boolean sendSupplyingAgencyMessage(PatronRequest pr, 
                                         String reason_for_message,
                                         String status, 
                                         String reasonUnfilled = null,
                                         String note = null) {

    log.debug("sendResponse(....)");
    boolean result = false;

    // pr.supplyingInstitutionSymbol
    // pr.peerRequestIdentifier
    if ( ( pr.resolvedSupplier != null ) && 
         ( pr.resolvedRequester != null ) ) {

      Map supplying_message_request = protocolMessageBuildingService.buildSupplyingAgencyMessage(pr, reason_for_message, status, reasonUnfilled, note)

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

  
  public void outgoingNotificationEntry(PatronRequest pr, String note, String action, Symbol message_sender, Symbol message_receiver, Boolean isRequester) {

    def outboundMessage = new PatronRequestNotification()
    outboundMessage.setPatronRequest(pr)
    outboundMessage.setTimestamp(Instant.now())
    outboundMessage.setMessageSender(message_sender)
    outboundMessage.setMessageReceiver(message_receiver)
    outboundMessage.setIsSender(true)

    outboundMessage.setAttachedAction(action)

    outboundMessage.setMessageContent(note)
    
    log.debug("Outbound Message: ${outboundMessage.messageContent}")
    pr.addToNotifications(outboundMessage)
    //outboundMessage.save(flush:true, failOnError:true)
  }
}
