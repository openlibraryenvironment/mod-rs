package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestLoanCondition;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestRequesterAgreeConditionsService extends AbstractAction {

	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY
								];
	
	@Override
	String name() {
		return("requesterAgreeConditions");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// If we are not the requester, flag it as an error
		if (!request.isRequester) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'Only the responder can accept the conditions';
		} else {
			String responseKey = "#ReShareLoanConditionAgreeResponse#";
	
			if (parameters.isNull("note")) {
				parameters.note = responseKey;
			} else {
				parameters.note = "${responseKey} ${parameters.note}";
			}
		
			// Inform the responder
			reshareActionService.sendRequestingAgencyMessage(request, "Notification", parameters);
	
			def conditions = PatronRequestLoanCondition.findAllByPatronRequestAndRelevantSupplier(request, request.resolvedSupplier);
			conditions.each {condition ->
				condition.setAccepted(true);
				condition.save(flush: true, failOnError: true);
			}
			
			actionResultDetails.auditMessage = 'Agreed to loan conditions';
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY);
		}

		return(actionResultDetails);
	}
}
