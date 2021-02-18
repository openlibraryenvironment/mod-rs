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
import com.k_int.web.toolkit.custprops.CustomProperty


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


  /* WARNING: this method is NOT responsible for saving or for managing state changes.
   * It simply performs the lookupAction and appends relevant info to the patron request
   */
  public Map lookupPatron(PatronRequest pr, Map actionParams) {
    Map result = [callSuccess: false, patronValid: false ]
    log.debug("lookupPatron(${pr})");
    def patron_details = hostLMSService.getHostLMSActions().lookupPatron(pr.patronIdentifier)
    log.debug("Result of patron lookup ${patron_details}");
    if ( patron_details.result ) {
      result.callSuccess = true

      // Save patron details whether they're valid or not
      if ( patron_details.userid == null )
          patron_details.userid = pr.patronIdentifier
      if ( ( patron_details != null ) && ( patron_details.userid != null ) ) {
        pr.resolvedPatron = lookupOrCreatePatronProxy(patron_details);
        if ( pr.patronSurname == null )
          pr.patronSurname = patron_details.surname;
        if ( pr.patronGivenName == null )
          pr.patronGivenName = patron_details.givenName;
        if ( pr.patronEmail == null )
          pr.patronEmail = patron_details.email;
      }

      if (isValidPatron(patron_details) || actionParams?.override) {
        result.patronValid = true
        // Let the user know if the success came from a real call or a spoofed one
        String reason = patron_details.reason == 'spoofed' ? '(No host LMS integration configured for borrower check call)' : 'Host LMS integration: borrower check call succeeded.'
        String outcome = actionParams?.override ? 'validation overriden' : 'validated'
        String message = "Patron ${outcome}. ${reason}"
        auditEntry(pr, pr.state, pr.state, message, null);
      }
    }
    if (patron_details.problems) {
      result.problems = patron_details.problems.toString()
    }
    result.status = patron_details?.status
    return result
  }

  public boolean isValidPatron(Map patron_record) {
    boolean result = false;
    log.debug("Check isValidPatron: ${patron_record}");
    if ( patron_record != null ) {
      if ( patron_record.status == 'OK' ) {
        result = true;
      }
    }
    return result;
  }

    private Patron lookupOrCreatePatronProxy(Map patron_details) {
    Patron result = null;
    if ( ( patron_details != null ) && 
         ( patron_details.userid != null ) &&
         ( patron_details.userid.trim().length() > 0 ) ) {
      result = Patron.findByHostSystemIdentifier(patron_details.userid) ?: new Patron(
                                                           hostSystemIdentifier:patron_details.userid, 
                                                           givenname: patron_details.givenName, 
                                                           surname: patron_details.surname).save()
    }
    return result;
  }

  public boolean checkInToReshare(PatronRequest pr, Map actionParams) {
    log.debug("checkInToReshare(${pr})");
    boolean result = false;

    if ( actionParams?.itemBarcode != null ) {
      if ( pr.state.code=='RES_AWAIT_PICKING' || pr.state.code=='RES_AWAIT_PROXY_BORROWER') {
        pr.selectedItemBarcode = actionParams?.itemBarcode;

        HostLMSActions host_lms = hostLMSService.getHostLMSActions();
        if ( host_lms ) {
          // Call the host lms to check the item out of the host system and in to reshare

          /*
           * The supplier shouldn't be attempting to check out of their host LMS with the requester's side patronID.
           * Instead use institutionalPatronID saved on DirEnt or default from settings.
          */

          /* 
           * This takes the resolvedRequester symbol, then looks at its owner, which is a DirectoryEntry
           * We then feed that into extractCustomPropertyFromDirectoryEntry to get a CustomProperty.
           * Finally we can extract the value from that custprop.
           * Here that value is a string, but in the refdata case we'd need value?.value
          */
          CustomProperty institutionalPatronId = extractCustomPropertyFromDirectoryEntry(pr.resolvedRequester?.owner, 'local_institutionalPatronId')
          String institutionalPatronIdValue = institutionalPatronId?.value
          if (!institutionalPatronIdValue) {
            // If nothing on the Directory Entry then fallback to the default in settings
            AppSetting default_institutional_patron_id = AppSetting.findByKey('default_institutional_patron_id')
            institutionalPatronIdValue = default_institutional_patron_id?.value
          }

          /*
           * Be aware that institutionalPatronIdValue here may well be blank or null.
           * In the case that host_lms == ManualHostLMSService we don't care, we're just spoofing a positive result,
           * so we delegate responsibility for checking this to the hostLMSService itself, with errors arising in the 'problems' block 
           */
          def checkout_result = host_lms.checkoutItem(pr.hrid,
                                                      actionParams?.itemBarcode, 
                                                      institutionalPatronIdValue,
                                                      pr.resolvedRequester)
          // If the host_lms adapter gave us a specific status to transition to, use it
          if ( checkout_result?.status ) {
            // the host lms service gave us a specific status to change to
            Status s = Status.lookup('Responder', checkout_result?.status);
            String message = 'Host LMS integration: NCIP CheckoutItem call failed. Review configuration and try again or deconfigure host LMS integration in settings. '+checkout_result.problems?.toString()
            auditEntry(pr, pr.state, s, message, null);
            pr.state = s;
            pr.save(flush:true, failOnError:true);
          }
          else {
            // Otherwise, if the checkout succeeded or failed, set appropriately
            Status s = null;
            if ( checkout_result.result == true ) {
              statisticsService.incrementCounter('/activeLoans');
              pr.activeLoan=true
              pr.needsAttention=false;
              pr.dueDateFromLMS=checkout_result?.dueDate;
              if(!pr?.dueDateRS) {
                pr.dueDateRS = pr.dueDateFromLMS;
              }
              
              try {
                pr.parsedDueDateFromLMS = parseDateString(pr.dueDateFromLMS);                  
              } catch(Exception e) {
                log.warn("Unable to parse ${pr.dueDateFromLMS} to date: ${e.getMessage()}");
              }
              
              try {
                pr.parsedDueDateRS = parseDateString(pr.dueDateRS);
              } catch(Exception e) {
                log.warn("Unable to parse ${pr.dueDateRS} to date: ${e.getMessage()}");
              }

              pr.overdue=false;
              s = Status.lookup('Responder', 'RES_AWAIT_SHIP');
              // Let the user know if the success came from a real call or a spoofed one
              auditEntry(pr, pr.state, s, "Fill request completed. ${checkout_result.reason=='spoofed' ? '(No host LMS integration configured for check out item call)' : 'Host LMS integration: CheckoutItem call succeeded.'}", null);
              pr.state = s;
              result = true;
            }
            else {
              pr.needsAttention=true;
              auditEntry(pr, pr.state, pr.state, 'Host LMS integration: NCIP CheckoutItem call failed. Review configuration and try again or deconfigure host LMS integration in settings. '+checkout_result.problems?.toString(), null);
            }
            pr.save(flush:true, failOnError:true);
          }
        }
        else {
          auditEntry(pr, pr.state, pr.state, 'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.', null);
          pr.needsAttention=true;
          pr.save(flush:true, failOnError:true);
        }
      }
      else {
        log.warn("Unable to locate RES_AWAIT_SHIPPING OR request not currently RES_AWAIT_PICKING(${pr.state.code})");
      }
    }
    checkRequestOverdue(pr);

    return result;
  }

  public boolean supplierCannotSupply(PatronRequest pr, Map actionParams) {
    boolean result = false;
    log.debug("supplierCannotSupply(${pr})");
    return result;
  }
  
  public void checkRequestOverdue(PatronRequest pr) {
    if(!pr?.parsedDueDateRS) {
      return;
    }
    nowDate = new Date();
    if(nowDate.compareTo(pr.parsedDueDateRS) < 0) {
      pr.overdue = true;
      Status s = Status.lookup('PatronRequest', 'REQ_OVERDUE');
      pr.state = s;
      pr.save(flush:true, failOnError:true);
    } 
    
    
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
      log.warn("Unable to locate RES_AWAIT_PICKING OR request not currently RES_NEW_AWAIT_PULL_SLIP(${pr.state.code})");
      result.code=-1; // Wrong state
      result.message="Unable to locate RES_AWAIT_PICKING OR request not currently RES_NEW_AWAIT_PULL_SLIP(${pr?.state?.code})"
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

    def conditions = PatronRequestLoanCondition.findAllByPatronRequestAndRelevantSupplier(pr, pr.resolvedSupplier)
    conditions.each {cond ->
      cond.setAccepted(true)
      cond.save(flush: true, failOnError: true)
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
    reshareApplicationEventHandlerService.auditEntry(pr, from, to, message, data);
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

      auditEntry(pr,
        pr.state,
        new_state,
        message, null);
      pr.state=new_state;
      pr.save(flush:true, failOnError:true);
      log.debug("Saved new state ${new_state.code} for pr ${pr.id}");
    }
  }


  public boolean sendRequesterReceived(PatronRequest pr, Object actionParams) {
    boolean result = false;

    // Increment the active borrowing counter
    statisticsService.incrementCounter('/activeBorrowing');

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
                                                pr.pickupLocationCode, // pickup_location,
                                                null) // requested_action

        if ( accept_result?.result == true ) {
          // Mark item as awaiting circ
          def new_state = reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_CHECKED_IN');
          // Let the user know if the success came from a real call or a spoofed one
          String message = "Receive succeeded. ${accept_result.reason=='spoofed' ? '(No host LMS integration configured for accept item call)' : 'Host LMS integration: AcceptItem call succeeded.'}"

          auditEntry(pr,
            pr.state,
            new_state,
            message, 
            null);
          pr.state=new_state;
          pr.needsAttention=false;
          pr.save(flush:true, failOnError:true);
          log.debug("Saved new state ${new_state.code} for pr ${pr.id}");
          result = true;
        }
        else {
          String message = 'Host LMS integration: NCIP AcceptItem call failed. Review configuration and try again or deconfigure host LMS integration in settings. '
          // PR-658 wants us to set some state here but doesn't say what that state is. Currently we leave the state as is
          auditEntry(pr,
            pr.state,
            pr.state,
            message+accept_result?.problems, 
            null);
          pr.needsAttention=true;
          pr.save(flush:true, failOnError:true);
        }
      }
      catch ( Exception e ) {
        log.error("NCIP Problem",e);
        pr.needsAttention=true;
        auditEntry(pr, pr.state, pr.state, 'Host LMS integration: NCIP AcceptItem call failed. Review configuration and try again or deconfigure host LMS integration in settings. '+e.message, null);
      }
    } else {
        auditEntry(pr, pr.state, pr.state, 'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.', null);
        pr.needsAttention=true;
        pr.save(flush:true, failOnError:true);
    }
    checkRequestOverdue(pr);
    sendRequestingAgencyMessage(pr, 'Received', actionParams);

    return result;
  }


  /** 
   * At the end of the process, check the item back into the HOST lms
   */
  public boolean checkOutOfReshare(PatronRequest patron_request, Map params) {
    boolean result = false;
    log.debug("checkOutOfReshare(${patron_request?.id}, ${params}");
    try {
      // Call the host lms to check the item out of the host system and in to reshare
      // def accept_result = host_lms.checkInItem(patron_request.hrid)
      HostLMSActions host_lms = hostLMSService.getHostLMSActions();
      if ( host_lms ) {
        def check_in_result = host_lms.checkInItem(patron_request.selectedItemBarcode)
        if(check_in_result?.result == true) {
          statisticsService.decrementCounter('/activeLoans');
          patron_request.needsAttention=false;
          patron_request.activeLoan=false;
          // Let the user know if the success came from a real call or a spoofed one
          String message = "Complete request succeeded. ${check_in_result.reason=='spoofed' ? '(No host LMS integration configured for check in item call)' : 'Host LMS integration: CheckinItem call succeeded.'}"
          auditEntry(patron_request, patron_request.state, patron_request.state, message, null);
          result = true;
        } else {
          patron_request.needsAttention=true;
          auditEntry(patron_request, patron_request.state, patron_request.state, 'Host LMS integration: NCIP CheckinItem call failed. Review configuration and try again or deconfigure host LMS integration in settings. '+check_in_result.problems?.toString(), null);
        }
      } else {
        auditEntry(patron_request, patron_request.state, patron_request.state, 'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.', null);
        patron_request.needsAttention=true;
      }
    }
    catch ( Exception e ) {
      log.error("NCIP Problem",e);
      patron_request.needsAttention=true;
      auditEntry(patron_request,
        patron_request.state,
        patron_request.state,
        'Host LMS integration: NCIP CheckinItem call failed. Review configuration and try again or deconfigure host LMS integration in settings. '+e.message,
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

    // Decrement the active borrowing counter - we are returning the item
    statisticsService.decrementCounter('/activeBorrowing');

    sendRequestingAgencyMessage(pr, 'ShippedReturn', actionParams);
  }

  public boolean sendCancel(PatronRequest pr, String action, Object actionParams) {
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

      log.debug("calling protocolMessageService.sendProtocolMessage(${pr.supplyingInstitutionSymbol},${pr.requestingInstitutionSymbol},${supplying_message_request}) for pr id ${pr.id}");
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
  
  /* 
   * DirectoryEntries have a property customProperties of class com.k_int.web.toolkit.custprops.types.CustomPropertyContainer
   * In turn, the CustomPropertyContainer hasMany values of class com.k_int.web.toolkit.custprops.CustomProperty
   * CustomProperties have a CustomPropertyDefinition, where the name lives, so we filter the list to find the matching custprop
   */
  public CustomProperty extractCustomPropertyFromDirectoryEntry(DirectoryEntry de, String cpName) {
    if (!de || ! cpName) {
      return null
    }
    def custProps = de.customProperties?.value ?: []
    CustomProperty cp = (custProps.find {custProp -> custProp.definition?.name == cpName})
    return cp
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
  
  protected Date parseDateString(String dateString) {
    def formatList = [
      "yyyy-MM-dd'T'HH:mm:ssZ",
      "yyyy-MM-dd HH:mm:ss"
    ];
    Date date = null;
    formatList.any {
      try {
        date = Date.parse(it, dateString); 
      } catch(Exception e) {
        log.debug("Unable to parse date ${dateString} with format '${it}' ${e.getMessage()}");
      }
      if(date != null) {
        return true;
      }      
    }
    if(date == null) {
      throw new Exception("Unable to parse " + dateString + " to date");
    }

  }
}
