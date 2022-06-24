package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * This action is performed when the requester rejects the conditions
 * @author Chas
 *
 */
public class ActionPatronRequestRequesterRejectConditionsService extends ActionPatronRequestCancelService {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        sendCancel(request, Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS, parameters, actionResultDetails);
        actionResultDetails.auditMessage = 'Rejected loan conditions';
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING);

        return(actionResultDetails);
    }
}
