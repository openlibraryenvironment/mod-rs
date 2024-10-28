package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestNotification;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Abstract action that marks a message as seen
 * @author Chas
 *
 */
public class ActionMessageSeenService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_MESSAGE_SEEN);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        log.debug("Called messageSeen service");
        // We must have an id
        if (parameters.id == null) {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            actionResultDetails.auditMessage = 'No message id supplied to mark as seen';
        } else if (parameters.seenStatus == null) {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            actionResultDetails.auditMessage = 'No seenStatus supplied to mark as seen';
        } else {
            PatronRequestNotification message = PatronRequestNotification.findById(parameters.id)
            if (message == null) {
                actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
                actionResultDetails.auditMessage = 'Message with id: ' + parameters.id + ' does not exist';
            } else {
                log.debug("Message id ${parameters.id} updated");
                message.seen = parameters.seenStatus;
                message.save(flush:true, failOnError:true);
            }
        }

        actionResultDetails.responseResult.status = (actionResultDetails.result == ActionResult.SUCCESS);
        return(actionResultDetails);
    }
}
