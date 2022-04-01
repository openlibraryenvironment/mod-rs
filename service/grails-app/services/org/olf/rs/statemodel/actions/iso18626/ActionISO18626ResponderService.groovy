package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;

/**
 * Action that deals with interpreting ISO18626 on the responder side
 * @author Chas
 *
 */
public abstract class ActionISO18626ResponderService extends AbstractAction {

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // If there is a note, create notification entry
        if (parameters?.activeSection?.note) {
            reshareApplicationEventHandlerService.incomingNotificationEntry(request, parameters, false);
        }

        return(actionResultDetails);
    }
}
