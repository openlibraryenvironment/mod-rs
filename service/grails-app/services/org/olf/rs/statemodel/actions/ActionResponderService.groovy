package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

public abstract class ActionResponderService extends AbstractAction {

	/**
	 * Performs check that the pick location has been supplied and routes it to this location
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters passed into the action
	 * @param actionResultDetails If successful there is no change
	 * @return The actionResultDetails 
	 */
	protected ActionResultDetails validatePickupLocationAndRoute(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// were we supplied with the location details
		if (parameters?.pickLocation != null) {
			// We have been supplied the item location details
			ItemLocation location = new ItemLocation(location: parameters.pickLocation,
													 shelvingLocation: parameters.pickShelvingLocation,
													 callNumber: parameters.callnumber);

			if (!reshareApplicationEventHandlerService.routeRequestToLocation(request, location)) {
				actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
				actionResultDetails.auditMessage = 'Failed to route request to given location';
				actionResultDetails.responseResult.code = -2; // No location specified
				actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
			}
		} else {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'No pick location specified. Unable to continue';
			actionResultDetails.responseResult.code = -1; // No location specified
			actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
		}

		return(actionResultDetails);
	}
}
