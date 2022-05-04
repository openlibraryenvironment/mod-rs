package org.olf.rs.statemodel.actions;

import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.Status;

/**
 * Action that performs the returned item action for the requester
 * @author Ethan Freestone
 *
 */
public class ActionPatronRequestPatronReturnedItemAndShippedReturnService extends AbstractAction {
    ActionPatronRequestPatronReturnedItemService actionPatronRequestPatronReturnedItemService;
    ActionPatronRequestShippedReturnService actionPatronRequestShippedReturnService

    private static final String[] TO_STATES = [
        Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER
    ];

    HostLMSService hostLMSService;

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED);
    }

    @Override
    String[] toStates() {
        return(TO_STATES);
    }

    @Override
    Boolean canLeadToSameState() {
        // We do not return the same state, so we need to override and return false
        return(false);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
      // Create ourselves an ActionResultDetails that we will pass to each of the actions we want to call
      ActionResultDetails resultDetails = new ActionResultDetails();

      // Default the result as being a success
      resultDetails.result = ActionResult.SUCCESS;

      // mark returned by patron
      if (actionPatronRequestPatronReturnedItemService.performAction(request, parameters, resultDetails).result == ActionResult.SUCCESS) {
        // Copy in the new status in case the second action fails
        actionResultDetails.newStatus = resultDetails.newStatus;

        // Now we can mark it as being return shipped
        if (actionPatronRequestShippedReturnService.performAction(request, parameters, resultDetails).result == ActionResult.SUCCESS) {
          // Its a success, so copy in the new status and response result
          actionResultDetails.newStatus = resultDetails.newStatus;
          actionResultDetails.responseResult = resultDetails.responseResult;
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
