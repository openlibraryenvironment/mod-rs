package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.refdata.RefdataCategory;
import com.k_int.web.toolkit.refdata.RefdataValue;

public class ActionPatronRequestCancelLocalService extends AbstractAction {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED);
		actionResultDetails.auditMessage = "Local request cancelled";
		if (parameters.reason) {
			def cat = RefdataCategory.findByDesc('cancellationReasons');
			def reason = RefdataValue.findByOwnerAndValue(cat, parametes.reason);
			if (reason) {
				request.cancellationReason = reason;
				actionResultDetails.auditMessage += ": ${reason}";
			}
		}
	
		return(actionResultDetails);
	}
}
