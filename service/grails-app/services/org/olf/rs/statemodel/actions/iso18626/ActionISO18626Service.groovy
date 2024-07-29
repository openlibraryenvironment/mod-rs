package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.ProtocolMessageBuildingService;
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.AbstractSlnpNonReturnableAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that has the general methods used by both the requester and responder
 * @author Chas
 *
 */
public abstract class ActionISO18626Service extends AbstractSlnpNonReturnableAction {

    ProtocolMessageBuildingService protocolMessageBuildingService;

    /**
     * This methods checks for our hack to see if the caller wants to verify that they received our last message
     * @param request The request that we are acting upon
     * @param note The note we need to extract the last sequence from
     * @param actionResultDetails The result details that need updating if it is our hack
     * @return true if it is our hack for checking if the last message was received, otherwise false if it is a normal message
     */
    public boolean checkForLastSequence(PatronRequest request, String note, ActionResultDetails actionResultDetails) {
        boolean result = false;
        Map noteParts = protocolMessageBuildingService.extractLastSequenceFromNote(note);

        // Are they wanting us to check as to whether we received their last message or not
        if (noteParts.sequence != null) {
            // This is our hack to see if their last message was received, everything else in the message is ignored
            result = true;

            // So we need to see if the last sequence we received is the same one specified in the note
            if (request.lastSequenceReceived == null) {
                // We havn't previously received a sequence
                actionResultDetails.result = ActionResult.ERROR;
                actionResultDetails.responseResult.errorType = 'NoPreviousMessage';
            } else if (request.lastSequenceReceived.equals(noteParts.sequence)) {
                // The last one they sent, was the last one we received
                actionResultDetails.result = ActionResult.SUCCESS;

                // We also set the error value to be the last received sequence, so that we know that the response is from our hack
                actionResultDetails.responseResult.errorType = EventISO18626IncomingAbstractService.ERROR_TYPE_NO_ERROR;
                actionResultDetails.responseResult.errorValue = request.lastSequenceReceived.toString();
            } else {
                // We are out of sequence with the Responder, so return an error
                actionResultDetails.result = ActionResult.ERROR;
                actionResultDetails.responseResult.errorType = 'SequenceDifferent';
                actionResultDetails.responseResult.errorValue = request.lastSequenceReceived.toString();
            }
        }

        // Let the caller know if it was our hack
        return(result);
    }
}
