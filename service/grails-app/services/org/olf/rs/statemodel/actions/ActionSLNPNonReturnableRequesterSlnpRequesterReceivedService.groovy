package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * Document has been successfully supplied
 *
 */
public class ActionSLNPNonReturnableRequesterSlnpRequesterReceivedService extends AbstractAction {
    @Override
    String name() {
        return(Actions.ACTION_SLNP_REQUESTER_REQUESTER_RECEIVED)
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        actionResultDetails.auditMessage = 'Document has been successfully supplied'

        return (actionResultDetails)
    }
}
