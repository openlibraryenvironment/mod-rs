package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StatusService;

/**
 * This action is performed when the user actions the request with Manual Close
 * @author Chas
 *
 */
public class ActionManualCloseService extends AbstractAction {

    StatusService statusService;

    @Override
    String name() {
        return(Actions.ACTION_MANUAL_CLOSE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        if ((parameters?.terminalState != null) && (parameters.terminalState ==~ /[A-Z_]+/)) {

            // Need to validate the terminal state is legitimate, this way is no longer valid
            Status closeStatus = Status.lookup(parameters.terminalState);

            // Have we been supplied a valid close status
            if (closeStatus && closeStatus.terminal) {
                reshareActionService.sendMessage(request, [note: "The ${request.isRequester ? 'requester' : 'responder'} has manually closed this request."], actionResultDetails);
                actionResultDetails.auditMessage = 'Manually closed' ;
                actionResultDetails.qualifier = parameters.terminalState ;
            } else {
                actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
                actionResultDetails.auditMessage = "Attemped manualClose action with non-terminal state: ${s} ${parameters?.terminalState}";
            }
        } else {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            actionResultDetails.auditMessage = "Attemped manualClose action with state containing invalid character: ${parameters?.terminalState}";
        }

        // Set the response status
        actionResultDetails.responseResult.status = (actionResultDetails.result == ActionResult.SUCCESS);

        return(actionResultDetails);
    }
}
