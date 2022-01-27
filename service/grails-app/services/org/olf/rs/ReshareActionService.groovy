package org.olf.rs;

import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import com.k_int.web.toolkit.refdata.RefdataValue
import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestRota
import org.olf.rs.RequestVolume
import org.olf.rs.PatronRequestNotification
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.StateModel
import org.olf.okapi.modules.directory.Symbol;
import groovy.json.JsonOutput;
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneOffset
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.rs.lms.HostLMSActions;
import com.k_int.web.toolkit.settings.AppSetting;
import com.k_int.web.toolkit.custprops.CustomProperty;
import org.olf.rs.patronstore.PatronStoreActions;

/**
 * Handle user events.
 *
 * wheras ReshareApplicationEventHandlerService is about detecting and handling
 * system generated events - incoming protocol messages etc this class is the
 * home for user triggered activities - checking an item into reshare, marking
 * the pull slip as printed etc.
 */
public class ReshareActionService {

	ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
	ProtocolMessageService protocolMessageService
	ProtocolMessageBuildingService protocolMessageBuildingService
	HostLMSService hostLMSService
	StatisticsService statisticsService
	PatronStoreService patronStoreService

	/*
	 * WARNING: this method is NOT responsible for saving or for managing state
	 * changes. It simply performs the lookupAction and appends relevant info to the
	 * patron request
	 */
	public Map lookupPatron(PatronRequest pr, Map actionParams) {
    if(patronStoreService) {
      log.debug("Patron Store Services are initialized");
    } else {
      log.error("Patron Store Services are not initialized");
    }
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
    PatronStoreActions patronStoreActions;
    patronStoreActions = patronStoreService.getPatronStoreActions();
    log.debug("patronStoreService is currently ${patronStoreService}");
    boolean updateOrCreatePatronStore = false;
    try {
      updateOrCreatePatronStore = patronStoreActions.updateOrCreatePatronStore(patron_details.userid, patron_details);
    } catch(Exception e) {
      log.error("Unable to update or create Patron Store: ${e}");
    }
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

	public boolean supplierCannotSupply(PatronRequest pr, Map actionParams) {
		boolean result = false;
		log.debug("supplierCannotSupply(${pr})");
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

	/**
   *  send a message.
   *  It appears this method can be called from multiple places including controllers and other services.
   *  Previously, we relied upon groovy magic to allow actionParams be a controller params object or a standard
   *  map. However, a standard map does not support isNull. In order to detect and tidy this, the method signture
   *  is changed to an explicit Map and the test for a note property is done via the map interface and not
   *  the special isNull method injected by the controller object (Which then breaks this method if called from another service).
   */
  public boolean sendMessage(PatronRequest pr, Map actionParams) {
    log.debug("actionMessage(${pr})");
    boolean result = false;
    // Sending a message does not change the state of a request

    // If the actionParams does not contain a note then this method should do nothing
    if ( actionParams.get('note') != null ) {
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

      if ( result == false) {
        log.warn("Unable to send protocol notification message");
      }
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
		if (combinedString != null) {
			String[] name_components = combinedString.split(':');
			if (name_components.length == 2) {
				result = resolveSymbol(name_components[0], name_components[1]);
			}
		}
		return result;
	}

	public simpleTransition(PatronRequest pr, Map params, String state_model, String target_status, String p_message=null) {

    if( pr == null ) {
      log.warn("Cannot transition status for null request object");
      return;
    }
    
    log.debug("request to transition ${pr} to ${state_model}/${target_status}");
    def new_state = reshareApplicationEventHandlerService.lookupStatus(state_model, target_status);

    if (  new_state != null )  {
      String message = p_message ?: "Simple Transition ${pr.state?.code} to ${new_state.code}".toString()

      auditEntry(pr,
        pr.state,
        new_state,
        message, null);
      pr.state=new_state;
      pr.save(flush:true, failOnError:true);
      log.debug("Saved new state ${new_state.code} for pr ${pr.id}");
    } else {
      log.warn("Unable to find state for state model ${state_model}, state name ${target_status}");
    }
  }

	public boolean sendRequesterReceived(PatronRequest pr, Object actionParams) {
    boolean result = false;

    // Increment the active borrowing counter
    statisticsService.incrementCounter('/activeBorrowing');

    // Check the item in to the local LMS
    HostLMSActions host_lms = hostLMSService.getHostLMSActions();
    if ( host_lms ) {
      def volumesWithoutTemporaryItem = pr.volumes.findAll {rv ->
        rv.status.value == 'awaiting_temporary_item_creation'
      }
      // Iterate over volumes without temp item in for loop so we can break out if we need to
      for (def vol : volumesWithoutTemporaryItem) {
        try {
          // Item Barcode - using Request human readable ID + volId for now
          // If we only have one volume, just use the HRID
          def temporaryItemBarcode = null;
          if(pr.volumes?.size() > 1) {
            temporaryItemBarcode = "${pr.hrid}-${vol.itemId}";
          } else {
            temporaryItemBarcode = pr.hrid;
          }

          // Call the host lms to check the item out of the host system and in to reshare
          Map accept_result = host_lms.acceptItem(temporaryItemBarcode,
                                                  pr.hrid,
                                                  pr.patronIdentifier, // user_idA
                                                  pr.author, // author,
                                                  pr.title, // title,
                                                  pr.isbn, // isbn,
                                                  pr.localCallNumber, // call_number,
                                                  pr.resolvedPickupLocation?.lmsLocationCode, // pickup_location,
                                                  null) // requested_action

          if ( accept_result?.result == true ) {
            // Let the user know if the success came from a real call or a spoofed one
            String message = "Receive succeeded for item id: ${vol.itemId}. ${accept_result.reason=='spoofed' ? '(No host LMS integration configured for accept item call)' : 'Host LMS integration: AcceptItem call succeeded.'}"
            def newVolState = accept_result.reason=='spoofed' ? vol.lookupStatus('temporary_item_creation_(no_integration)') : vol.lookupStatus('temporary_item_created_in_host_lms')

            auditEntry(pr,
              pr.state,
              pr.state,
              message, 
              null);
            vol.status=newVolState;
            vol.save(failOnError: true)
          }
          else {
            String message = "Host LMS integration: NCIP AcceptItem call failed for item: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. "
            // PR-658 wants us to set some state here but doesn't say what that state is. Currently we leave the state as is.
            // IF THIS NEEDS TO GO INTO ANOTHER STATE, WE SHOULD DO IT AFTER ALL VOLS HAVE BEEN ATTEMPTED
            auditEntry(pr,
              pr.state,
              pr.state,
              message+accept_result?.problems, 
              null);
          }
        }
        catch ( Exception e ) {
          log.error("NCIP Problem",e);
          auditEntry(pr, pr.state, pr.state, "Host LMS integration: NCIP AcceptItem call failed for item: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. "+e.message, null);
        }
      }
      pr.save(flush:true, failOnError:true);

      // At this point we should have all volumes' temporary items created. Check that again
      volumesWithoutTemporaryItem = pr.volumes.findAll {rv ->
        rv.status.value == 'awaiting_temporary_item_creation'
      }

      if (volumesWithoutTemporaryItem.size() == 0) {
        // Mark item as awaiting circ
        def new_state = reshareApplicationEventHandlerService.lookupStatus('PatronRequest', 'REQ_CHECKED_IN');
        // Let the user know if the success came from a real call or a spoofed one
        String message = "Host LMS integration: AcceptItem call succeeded for all items."

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
        sendRequestingAgencyMessage(pr, 'Received', actionParams);
      } else {
        String message = "Host LMS integration: AcceptItem call failed for some items."
        auditEntry(pr,
          pr.state,
          pr.state,
          message,
          null);
        pr.needsAttention=true;
        pr.save(flush:true, failOnError:true);
      }
    } else {
        auditEntry(pr, pr.state, pr.state, 'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.', null);
        pr.needsAttention=true;
        pr.save(flush:true, failOnError:true);
    }

    return result;
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
    pr.save(failOnError:true);
    
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

	// see
	// http://biblstandard.dk/ill/dk/examples/request-without-additional-information.xml
	// http://biblstandard.dk/ill/dk/examples/supplying-agency-message-delivery-next-day.xml
	// RequestReceived, ExpectToSupply, WillSupply, Loaned, Overdue, Recalled,
	// RetryPossible,
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
    if (dateString == null) {
      throw new Exception("Attempted to parse null as date")
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd[ ]['T']HH:mm[:ss][.SSS][z][XXX][Z]")
    Date date
    try {
      date = Date.from(ZonedDateTime.parse(dateString, formatter).toInstant())
    } catch(Exception e) {
      log.debug("Failed to parse ${dateString} as ZonedDateTime, falling back to LocalDateTime")
      date = Date.from(LocalDateTime.parse(dateString, formatter).toInstant(ZoneOffset.UTC))
    }
    return date
  }
}
