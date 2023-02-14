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
        // All we do is deal with the note
        processNote(request, parameters, parameters?.activeSection?.note, actionResultDetails);

        // return the action result details to the caller
        return(actionResultDetails);
    }

    /**
     * This method allows a caller to preprocess the note as we effectively use it for extensions at the moment
     * @param request The request that needs to be manipulated
     * @param parameters The parameters we use to manipulate the request
     * @param note The note from the parameters that may have been manipulated
     * @param actionResultDetails Any details that influence perdorming this action
     * @return The action result details
     */
    protected ActionResultDetails processNote(PatronRequest request, Object parameters, String note, ActionResultDetails actionResultDetails) {
        // Extract the sequence from the note
        Map sequenceResult = protocolMessageBuildingService.extractSequenceFromNote(note);

        // Now we deal with the note without the sequence in it
        note = sequenceResult.note;
        request.lastSequenceReceived = sequenceResult.sequence;

        // If there is a note, create notification entry
        if (note) {
            reshareApplicationEventHandlerService.incomingNotificationEntry(request, parameters, false, note);
        }

        return(actionResultDetails);
    }
}
