package org.olf.rs.statemodel.events;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestLoanCondition;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.RequestVolume;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class EventSupplyingAgencyMessageIndService extends AbstractEvent {

	ReshareActionService reshareActionService;
	
	String name() {
		return(Events.EVENT_SUPPLYING_AGENCY_MESSAGE_INDICATION);
	}

	EventFetchRequestMethod fetchRequestMethod() {
		// We are dealing with the transaction directly
		return(EventFetchRequestMethod.HANDLED_BY_EVENT_HANDLER);
	}

	String[] toStates(String model) {
		// We are dealing with the whole model so just return null as no sensible interpretation can be made of what we return
		return(null);
	}
		
	String[] fromStates(String model) {
		// We are dealing with the whole model so just return null as no sensible interpretation can be made of what we return
		return(null);
	}

	boolean supportsModel(String model) {
		// We do not want want this event to appear anywhere 
		return(false);	
	}
	
	EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {

		// In our scenario the request will be null, as we do everything ourselves, so never reference that parameter
		// We use the responseResult field for returning data back to the caller
		
		/**
		 * An incoming message to the requesting agency FROM the supplying agency - so we look in 
		 * eventData.header?.requestingAgencyRequestId to find our own ID for the request.
		 * This should return everything that ISO18626Controller needs to build a confirmation message
		 */

		def result = [:];
		
		/* Occasionally the incoming status is not granular enough, so we deal with it separately in order
		 * to be able to cater to "in-between" statuses, such as Conditional--which actually comes in as "ExpectsToSupply"
		*/
		Map incomingStatus = eventData.statusInfo;
		
		try {
			if (eventData.header?.requestingAgencyRequestId == null) {
				result.status = "ERROR";
				result.errorType = "BadlyFormedMessage";
				throw new Exception("requestingAgencyRequestId missing");
			}

			PatronRequest.withTransaction { status ->
				  
				PatronRequest pr = lookupPatronRequestWithRole(eventData.header.requestingAgencyRequestId, true, true);

				if (pr == null) {
					throw new Exception("Unable to locate PatronRequest corresponding to ID or hrid in requestingAgencyRequestId \"${eventData.header.requestingAgencyRequestId}\"");
				}
		  
				// if eventData.deliveryInfo.itemId then we should stash the item id
				if (eventData?.deliveryInfo ) {
		  
					if (eventData?.deliveryInfo?.loanCondition) {
						log.debug("Loan condition found: ${eventData?.deliveryInfo?.loanCondition}")
						incomingStatus = [status: "Conditional"]

						// Save the loan condition to the patron request
						String loanCondition = eventData?.deliveryInfo?.loanCondition;
						Symbol relevantSupplier = reshareApplicationEventHandlerService.resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue);
						String note = eventData.messageInfo?.note;
		  
						reshareApplicationEventHandlerService.addLoanConditionToRequest(pr, loanCondition, relevantSupplier, note);
					}
		  
					// If we're being told about the barcode of the selected item (and we don't already have one saved), stash it in selectedItemBarcode on the requester side
					if (!pr.selectedItemBarcode && eventData.deliveryInfo.itemId) {
						pr.selectedItemBarcode = eventData.deliveryInfo.itemId;
					}
		  
					// Could recieve a single string or an array here as per the standard/our profile
					def itemId = eventData?.deliveryInfo?.itemId;
					if (itemId) {
						if (itemId instanceof Collection) {
							// Item ids coming in, handle those
							itemId.each {iid ->
								def matcher = iid =~ /multivol:(.*),((?!\s*$).+)/;
								if (matcher.size() > 0) {
									// At this point we have an itemId of the form "multivol:<name>,<id>"
									def iidId = matcher[0][2];
									def iidName = matcher[0][1];
			
									// Check if a RequestVolume exists for this itemId, and if not, create one
									RequestVolume rv = pr.volumes.find {rv -> rv.itemId == iidId };
									if (!rv) {
										rv = new RequestVolume(
											name: iidName ?: pr.volume ?: iidId,
											itemId: iidId,
											status: RequestVolume.lookupStatus('awaiting_temporary_item_creation')
										);
										pr.addToVolumes(rv);
									}
								}
							}
						} else {
							// We have a single string, this is the usual standard case and should be handled as a single request volume
							// Check if a RequestVolume exists for this itemId, and if not, create one
							RequestVolume rv = pr.volumes.find {rv -> rv.itemId == itemId };
							if (!rv) {
								rv = new RequestVolume(
									name: pr.volume ?: itemId,
									itemId: itemId,
									status: RequestVolume.lookupStatus('awaiting_temporary_item_creation')
								);
								pr.addToVolumes(rv);
							}
						}
					}
				}
		  
				// Awesome - managed to look up patron request - see if we can action
				if (eventData.messageInfo?.reasonForMessage != null) {
		  
					// If there is a note, create notification entry
					if (eventData.messageInfo?.note) {
						reshareApplicationEventHandlerService.incomingNotificationEntry(pr, eventData, true);
					}
		  
					switch (eventData.messageInfo?.reasonForMessage) {
						case 'RequestResponse':
							break;
						case 'StatusRequestResponse':
							break;
						case 'RenewResponse':
							break;
						case 'CancelResponse':
							switch (eventData.messageInfo.answerYesNo) {
								case 'Y':
									log.debug("Affirmative cancel response received")
									// The cancel response ISO18626 message should contain a status of "Cancelled", and so this case will be handled by handleStatusChange
									break;
								case 'N':
									log.debug("Negative cancel response received")
									def previousState = reshareApplicationEventHandlerService.lookupStatus('PatronRequest', pr.previousStates[pr.state.code]);
									reshareApplicationEventHandlerService.auditEntry(pr, pr.state, previousState, "Supplier denied cancellation.", null);
									pr.previousStates[pr.state.code] = null;
									pr.state = previousState
									break;
								default:
									log.error("handleSupplyingAgencyMessage does not know how to deal with a CancelResponse answerYesNo of ${eventData.messageInfo.answerYesNo}")
							}
							break;
						case 'StatusChange':
							break;
						case 'Notification':
							// If this note starts with #ReShareAddLoanCondition# then we know that we have to add another loan condition to the request -- might just work automatically.
							reshareApplicationEventHandlerService.auditEntry(pr, pr.state, pr.state, "Notification message received from supplying agency: ${eventData.messageInfo.note}", null);
							break;
						default:
							result.status = "ERROR";
							result.errorType = "UnsupportedReasonForMessageType";
							result.errorValue = eventData.messageInfo.reasonForMessage;
							throw new Exception("Unhandled reasonForMessage: ${eventData.messageInfo.reasonForMessage}");
							break;
					}
				} else {
					result.status = "ERROR";
					result.errorType = "BadlyFormedMessage";
					throw new Exception("No reason for message");
				}
				
				if ( eventData.statusInfo?.dueDate ) {
					pr.dueDateRS = eventData.statusInfo.dueDate;
					try {
						pr.parsedDueDateRS = reshareActionService.parseDateString(pr.dueDateRS);
					} catch(Exception e) {
						log.warn("Unable to parse ${pr.dueDateRS} to date: ${e.getMessage()}");
					}
				} else {
					log.debug("No duedate found in eventData.statusInfo");
				}
		  
				if ( incomingStatus != null ) {
					handleStatusChange(pr, incomingStatus, eventData.header.supplyingAgencyRequestId);
				}
		  
				pr.save(flush:true, failOnError:true);
			}
		
			log.debug("LOCKING: handleSupplyingAgencyMessage transaction has completed");
		
		} catch ( Exception e ) {
			log.error("Problem processing SupplyingAgencyMessage: ${e.message}", e);
		}
		
		if (result.status != "ERROR") {
			result.status = "OK";
		}
		
		result.messageType = "SUPPLYING_AGENCY_MESSAGE";
		result.supIdType = eventData.header.supplyingAgencyId.agencyIdType;
		result.supId = eventData.header.supplyingAgencyId.agencyIdValue;
		result.reqAgencyIdType = eventData.header.requestingAgencyId.agencyIdType;
		result.reqAgencyId = eventData.header.requestingAgencyId.agencyIdValue;
		result.reqId = eventData.header.requestingAgencyRequestId;
		result.timeRec = eventData.header.timestamp;
		result.reasonForMessage = eventData.messageInfo.reasonForMessage;

		// I didn't go through changing everywhere result was mentioned to eventResultDetails.responseResult
		eventResultDetails.responseResult = result;
		return(eventResultDetails);
	}
	
	private PatronRequest lookupPatronRequestWithRole(String id, boolean isRequester, boolean with_lock=false) {

		log.debug("LOCKING ReshareApplicationEventHandlerService::lookupPatronRequestWithRole(${id},${isRequester},${with_lock})");
		PatronRequest result = PatronRequest.createCriteria().get {
			and {
				or {
					eq('id', id)
					eq('hrid', id)
				}
				eq('isRequester', isRequester)
			}
			lock with_lock
		}

		log.debug("LOCKING ReshareApplicationEventHandlerService::lookupPatronRequestWithRole located ${result?.id}/${result?.hrid}");

		return result;
	}
	
	// ISO18626 states are RequestReceived ExpectToSupply WillSupply Loaned Overdue Recalled RetryPossible Unfilled CopyCompleted LoanCompleted CompletedWithoutReturn Cancelled
	private void handleStatusChange(PatronRequest pr, Map statusInfo, String supplyingAgencyRequestId) {
		log.debug("handleStatusChange(${pr.id},${statusInfo})");

		// Get the rota entry for the current peer
		PatronRequestRota prr = pr.rota.find( { it.rotaPosition == pr.rotaPosition } );

		if (statusInfo.status) {
			switch ( statusInfo.status ) {
				case 'ExpectToSupply':
					def new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY);
						reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, 'Protocol message', null);
						pr.state=new_state;
						if (prr != null) {
							 prr.state = new_state;
						}
					break;
				case 'Unfilled':
					def new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNFILLED);
					reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, 'Protocol message', null);
					pr.state=new_state;
					if (prr != null) {
						prr.state = new_state;
					}
					break;
				case 'Conditional':
					log.debug("Moving to state REQ_CONDITIONAL_ANSWER_RECEIVED")
					def new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED);
					reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, 'Protocol message', null);
					pr.state=new_state;
					if (prr != null) {
						prr.state = new_state;
					}
					break;
				case 'Loaned':
					def new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED);
					reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, 'Protocol message', null);
					pr.state=new_state;
					if (prr != null) {
						prr.state = new_state;
					}
					break;
				case 'Overdue':
					def new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE);
					reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, 'Protocol message', null);
					pr.state=new_state;
					if (prr != null) {
						prr.state = new_state;
					}
					break;
				case 'Recalled':
					def new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_RECALLED);
					reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, 'Protocol message', null);
					pr.state=new_state;
					if (prr != null) {
						prr.state = new_state;
					}
					break;
				case 'Cancelled':
					def new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER);
					reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, 'Protocol message', null);
					pr.state=new_state;
					if (prr != null) {
						prr.state = new_state;
					}
					break;
				case 'LoanCompleted':
					def new_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_COMPLETE);
					reshareApplicationEventHandlerService.auditEntry(pr, pr.state, new_state, 'Protocol message', null);
					pr.state=new_state;
					if (prr != null) {
						prr.state = new_state;
					}
					break;
				default:
					log.error("Unhandled statusInfo.status ${statusInfo.status}");
					break;
			}
		}
	}
}
