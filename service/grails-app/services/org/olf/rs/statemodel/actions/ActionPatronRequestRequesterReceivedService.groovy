package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

public class ActionPatronRequestRequesterReceivedService extends AbstractAction {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {
		
		// This will trigger an NCIP acceptItem as well
		if (!reshareActionService.sendRequesterReceived(request, parameters)) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'NCIP AcceptItem call failed';
			actionResultDetails.responseResult.code = -3; // NCIP action failed
			actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
		};

		return(actionResultDetails);
	}
}
