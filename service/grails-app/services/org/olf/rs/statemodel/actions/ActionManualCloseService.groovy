package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public abstract class ActionManualCloseService extends AbstractAction {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		if (!(parameters?.terminalState ==~ /[A-Z_]+/)) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = "Attemped manualClose action with state containing invalid character: ${parameters?.terminalState}";
		} else {
			Status closeStatus = Status.lookup(request.isRequester ? StateModel.MODEL_REQUESTER : StateModel.MODEL_RESPONDER, parameters?.terminalState);

			// Have we been supplied a valid close status	  
			if (closeStatus && closeStatus.terminal) {
				reshareActionService.sendMessage(request, [note: "The ${request.isRequester ? StateModel.MODEL_REQUESTER : StateModel.MODEL_RESPONDER} has manually closed this request."]);
				actionResultDetails.auditMessage = "Manually closed";
				actionResultDetails.newStatus = closeStatus;
			} else {
				actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
				actionResultDetails.auditMessage = "Attemped manualClose action with non-terminal state: ${s.toString()} ${parameters?.terminalState}";
			}
		}  

		// Set the response status
        actionResultDetails.responseResult.status = ((actionResultDetails.result == ActionResult.SUCCESS) ? true : false);

		return(actionResultDetails);
	}
}
