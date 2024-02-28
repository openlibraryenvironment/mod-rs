package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * This action is performed when the responder responds "Abort Supply"
 *
 */
public class ActionSLNPRequesterSlnpPrintPullSlipService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // TODO: Implement this action
        // Send ISO18626 message with notification type note = "Abort"

        return(actionResultDetails);
    }
}
