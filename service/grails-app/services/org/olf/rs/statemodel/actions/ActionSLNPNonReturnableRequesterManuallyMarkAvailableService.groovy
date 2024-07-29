package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractSlnpNonReturnableAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

/**
 * This actions performs manual marking of item being available.
 *
 */
public class ActionSLNPNonReturnableRequesterManuallyMarkAvailableService extends AbstractSlnpNonReturnableAction {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE)
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        performCommonAction(request, parameters, actionResultDetails, "Request is manually marked as available.")

        return(actionResultDetails)
    }
}