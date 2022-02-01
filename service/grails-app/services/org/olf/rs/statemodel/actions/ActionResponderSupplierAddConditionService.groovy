package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierAddConditionService extends ActionResponderConditionService {

	static String[] TO_STATES = [
								 Status.RESPONDER_PENDING_CONDITIONAL_ANSWER
								];
	
	@Override
	String name() {
		return("supplierAddCondition");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Add the condition and send it to the requester
		Map conditionParams = parameters
	
		if (!parameters.isNull("note")){
			conditionParams.note = "#ReShareAddLoanCondition# ${parameters.note}"
		} else {
			conditionParams.note = "#ReShareAddLoanCondition#";
		}

		if (!conditionParams.isNull("loanCondition")) {
			reshareActionService.sendMessage(request, conditionParams);
		} else {
			log.warn("addCondition not handed any conditions");
		}

		// Send over the supplier conditional warning	
		sendSupplierConditionalWarning(request, parameters);
		
		// Do we need to hold the request
		if (parameters.isNull('holdingState') || parameters.holdingState == "no") {
			// The supplying agency wants to continue with the request
			actionResultDetails.auditMessage = 'Added loan condition to request, request continuing';
		} else {
			// The supplying agency wants to go into a holding state
			request.previousStates.put(Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, request.state.code)
			actionResultDetails.auditMessage = 'Condition added to request, placed in hold state';
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER);
		}
		return(actionResultDetails);
	}
}
