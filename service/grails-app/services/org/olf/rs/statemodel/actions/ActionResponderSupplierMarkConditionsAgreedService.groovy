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

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED);
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
