package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Action to approve the creation of a duplicate request
 */
public class ActionPatronRequestRequesterBypassValidationService extends AbstractAction {
    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_BYPASS_VALIDATION);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just set the status
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
