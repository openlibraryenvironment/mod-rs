package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.refdata.RefdataCategory;
import com.k_int.web.toolkit.refdata.RefdataValue;

public class ActionPatronRequestCancelLocalService extends AbstractAction {

	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_CANCELLED
								];
	
	@Override
	String name() {
		return("cancelLocal");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	Boolean canLeadToSameState() {
	    // We do not return the same state, so we need to override and return false
		return(false);
	}
	
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
