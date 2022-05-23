package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Requester has agreed to the conditions, which is being manually marked by the responder
 * @author Chas
 *
 */
public class ActionResponderSupplierMarkConditionsAgreedService extends ActionResponderService {

    private static final String[] TO_STATES = [
        Status.RESPONDER_AWAIT_PICKING,
        Status.RESPONDER_AWAIT_SHIP,
        Status.RESPONDER_CHECKED_IN_TO_RESHARE,
        Status.RESPONDER_IDLE,
        Status.RESPONDER_NEW_AWAIT_PULL_SLIP
    ];

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED);
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
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Mark all conditions as accepted
        reshareApplicationEventHandlerService.markAllLoanConditionsAccepted(request)

        actionResultDetails.auditMessage = 'Conditions manually marked as agreed';
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, request.previousStates[Status.RESPONDER_PENDING_CONDITIONAL_ANSWER]);
        return(actionResultDetails);
    }
}
