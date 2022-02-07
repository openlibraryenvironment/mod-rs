package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestNotification;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

public abstract class ActionMessageSeenService extends AbstractAction {

	static String[] TO_STATES = [
								];

	@Override
	String name() {
		return("messageSeen");
	}
	
	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// We must have an id
		if (parameters.id == null) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'No message id supplied to mark as seen';
		} else if (parameters.seenStatus == null) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'No seenStatus supplied to mark as seen';
		} else {
			PatronRequestNotification message = PatronRequestNotification.findById(parameters.id)
			if (message == null) {
				actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
				actionResultDetails.auditMessage = 'Message with id: ' + parameters.id + ' does not exist';
			} else {
				message.setSeen(parameters.seenStatus)
				message.save(flush:true, failOnError:true)
			}
		}

		actionResultDetails.responseResult.status = (actionResultDetails.result == ActionResult.SUCCESS ? true : false);
		return(actionResultDetails);
	}
}