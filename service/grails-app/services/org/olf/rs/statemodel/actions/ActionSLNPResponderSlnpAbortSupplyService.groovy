package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * This action is performed when the responder aborts the supply
 *
 */
public class ActionSLNPResponderSlnpAbortSupplyService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        reshareActionService.sendResponse(request, ActionEventResultQualifier.QUALIFIER_ABORTED, parameters, actionResultDetails);
        actionResultDetails.auditMessage = 'Aborted from supplier';

        return(actionResultDetails);
    }
}
