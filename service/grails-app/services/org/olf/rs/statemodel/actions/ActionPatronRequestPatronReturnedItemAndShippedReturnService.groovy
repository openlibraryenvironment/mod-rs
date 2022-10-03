package org.olf.rs.statemodel.actions;

import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Action that performs the returned item action for the requester
 * @author Ethan Freestone
 *
 */
public class ActionPatronRequestPatronReturnedItemAndShippedReturnService extends AbstractAction {
    ActionPatronRequestPatronReturnedItemService actionPatronRequestPatronReturnedItemService;
    ActionPatronRequestShippedReturnService actionPatronRequestShippedReturnService
    HostLMSService hostLMSService;

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Create ourselves an ActionResultDetails that we will pass to each of the actions we want to call
        ActionResultDetails resultDetails = new ActionResultDetails();

        // Default the result as being a success
        resultDetails.result = ActionResult.SUCCESS;

        // mark returned by patron
        if (actionPatronRequestPatronReturnedItemService.performAction(request, parameters, resultDetails).result == ActionResult.SUCCESS) {
            // Now we can mark it as being return shipped
            if (actionPatronRequestShippedReturnService.performAction(request, parameters, resultDetails).result == ActionResult.SUCCESS) {
                // Its a success, so set the response result
                actionResultDetails.responseResult = resultDetails.responseResult;
            } else {
                // Set the qualifier as the item has been returned by the patron
                actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_SHIP_ITEM;
            }
        }

        // At least one of our two calls failed
        if (resultDetails.result != ActionResult.SUCCESS) {
            // Failed so copy back the appropriate details so it can be diagnosed
            actionResultDetails.responseResult = resultDetails.responseResult;
            actionResultDetails.result = resultDetails.result;
            actionResultDetails.auditMessage = resultDetails.auditMessage
        }

        return(actionResultDetails);
    }
}
