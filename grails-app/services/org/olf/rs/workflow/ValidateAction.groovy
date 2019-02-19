package org.olf.rs.workflow

import org.olf.rs.PatronRequest;
import org.olf.rs.workflow.AbstractAction.ActionResponse;


class ValidateAction extends AbstractAction {

	/** Returns the action that this class represents */
	@Override
	String GetActionCode() {
		return(Action.VALIDATE);
	}

	/** Performs the action */
	@Override
	ActionResponse perform(PatronRequest requestToBeProcessed) {
		// For the time being we just return OK as we do not do anything
		return(ActionResponse.SUCCESS);
	}
}
