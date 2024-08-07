package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractSlnpNonReturnableAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

/**
 * This actions performs manual marking of item being supplied.
 *
 */
public class ActionSLNPNonReturnableRequesterManuallyMarkSuppliedService extends AbstractSlnpNonReturnableAction {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED)
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        performCommonAction(request, parameters, actionResultDetails, "Request is manually marked as supplied.")
        
        return(actionResultDetails)
    }
}