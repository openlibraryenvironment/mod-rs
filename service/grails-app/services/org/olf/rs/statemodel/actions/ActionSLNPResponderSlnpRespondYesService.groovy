package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.ActionResult
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

/**
 * Performs an answer will supply action for the responder
 *
 */
public class ActionSLNPResponderSlnpRespondYesService extends ActionResponderService {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_RESPOND_YES);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Check the pickup location and route
        if (validatePickupLocationAndRoute(request, parameters, actionResultDetails).result == ActionResult.SUCCESS) {
            actionResultDetails.auditMessage = 'Will Supply';
        }

        return(actionResultDetails);
    }
}
