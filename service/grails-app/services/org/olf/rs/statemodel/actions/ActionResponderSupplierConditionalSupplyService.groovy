package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * This action is when the responder is applying conditions before they will supply
 * @author Chas
 *
 */
public class ActionResponderSupplierConditionalSupplyService extends ActionResponderConditionService {

    private static final String[] TO_STATES = [
        Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        Status.RESPONDER_PENDING_CONDITIONAL_ANSWER
    ];

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY);
    }

    @Override
    String[] toStates() {
        return(TO_STATES);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Check the pickup location and route
        if (validatePickupLocationAndRoute(request, parameters, actionResultDetails).result == ActionResult.SUCCESS) {
            reshareActionService.sendResponse(request, 'ExpectToSupply', parameters);
            sendSupplierConditionalWarning(request, parameters);

            if (parameters.isNull('holdingState') || parameters.holdingState == 'no') {
                // The supplying agency wants to continue with the request
                actionResultDetails.auditMessage = 'Request responded to conditionally, request continuing';
            // Status is set to Status.RESPONDER_NEW_AWAIT_PULL_SLIP in validatePickupLocationAndRoute
            } else {
                // The supplying agency wants to go into a holding state
                // In this case we want to "pretend" the previous state was actually the next one, for later when it looks up the previous state
                request.previousStates.put(Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP);
                actionResultDetails.auditMessage = 'Request responded to conditionally, placed in hold state';
                actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER);
                actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_HOLDING;
            }
        }

        return(actionResultDetails);
    }
}
