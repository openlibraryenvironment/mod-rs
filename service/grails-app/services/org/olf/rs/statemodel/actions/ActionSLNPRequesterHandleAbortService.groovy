package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * This action is performed when the requester aborts the request
 * @author Chas
 *
 */
public class ActionSLNPRequesterHandleAbortService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // TODO: Implement this action

        return(actionResultDetails);
    }
}
