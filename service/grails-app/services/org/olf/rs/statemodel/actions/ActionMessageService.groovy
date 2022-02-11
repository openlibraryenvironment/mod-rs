package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

public abstract class ActionMessageService extends AbstractAction {

	static String[] TO_STATES = [
								];
	
	@Override
	String name() {
		return(Actions.ACTION_MESSAGE);
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// We must have a note
		if (parameters.note == null) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'No note supplied to send';
		} else {
			// Send the message
	        actionResultDetails.responseResult.status = reshareActionService.sendMessage(request, parameters);
		}
		return(actionResultDetails);
	}
}
