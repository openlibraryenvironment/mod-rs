package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * This action is performed when the requester initiates print pull slip
 *
 */
public class ActionSLNPResponderSlnpAbortSupplyService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_REQUESTER_PRINT_PULL_SLIP);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // TODO: Implement this action

        return(actionResultDetails);
    }
}
