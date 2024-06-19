package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * This action is performed when the requester marks item lost
 *
 */
public class ActionSLNPRequesterSlnpMarkItemLostService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_REQUESTER_MARK_ITEM_LOST);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        actionResultDetails.auditMessage = 'Mark item lost';
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
