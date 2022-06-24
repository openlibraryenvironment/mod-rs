package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Performs an answer will supply action for the responder
 * @author Chas
 *
 */
public class ActionResponderRespondYesService extends ActionResponderService {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_RESPOND_YES);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Check the pickup location and route
        if (validatePickupLocationAndRoute(request, parameters, actionResultDetails).result == ActionResult.SUCCESS) {
            // Status is set to Status.RESPONDER_NEW_AWAIT_PULL_SLIP in validatePickupLocationAndRoute
            reshareActionService.sendResponse(request, 'ExpectToSupply', parameters, actionResultDetails);
        }

        return(actionResultDetails);
    }
}
