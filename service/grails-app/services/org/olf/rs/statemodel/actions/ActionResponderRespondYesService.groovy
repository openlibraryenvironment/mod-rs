package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Status;

public class ActionResponderRespondYesService extends ActionResponderService {

	static String[] TO_STATES = [
		Status.RESPONDER_NEW_AWAIT_PULL_SLIP
	];
	
	@Override
	String name() {
		return("respondYes");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Check the pickup location and route
		if (validatePickupLocationAndRoute(request, parameters, actionResultDetails).result == ActionResult.SUCCESS) {
			// Status is set to Status.RESPONDER_NEW_AWAIT_PULL_SLIP in validatePickupLocationAndRoute
			reshareActionService.sendResponse(request, 'ExpectToSupply', parameters);
		}

		return(actionResultDetails);
	}
}
