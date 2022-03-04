package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;

/**
 * Event that is triggered by an overduw
 * @author Chas
 *
 */
public class EventStatusResOverdueIndService extends AbstractEvent {

    private static final String[] FROM_STATES = [
    ];

    private static final String[] TO_STATES = [
    ];

    @Override
    String name() {
        return(Events.EVENT_STATUS_RES_OVERDUE_INDICATION);
    }

    ReshareActionService reshareActionService;

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    String[] toStates(String model) {
        return(TO_STATES);
    }

    @Override
    String[] fromStates(String model) {
        return(FROM_STATES);
    }

    @Override
    boolean supportsModel(String model) {
        // This event
        return(model == StateModel.MODEL_RESPONDER);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // We only deal with responder
        if (request.isRequester) {
            log.debug("pr ${request.id} is requester, not sending protocol message");
        } else {
            log.debug("Sending protocol message with overdue status change from PatronRequest ${request.id}");
            reshareActionService.sendStatusChange(request, 'Overdue', 'Request is Overdue');
        }

        return(eventResultDetails);
    }
}
