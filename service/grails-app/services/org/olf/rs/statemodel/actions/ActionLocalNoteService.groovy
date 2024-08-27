package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResult
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

public class ActionLocalNoteService extends AbstractAction {
    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        def newNote = parameters?.localNote;
        if (newNote == request?.localNote) {
            log.debug("Local note for request ${request.id} unchanged");
            actionResultDetails.auditMessage = "Local note unchanged";
            actionResultDetails.result = ActionResult.SUCCESS;
        } else if (!newNote?.trim()) {
            log.debug("No note provided or empty, removing note");
            request.localNote = null;
            actionResultDetails.auditMessage = "Removed local note";
            actionResultDetails.result = ActionResult.SUCCESS;
        } else {
            log.debug("Updating request ${request.id} with local note");
            request.localNote = newNote;
            actionResultDetails.auditMessage = "Local note updated";
            actionResultDetails.result = ActionResult.SUCCESS;
        }
        return(actionResultDetails);
    }

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_LOCAL_NOTE);
    }
}
