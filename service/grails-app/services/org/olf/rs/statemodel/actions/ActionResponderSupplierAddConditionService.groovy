package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Executes adding a condition on to the request for the responder
 * @author Chas
 *
 */
public class ActionResponderSupplierAddConditionService extends ActionResponderConditionService {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Add the condition and send it to the requester
        Map conditionParams = parameters

        if (parameters.isNull('note')) {
            conditionParams.note = '#ReShareAddLoanCondition#';
        } else {
            conditionParams.note = "#ReShareAddLoanCondition# ${parameters.note}"
        }

        if (conditionParams.isNull('loanCondition')) {
            log.warn('addCondition not handed any conditions');
        } else {
            reshareActionService.sendMessage(request, conditionParams, actionResultDetails);
        }

        // Send over the supplier conditional warning
        sendSupplierConditionalWarning(request, parameters, actionResultDetails);

        // Do we need to hold the request
        if (parameters.isNull('holdingState') || parameters.holdingState == 'no') {
            // The supplying agency wants to continue with the request
            actionResultDetails.auditMessage = 'Added loan condition to request, request continuing';
        } else {
            // The supplying agency wants to go into a holding state
//            request.previousStates.put(Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, request.state.code)
            actionResultDetails.auditMessage = 'Condition added to request, placed in hold state';
            actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER);
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_HOLDING
        }
        return(actionResultDetails);
    }
}
