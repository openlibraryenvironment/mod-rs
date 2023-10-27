package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Action to send a request back to the "new request" state to be re-tried
 */

public class ActionPatronRequestRequesterRetryRequestService extends AbstractAction {
    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_RETRY_REQUEST);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
