package org.olf.rs.statemodel.actions;

import org.olf.rs.DirectoryEntryService;
import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest;
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierCheckOutOfReshareService extends AbstractAction {

	HostLMSService hostLMSService;
	DirectoryEntryService directoryEntryService;

	static String[] TO_STATES = [
								 Status.RESPONDER_COMPLETE
								];
	
	@Override
	String name() {
		return("supplierCheckOutOfReshare");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		boolean result = false;
	
		def volumesNotCheckedOut = request.volumes.findAll {rv ->
		  rv.status.value == 'awaiting_lms_check_in';
		}
		try {
			// Call the host lms to check the item out of reshare and in to the host system
			HostLMSActions host_lms = hostLMSService.getHostLMSActions();
			if (host_lms) {
				// Iterate over volumes not yet checked out in for loop so we can break out if we need to
				for (def vol : volumesNotCheckedOut) {
					def check_in_result = host_lms.checkInItem(vol.itemId)
					if (check_in_result?.result == true) {
						String message;
						if(check_in_result?.already_checked_in == true) {	
							message = "NCIP CheckinItem call succeeded for item: ${vol.itemId}. ${check_in_result.reason=='spoofed' ? '(No host LMS integration configured for check in item call)' : 'Host LMS integration: CheckinItem not performed because the item was already checked in.'}"
						} else {
							message = "NCIP CheckinItem call succeeded for item: ${vol.itemId}. ${check_in_result.reason=='spoofed' ? '(No host LMS integration configured for check in item call)' : 'Host LMS integration: CheckinItem call succeeded.'}"
						}
						reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, message, null);
				
						def newVolStatus = check_in_result.reason=='spoofed' ? vol.lookupStatus('lms_check_in_(no_integration)') : vol.lookupStatus('completed')
						vol.status = newVolStatus
						vol.save(failOnError: true)
					} else {
						request.needsAttention=true;
						reshareApplicationEventHandlerService.auditEntry(
							request,
							request.state,
							request.state,
							"Host LMS integration: NCIP CheckinItem call failed for item: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. "+check_in_result.problems?.toString(),
							null);
					}
				}
			} else {
				reshareApplicationEventHandlerService.auditEntry(
					request,
					request.state,
					request.state,
					'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.',
					null);
				request.needsAttention=true;
			}
		}
		catch ( Exception e ) {
			log.error("NCIP Problem",e);
			request.needsAttention=true;
			reshareApplicationEventHandlerService.auditEntry(
				request,
				request.state,
				request.state,
				"Host LMS integration: NCIP CheckinItem call failed for item: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message,
				null);
		  	result = false;
		}
	
		// At this point we should have all volumes checked out. Check that again
		volumesNotCheckedOut = request.volumes.findAll {rv ->
			rv.status.value == 'awaiting_lms_check_in';
		}
	
		if (volumesNotCheckedOut.size() == 0) {
			statisticsService.decrementCounter('/activeLoans');
			request.needsAttention = false;
			request.activeLoan = false;
			
			// Let the user know if the success came from a real call or a spoofed one
			String message = "Complete request succeeded. Host LMS integration: CheckinItem call succeeded for all items.";
			reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, message, null);
			result = true;
		} else {
			String message = "Host LMS integration: NCIP CheckinItem calls failed for some items."
			reshareApplicationEventHandlerService.auditEntry(request,
				request.state,
				request.state,
				message,
				null);
			request.needsAttention = true;
		}
	
		if (result == false) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'NCIP CheckinItem call failed';
			actionResultDetails.responseResult.code = -3; // NCIP action failed
			actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
			actionResultDetails.responseResult.status = false;
        } else {
            log.debug("supplierCheckOutOfReshare::transition and send status change");
            reshareActionService.sendStatusChange(request, "LoanCompleted", parameters?.note);
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_COMPLETE);
			actionResultDetails.responseResult.status = false;
        }

		return(actionResultDetails);
	}
}
