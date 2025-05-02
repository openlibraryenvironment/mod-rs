package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest
import org.olf.rs.iso18626.TypeStatus;
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
        boolean validPickupLocationAndRoute;
        //If this is a copy service type, we don't need to worry about valid route/pickup location
        if (request.serviceType?.value == 'copy') {
            validPickupLocationAndRoute = true;
        } else {
            validPickupLocationAndRoute
                    = (validatePickupLocationAndRoute(request, parameters, actionResultDetails).result == ActionResult.SUCCESS)
        }
        // Check the pickup location and route
        if (validPickupLocationAndRoute) {
            // Status is set to Status.RESPONDER_NEW_AWAIT_PULL_SLIP in validatePickupLocationAndRoute
            reshareActionService.sendResponse(request, TypeStatus.WILL_SUPPLY.value(), parameters, actionResultDetails);
        }

        return(actionResultDetails);
    }
}
