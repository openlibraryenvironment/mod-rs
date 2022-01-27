package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierMarkConditionsAgreedService extends ActionResponderService {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {
		
		// Mark all conditions as accepted
		reshareApplicationEventHandlerService.markAllLoanConditionsAccepted(request)

		actionResultDetails.auditMessage = 'Conditions manually marked as agreed';
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, request.previousStates[Status.RESPONDER_PENDING_CONDITIONAL_ANSWER]);

		// No longer need to have the state saved prior to the conditions being added
		request.previousStates[Status.RESPONDER_PENDING_CONDITIONAL_ANSWER] = null;
		
		return(actionResultDetails);
	}
}
