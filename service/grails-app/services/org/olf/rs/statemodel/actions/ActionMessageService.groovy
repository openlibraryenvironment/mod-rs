package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Abstract action that sends a message to the other side of the transaction
 * @author Chas
 *
 */
public abstract class ActionMessageService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_MESSAGE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // We must have a note
        if (parameters.note == null) {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            actionResultDetails.auditMessage = 'No note supplied to send';
        } else {
            // Send the message
            actionResultDetails.responseResult.status = reshareActionService.sendMessage(request, parameters);
            actionResultDetails.auditMessage = 'Message sent: ' + parameters.note;
        }
        return(actionResultDetails);
    }
}
