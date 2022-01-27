package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

public class ActionResponderRespondYesService extends ActionResponderService {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Check the pickup location and route
		if (validatePickupLocationAndRoute(request, parameters, actionResultDetails).result == ActionResult.SUCCESS) {
			reshareActionService.sendResponse(request, 'ExpectToSupply', parameters);
		}

		return(actionResultDetails);
	}
}
