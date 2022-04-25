package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;

/**
 * Action that deals with interpreting ISO18626 on the responder side
 * @author Chas
 *
 */
public abstract class ActionISO18626ResponderService extends ActionISO18626Service {

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Extract the sequence from the note
        Map sequenceResult = protocolMessageBuildingService.extractSequenceFromNote(parameters?.activeSection?.note);
        String note = sequenceResult.note;
        request.lastSequenceReceived = sequenceResult.sequence;

        // If there is a note, create notification entry
        if (note) {
            reshareApplicationEventHandlerService.incomingNotificationEntry(request, parameters, false, note);
        }

        return(actionResultDetails);
    }
}
