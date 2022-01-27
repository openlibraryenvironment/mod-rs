package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierPrintPullSlipService extends AbstractAction {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Get hold of the new status
	    Status status = Status.lookup(StateModel.getStateModel(request.isRequester).shortcode, Status.RESPONDER_AWAIT_PICKING);
	    if (status && request.state.code == Status.RESPONDER_NEW_AWAIT_PULL_SLIP) {
			// Managed to get hold of the new status and we are in the correct state in the first place
			actionResultDetails.auditMessage = 'Pull slip printed';
			actionResultDetails.newStatus = status;
			actionResultDetails.responseResult.status = true;
	    } else {
			// Either we couldn't get the new state or we were not in the correct state in the first place
			actionResultDetails.auditMessage = "Unable to locate ${Status.RESPONDER_AWAIT_PICKING} OR request status not currently ${Status.RESPONDER_NEW_AWAIT_PULL_SLIP} it is (${request?.state?.code})";
		    actionResultDetails.responseResult.code = -1; // Wrong state
		    actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
			actionResultDetails.responseResult.status = false;
		    log.warn(actionResultDetails.auditMessage);
	    }

		return(actionResultDetails);
	}
}
