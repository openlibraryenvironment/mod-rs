package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

/**
 * The requester has marked the request as complete
 */
class ActionPatronRequestRequesterCompleteRequestService extends AbstractAction {
    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }

    @Override
    String name() {
        return Actions.ACTION_REQUESTER_COMPLETE_REQUEST;
    }
}
