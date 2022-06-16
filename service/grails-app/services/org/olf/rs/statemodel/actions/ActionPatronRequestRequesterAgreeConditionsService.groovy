package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestLoanCondition;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * This action performs the agreeing of conditions by the requester
 * @author Chas
 *
 */
public class ActionPatronRequestRequesterAgreeConditionsService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // If we are not the requester, flag it as an error
        if (request.isRequester) {
            String responseKey = '#ReShareLoanConditionAgreeResponse#';

            if (parameters.isNull('note')) {
                parameters.note = responseKey;
            } else {
                parameters.note = "${responseKey} ${parameters.note}";
            }

            // Inform the responder
            reshareActionService.sendRequestingAgencyMessage(request, 'Notification', parameters);

            PatronRequestLoanCondition[] conditions = PatronRequestLoanCondition.findAllByPatronRequestAndRelevantSupplier(request, request.resolvedSupplier);
            conditions.each { condition ->
                condition.accepted = true;
                condition.save(flush: true, failOnError: true);
            }

            actionResultDetails.auditMessage = 'Agreed to loan conditions';
            actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY);
        } else {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            actionResultDetails.auditMessage = 'Only the responder can accept the conditions';
        }

        return(actionResultDetails);
    }
}
