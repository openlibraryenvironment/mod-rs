package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestBorrowerCheckService extends AbstractAction {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Performs a lookup against the patron
		Map borrower_check = reshareActionService.lookupPatron(request, parameters);
		actionResultDetails.responseResult.status = borrower_check?.callSuccess && borrower_check?.patronValid
		if (actionResultDetails.responseResult.status) {
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED);
		} else if (!borrower_check?.callSuccess) {
			// The Host LMS check call has failed, stay in current state
			request.needsAttention = true;
			actionResultDetails.auditMessage = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. ' + borrower_check?.problems;
		} else {
			// The call succeeded but patron is invalid
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON);
			actionResultDetails.auditMessage = "Failed to validate patron with id: \"${request.patronIdentifier}\".${borrower_check?.status != null ? " (Patron state = ${borrower_check.status})" : ""}";
			request.needsAttention = true;
		}

		return(actionResultDetails);
	}
}
