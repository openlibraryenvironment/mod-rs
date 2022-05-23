package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * This action checks whether the borrower is valid or not
 * @author Chas
 *
 */
public class ActionPatronRequestBorrowerCheckService extends AbstractAction {

    private static final String[] TO_STATES = [
        Status.PATRON_REQUEST_INVALID_PATRON,
        Status.PATRON_REQUEST_VALIDATED
    ];

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_BORROWER_CHECK);
    }

    @Override
    String[] toStates() {
        return(TO_STATES);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Performs a lookup against the patron
        Map borrowerCheck = reshareActionService.lookupPatron(request, parameters);
        actionResultDetails.responseResult.status = borrowerCheck?.callSuccess && borrowerCheck?.patronValid
        if (actionResultDetails.responseResult.status) {
            actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED);
        } else if (!borrowerCheck?.callSuccess) {
            // The Host LMS check call has failed, stay in current state
            request.needsAttention = true;
            actionResultDetails.auditMessage = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. ' + borrowerCheck?.problems;
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_HOST_LMS_CALL_FAILED;
        } else {
            // The call succeeded but patron is invalid
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_INVALID_PATRON;
            actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON);
            String errors = (borrowerCheck?.problems == null) ? '' : (' (Errors: ' + borrowerCheck.problems + ')');
            String status = borrowerCheck?.status == null ? '' : (' (Patron state = ' + borrowerCheck.status + ')');
            actionResultDetails.auditMessage = "Failed to validate patron with id: \"${request.patronIdentifier}\".${status}${errors}";
            request.needsAttention = true;
        }

        return(actionResultDetails);
    }
}
