package org.olf.rs.statemodel.actions;

import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest;
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestRequesterReceivedService extends AbstractAction {

	HostLMSService hostLMSService;
	
	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_CHECKED_IN
								];
	
	@Override
	String name() {
		return(Actions.ACTION_REQUESTER_REQUESTER_RECEIVED);
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {
		
		boolean ncipResult = false;

		// Increment the active borrowing counter
		statisticsService.incrementCounter('/activeBorrowing');

		// Check the item in to the local LMS
		HostLMSActions host_lms = hostLMSService.getHostLMSActions();
		if ( host_lms ) {
			def volumesWithoutTemporaryItem = request.volumes.findAll {rv ->
				rv.status.value == 'awaiting_temporary_item_creation'
			}
			// Iterate over volumes without temp item in for loop so we can break out if we need to
			for (def vol : volumesWithoutTemporaryItem) {
				try {
					// Item Barcode - using Request human readable ID + volId for now
					// If we only have one volume, just use the HRID
					def temporaryItemBarcode = null;
					if(request.volumes?.size() > 1) {
						temporaryItemBarcode = "${request.hrid}-${vol.itemId}";
					} else {
						temporaryItemBarcode = request.hrid;
					}

					// Call the host lms to check the item out of the host system and in to reshare
					Map accept_result = host_lms.acceptItem(
						temporaryItemBarcode,
                        request.hrid,
                        request.patronIdentifier, // user_idA
                        request.author, // author,
                        request.title, // title,
                        request.isbn, // isbn,
                        request.localCallNumber, // call_number,
                        request.resolvedPickupLocation?.lmsLocationCode, // pickup_location,
                        null); // requested_action

					if (accept_result?.result == true) {
						// Let the user know if the success came from a real call or a spoofed one
						String message = "Receive succeeded for item id: ${vol.itemId}. ${accept_result.reason=='spoofed' ? '(No host LMS integration configured for accept item call)' : 'Host LMS integration: AcceptItem call succeeded.'}";
						def newVolState = accept_result.reason=='spoofed' ? vol.lookupStatus('temporary_item_creation_(no_integration)') : vol.lookupStatus('temporary_item_created_in_host_lms');

						reshareApplicationEventHandlerService.auditEntry(request,
							request.state,
							request.state,
							message, 
							null);
						vol.status = newVolState;
						vol.save(failOnError: true);
					} else {
						String message = "Host LMS integration: NCIP AcceptItem call failed for item: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. ";
						// PR-658 wants us to set some state here but doesn't say what that state is. Currently we leave the state as is.
						// IF THIS NEEDS TO GO INTO ANOTHER STATE, WE SHOULD DO IT AFTER ALL VOLS HAVE BEEN ATTEMPTED
						reshareApplicationEventHandlerService.auditEntry(request,
							request.state,
							request.state,
							message + accept_result?.problems, 
							null);
					}
				} catch (Exception e) {
					log.error("NCIP Problem",e);
					reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, "Host LMS integration: NCIP AcceptItem call failed for item: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message, null);
				}
			}
		    request.save(flush:true, failOnError:true);

			// At this point we should have all volumes' temporary items created. Check that again
			volumesWithoutTemporaryItem = request.volumes.findAll {rv ->
				rv.status.value == 'awaiting_temporary_item_creation'
			}

			if (volumesWithoutTemporaryItem.size() == 0) {
				// Mark item as awaiting circ
				actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN);
				// Let the user know if the success came from a real call or a spoofed one
				actionResultDetails.auditMessage = "Host LMS integration: AcceptItem call succeeded for all items.";

				request.needsAttention=false;
				ncipResult = true;
				reshareActionService.sendRequestingAgencyMessage(request, 'Received', parameters);
			} else {
				actionResultDetails.auditMessage = "Host LMS integration: AcceptItem call failed for some items.";
				request.needsAttention=true;
			}
		} else {
			actionResultDetails.auditMessage = 'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.';
			request.needsAttention=true;
		}

		// Take into account if we failed on the ncip message
		if (!ncipResult) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			if (actionResultDetails.auditMessage != null) {
				actionResultDetails.auditMessage = 'NCIP AcceptItem call failed';
			}
			actionResultDetails.responseResult.code = -3; // NCIP action failed
			actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
		};

		return(actionResultDetails);
	}
}
