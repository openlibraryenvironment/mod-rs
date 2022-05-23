package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Performs a check against the host LMS to validate the patron, overriding to be a valid patron ...
 * @author Chas
 *
 */
public class ActionPatronRequestBorrowerCheckOverrideService extends AbstractAction {

    private static final String[] TO_STATES = [
        Status.PATRON_REQUEST_VALIDATED
    ];

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE);
    }

    @Override
    String[] toStates() {
        return(TO_STATES);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        Map localParameters = parameters;
        localParameters.override = true;

        // Perform the borrower check
        Map borrowerCheck = reshareActionService.lookupPatron(request, localParameters);

        // borrowerCheck.patronValid should ALWAYS be true in this action
        actionResultDetails.responseResult.status = borrowerCheck?.callSuccess
        if (actionResultDetails.responseResult.status) {
            actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED);
        } else {
            // The Host LMS check call has failed, stay in current state
            request.needsAttention = true;
            actionResultDetails.auditMessage = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. ' + borrowerCheck?.problems;
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_HOST_LMS_CALL_FAILED;
        }

        return(actionResultDetails);
    }
}
