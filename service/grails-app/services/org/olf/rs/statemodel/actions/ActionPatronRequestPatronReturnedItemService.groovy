package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import org.olf.rs.HostLMSService;
import com.k_int.web.toolkit.settings.AppSetting;

public class ActionPatronRequestPatronReturnedItemService extends AbstractAction {

	HostLMSService hostLMSService;

	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING
								];
	
	@Override
	String name() {
		return(Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM);
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	Boolean canLeadToSameState() {
	    // We do not return the same state, so we need to override and return false
		return(false);
	}
	
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Just set the status
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING);
		actionResultDetails.responseResult.status = true;

		AppSetting check_in_on_return = AppSetting.findByKey('check_in_on_return');


		if( check_in_on_return?.value != 'off' ) {
			log.debug("Attempting NCIP CheckInItem for volumes for request {$request?.id}");
			Map result_map = [:];
			try {
				result_map = hostLMSService.checkInRequestVolumes(request);
			} catch( Exception e) {
				log.error("Error attempting NCIP CheckinItem for request {$request.id}: {$e}");	
				result_map.result = false;
			}
			if(result_map.result) {
				log.debug("Successfully checked in volumes for request {$request.id}")
			} else {
				log.debug("Failed to check in volumes for request {$request.id}")
			}
		}
		
		return(actionResultDetails);
	}
}
