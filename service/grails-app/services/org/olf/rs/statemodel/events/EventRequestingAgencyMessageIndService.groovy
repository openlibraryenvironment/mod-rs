package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class EventRequestingAgencyMessageIndService extends AbstractEvent {

	static String[] fromStates = [
	];
	
	static String[] toStates = [
		Status.RESPONDER_CANCEL_REQUEST_RECEIVED,
		Status.RESPONDER_ITEM_RETURNED
	];
	 	
	String name() {
		return(Events.EVENT_REQUESTING_AGENCY_MESSAGE_INDICATION);
	}

	EventFetchRequestMethod fetchRequestMethod() {
		// We are dealing with the transaction directly
		return(EventFetchRequestMethod.HANDLED_BY_EVENT_HANDLER);
	}

	String[] toStates(String model) {
		return(toStates);
	}
		
	String[] fromStates(String model) {
		return(fromStates);
	}

	boolean supportsModel(String model) {
		// We do not want want this event to appear anywhere 
		return(model == StateModel.MODEL_RESPONDER);	
	}
	
/**
   * An incoming message to the supplying agency from the requesting agency - so we look in 
   * eventData.header?.supplyingAgencyRequestId to find our own ID for the request.
   * This should return everything that ISO18626Controller needs to build a confirmation message
   */
	EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {

		// In our scenario the request will be null, as we do everything ourselves, so never reference that parameter
		// We use the responseResult field for returning data back to the caller
		
		def result = [ : ];
		
		try {
			if (eventData.header?.supplyingAgencyRequestId == null) {
				result.status = "ERROR"
				result.errorType = "BadlyFormedMessage"
				throw new Exception("supplyingAgencyRequestId missing");
			}
		
			PatronRequest.withTransaction { status ->
		  
				PatronRequest pr = lookupPatronRequest(eventData.header.supplyingAgencyRequestId, true);
		
				if (pr == null) {
					log.warn("Unable to locate PatronRequest corresponding to ID or Hrid in supplyingAgencyRequestId \"${eventData.header.supplyingAgencyRequestId}\", trying to locate by peerId.");
					pr = lookupPatronRequestByPeerId(eventData.header.requestingAgencyRequestId, true);
				}
				if (pr == null) {
					throw new Exception("Unable to locate PatronRequest corresponding to peerRequestIdentifier in requestingAgencyRequestId \"${eventData.header.requestingAgencyRequestId}\"");
				} else {
					log.debug("Lookup by peerID successful.");
				}
		  
				// TODO Handle incoming reasons other than notification for RequestingAgencyMessage
				// Needs to look for action and try to do something with that.
				if (eventData.activeSection?.action != null) {
		  
					// If there's a note, create a notification entry
					if (eventData.activeSection?.note != null && eventData.activeSection?.note != "") {
						reshareApplicationEventHandlerService.incomingNotificationEntry(pr, eventData, false);
					}
		  
					switch (eventData.activeSection?.action) {
						case 'Received':
							// Adding an audit entry so we can see what states we are going to for the event
							// Do not commit this uncommented, here to aid seeing what transition changes we allow
//							reshareApplicationEventHandlerService.auditEntry(pr, pr.state, pr.state, "Incomnig message: Received, State change: " + pr.state.code + " -> "  + pr.state.code, null);   

							reshareApplicationEventHandlerService.auditEntry(pr, pr.state, pr.state, "Shipment received by requester", null);
							pr.save(flush: true, failOnError: true);
							break;
							
						case 'ShippedReturn':
							Status new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED);
							pr.volumes?.each {vol ->
								vol.status = vol.lookupStatus('awaiting_lms_check_in');
							}

							// Adding an audit entry so we can see what states we are going to for the event
							// Do not commit this uncommented, here to aid seeing what transition changes we allow
//							reshareApplicationEventHandlerService.auditEntry(pr, pr.state, pr.state, "Incomnig message: ShippedReturn, State change: " + pr.state.code + " -> "  + new_state.code, null);

							reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, "Item(s) Returned by requester", null);
							pr.state = new_state;
							pr.save(flush: true, failOnError: true);
							break;
							
						case 'Notification':
							Map messageData = eventData.activeSection;
		  
							/* If the message is preceded by #ReShareLoanConditionAgreeResponse#
							 * then we'll need to check whether or not we need to change state.
							 */
							Status originalState = pr.state;
							if ((messageData.note != null) &&
								(messageData.note.startsWith("#ReShareLoanConditionAgreeResponse#"))) {
								// First check we're in the state where we need to change states, otherwise we just ignore this and treat as a regular message, albeit with warning
								if (pr.state.code == "RES_PENDING_CONDITIONAL_ANSWER") {
									 pr.state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, pr.previousStates[pr.state.code])

									reshareApplicationEventHandlerService.auditEntry(pr, originalState, pr.state, "Requester agreed to loan conditions, moving request forward", null);
									pr.previousStates[originalState.code] = null;
									reshareApplicationEventHandlerService.markAllLoanConditionsAccepted(pr);
								} else {
									// Loan conditions were already marked as agreed
									reshareApplicationEventHandlerService.auditEntry(pr, pr.state, pr.state, "Requester agreed to loan conditions, no action required on supplier side", null);
								}
							} else {
								reshareApplicationEventHandlerService.auditEntry(pr, pr.state, pr.state, "Notification message received from requesting agency: ${messageData.note}", null);
							}
							
							// Adding an audit entry so we can see what states we are going to for the event
							// Do not commit this uncommented, here to aid seeing what transition changes we allow
//							reshareApplicationEventHandlerService.auditEntry(pr, pr.state, pr.state, "Incomnig message: Notification, State change: " + originalState.code + " -> "  + pr.state.code, null);

							pr.save(flush: true, failOnError: true);
							break;
		  
						case 'Cancel':
							// We cannot cancel a shipped item
							Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED);
							reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, "Requester requested cancellation of the request", null);
							pr.previousStates[Status.RESPONDER_CANCEL_REQUEST_RECEIVED] = pr.state.code;
							
							// Adding an audit entry so we can see what states we are going to for the event
							// Do not commit this uncommented, here to aid seeing what transition changes we allow
//							reshareApplicationEventHandlerService.auditEntry(pr, newState, newState, "Incomnig message: Cancel, State change: " + pr.state.code + " -> "  + newState.code, null);

							pr.state = newState;
							pr.save(flush: true, failOnError: true);
							break;
		  
						default:
							result.status = "ERROR";
							result.errorType = "UnsupportedActionType";
							result.errorValue = eventData.activeSection.action;
							throw new Exception("Unhandled action: ${eventData.activeSection.action}");
							break;
					}
				} else {
					result.status = "ERROR";
					result.errorType = "BadlyFormedMessage";
					throw new Exception("No action in active section");
				}
			}
			log.debug("LOCKING: handleRequestingAgencyMessage transaction has completetd");
		} catch ( Exception e ) {
			log.error("Problem processing RequestingAgencyMessage: ${e.message}", e);
		}
		
		if (result.status != "ERROR") {
			result.status = "OK"
		}
		
		result.messageType = "REQUESTING_AGENCY_MESSAGE";
		result.supIdType = eventData.header.supplyingAgencyId.agencyIdType;
		result.supId = eventData.header.supplyingAgencyId.agencyIdValue;
		result.reqAgencyIdType = eventData.header.requestingAgencyId.agencyIdType;
		result.reqAgencyId = eventData.header.requestingAgencyId.agencyIdValue;
		result.reqId = eventData.header.requestingAgencyRequestId;
		result.timeRec = eventData.header.timestamp;
		result.action = eventData.activeSection?.action;
		
		// I didn't go through changing everywhere result was mentioned to eventResultDetails.responseResult
		eventResultDetails.responseResult = result;
		return(eventResultDetails);
	}
	
	/** We aren't yet sure how human readable IDs will pan out in the system and there is a desire to use
	 * HRIDs as the requesting agency ID instead of a UUID. For now, isolating all the request lookup functionality
	 * in this method - which will try both approaches to give us some flexibility in adapting to different schemes.
	 * @Param  id - a UUID OR a HRID String
	 * IMPORTANT: If calling with_lock true the caller must establish transaction boundaries
	 */
	private PatronRequest lookupPatronRequest(String id, boolean with_lock=false) {
  
		PatronRequest result = PatronRequest.createCriteria().get {
			or {
				eq('id', id)
				eq('hrid', id)
			}
			lock with_lock
		};
		return result;
	}
	
	private PatronRequest lookupPatronRequestByPeerId(String id, boolean with_lock) {
		
		PatronRequest result = PatronRequest.createCriteria().get {
			eq('peerRequestIdentifier', id)
			lock with_lock
		};
		return result;
	}
}
