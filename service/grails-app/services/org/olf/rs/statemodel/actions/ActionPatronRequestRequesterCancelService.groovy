package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.refdata.RefdataCategory;
import com.k_int.web.toolkit.refdata.RefdataValue;

public class ActionPatronRequestRequesterCancelService extends AbstractAction {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Do we have a reason
		if (parameters.reason) {
			def cat = RefdataCategory.findByDesc('cancellationReasons');
			def val = RefdataValue.findByOwnerAndValue(cat, parameters.reason);
			if  (val) {
				request.cancellationReason = val;
			}
		}

		// If we do not already have a resolved supplier in hand we cannot send ISO18626 messages
		if (request.resolvedSupplier?.id) {
			request.previousStates[Status.PATRON_REQUEST_CANCEL_PENDING] = request.state.code;
			reshareActionService.sendCancel(request, "requesterCancel", parameters)
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING);
		} else {
			// In this case, just directly send request to state "cancelled"
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED);
		}

		return(actionResultDetails);
	}
}
