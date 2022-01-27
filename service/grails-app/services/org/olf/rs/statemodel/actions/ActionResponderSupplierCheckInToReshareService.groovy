package org.olf.rs.statemodel.actions;

import org.olf.rs.DirectoryEntryService;
import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest;
import org.olf.rs.RequestVolume;
import org.olf.rs.StatisticsService;
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.custprops.CustomProperty;
import com.k_int.web.toolkit.refdata.RefdataValue;
import com.k_int.web.toolkit.settings.AppSetting;

public class ActionResponderSupplierCheckInToReshareService extends AbstractAction {

	HostLMSService hostLMSService;
	DirectoryEntryService directoryEntryService;
	StatisticsService statisticsService;
	
	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

	    boolean result = false;
	
	    if (parameters?.itemBarcodes.size() != 0) {
	        if (request.state.code == Status.RESPONDER_AWAIT_PICKING ||
	            request.state.code == Status.RESPONDER_AWAIT_PROXY_BORROWER ||
	            request.state.code == Status.RESPONDER_AWAIT_SHIP) {
				
		        // TODO For now we still use this, so just set to first item in array for now. Should be removed though
		        request.selectedItemBarcode = parameters?.itemBarcodes[0]?.itemId;
		
		        // We now want to update the patron request's "volumes" field to reflect the incoming params
		        // In order to then use the updated list later, we mimic those actions on a dummy list, 
			    parameters?.itemBarcodes.each { ib ->
			        RequestVolume rv = request.volumes.find {rv -> rv.itemId == ib.itemId};

					// If there's no rv and the delete is true then just skip creation
		            if (!rv && !ib._delete) {
						rv = new RequestVolume(
							name: ib.name ?: request.volume ?: ib.itemId,
							itemId: ib.itemId,
							status: RequestVolume.lookupStatus('awaiting_lms_check_out')
						);
						request.addToVolumes(rv);
					}
			
					if (rv) {
						if (ib._delete && rv.status.value == 'awaiting_lms_check_out') {
							// Remove if deleted by incoming call and NCIP call hasn't succeeded yet
							request.removeFromVolumes(rv);
						} else if (ib.name && rv.name != ib.name) {
							// Allow changing of label up to shipping
							rv.name = ib.name;
						}
					}
					
					// Why do we save at this point ?
					request.save(failOnError: true)
		        }
		
		        // At this point we should have an accurate list of the calls that need to run/have succeeded
		        def volumesNotCheckedIn = request.volumes.findAll {rv ->
		            rv.status.value == 'awaiting_lms_check_out'
		        }
		
		        if (volumesNotCheckedIn.size() > 0) {
					HostLMSActions host_lms = hostLMSService.getHostLMSActions();
					if (host_lms) {
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
			            CustomProperty institutionalPatronId = directoryEntryService.extractCustomPropertyFromDirectoryEntry(request.resolvedRequester?.owner, 'local_institutionalPatronId');
						String institutionalPatronIdValue = institutionalPatronId?.value
						if (!institutionalPatronIdValue) {
							// If nothing on the Directory Entry then fallback to the default in settings
							AppSetting default_institutional_patron_id = AppSetting.findByKey('default_institutional_patron_id')
							institutionalPatronIdValue = default_institutional_patron_id?.value
						}
		
			            // At this point we have a list of NCIP calls to make.
			            // We should make those calls and track which succeeded/failed
			            // TODO perhaps test by inserting a temporary % chance of NCIP failure in manual adapter
	
			            // Store a string and a Date to save onto the request at the end
			            Date parsedDate
			            String stringDate
		
			            // Iterate over volumes not yet checked in in for loop so we can break out if we need to
			            for (def vol : volumesNotCheckedIn) {
				            /*
				             * Be aware that institutionalPatronIdValue here may well be blank or null.
				             * In the case that host_lms == ManualHostLMSService we don't care, we're just spoofing a positive result,
				             * so we delegate responsibility for checking this to the hostLMSService itself, with errors arising in the 'problems' block 
				             */
				            def checkout_result = host_lms.checkoutItem(request.hrid,
				                                                        vol.itemId,
				                                                        institutionalPatronIdValue,
				                                                        request.resolvedRequester);
				
				            // If the host_lms adapter gave us a specific status to transition to, use it
				            if ( checkout_result?.status ) {
					            // the host lms service gave us a specific status to change to
					            actionResultDetails.newStatus = Status.lookup(StateModel.MODEL_RESPONDER, checkout_result?.status);
					            actionResultDetails.auditMessage = "Host LMS integration: NCIP CheckoutItem call failed for itemId: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. "+checkout_result.problems?.toString()
					
					            // We're in a new status, break out of the loop to deal with that,
					            // we can deal with the other checkouts later
					            break;
							}
	
				            // Otherwise, if the checkout succeeded or failed, set appropriately
				            if (checkout_result.result == true) {
					            RefdataValue volStatus = checkout_result.reason=='spoofed' ? vol.lookupStatus('lms_check_out_(no_integration)') : vol.lookupStatus('lms_check_out_complete'); 
					            if (volStatus) {
					                vol.status = volStatus;
					            }
					            vol.save(failOnError: true);
					            reshareActionService.auditEntry(request, request.state, request.state, "Check in to ReShare completed for itemId: ${vol.itemId}. ${checkout_result.reason=='spoofed' ? '(No host LMS integration configured for check out item call)' : 'Host LMS integration: CheckoutItem call succeeded.'}", null);
					
					            // Attempt to store any dueDate coming in from LMS iff it is earlier than what we have stored
					            try {
					                Date tempParsedDate = reshareActionService.parseDateString(checkout_result?.dueDate)
					                if (!request.parsedDueDateFromLMS || parsedDate.before(request.parsedDueDateFromLMS)) {
					                    parsedDate = tempParsedDate;
					                    stringDate = checkout_result?.dueDate;
					                }
					            } catch(Exception e) {
					                log.warn("Unable to parse ${checkout_result?.dueDate} to date: ${e.getMessage()}");
					            }
				            } else {
								reshareActionService.auditEntry(request, request.state, request.state, "Host LMS integration: NCIP CheckoutItem call failed for itemId: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. "+checkout_result.problems?.toString(), null);
							}
						}
		
			            // Save the earliest Date we found as the dueDate
			            request.dueDateFromLMS = stringDate;
			            request.parsedDueDateFromLMS = parsedDate;
			            request.save(flush:true, failOnError:true);
		
						// At this point we should have all volumes checked out. Check that again
						volumesNotCheckedIn = request.volumes.findAll {rv ->
							rv.status.value == 'awaiting_lms_check_out'
						}
		
						Status s = null;
						if (volumesNotCheckedIn.size() == 0) {
							statisticsService.incrementCounter('/activeLoans');
				            request.activeLoan = true
				            request.needsAttention = false;
				            AppSetting useLMSDueDate = AppSetting.findByKey('ncip_use_due_date');
				            if (!request?.dueDateRS && useLMSDueDate?.value != 'off') {
				                request.dueDateRS = request.dueDateFromLMS;
				            }
	
				            try {
				                request.parsedDueDateRS = reshareActionService.parseDateString(request.dueDateRS);
				            } catch(Exception e) {
				                log.warn("Unable to parse ${request.dueDateRS} to date: ${e.getMessage()}");
				            }
	
				            request.overdue=false;
				            actionResultDetails.newStatus = Status.lookup(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP);
				
				            // Log message differs from "fill request" to "add additional items"
				            actionResultDetails.auditMessage = (request.state.code == Status.RESPONDER_AWAIT_PICKING ||
																request.state.code == Status.RESPONDER_AWAIT_PROXY_BORROWER) ?
															 'Fill request completed.' :
															 'Additional items successfully checked in to ReShare';
				            result = true;
						} else {
							// If status is in RES_AWAIT_SHIP, send back to RES_AWAIT_PICKING til all checked in
							if (request.state.code == Status.RESPONDER_AWAIT_SHIP) {
								actionResultDetails.newStatus = Status.lookup(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING);
								actionResultDetails.auditMessage = "One or more items failed to be checked into ReShare, returning to RES_AWAIT_PICKING. Review configuration and try again or deconfigure host LMS integration in settings.";
							} else {
								actionResultDetails.auditMessage = "One or more items failed to be checked into ReShare. Review configuration and try again or deconfigure host LMS integration in settings.";
							}
							request.needsAttention=true;
						}
					} else {
						actionResultDetails.auditMessage = 'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.';
						request.needsAttention=true;
					}
		        } else {
			        // If we have deleted all failing requests, we can move to next state
			        if (request.state.code != Status.RESPONDER_AWAIT_SHIP) {
						actionResultDetails.newStatus = Status.lookup(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP);
						actionResultDetails.auditMessage = 'Fill request completed.';
			        }
	
		            // Result is successful
		            result = true
					log.info("No item ids remain not checked into ReShare, return true");
		        }
		    } else {
				actionResultDetails.auditMessage = "Unable to locate ${Status.RESPONDER_AWAIT_SHIP} OR request not currently {Status.RESPONDER_AWAIT_PICKING} (${request.state.code})";
		        log.warn(actionResultDetails.auditMessage);
			}
	    }
		
		if (result == false) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.responseResult.code = -3; // NCIP action failed

			// Ensure we have a message
			if (actionResultDetails.responseResult.message == null) {
				actionResultDetails.responseResult.message = 'NCIP CheckoutItem call failed.';
			}
		}

		return(actionResultDetails);
	}
}
