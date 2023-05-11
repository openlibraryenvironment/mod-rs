package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.ReshareActionService
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

public class EventStatusResAwaitDesequestrationIndService extends AbstractEvent {

    @Override
    String name() {
        return(Events.EVENT_STATUS_RES_AWAIT_DESEQUESTRATION_INDICATION);
    }

    ReshareActionService reshareActionService;

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // We only deal with responder
        if (request.isRequester) {
            log.debug("pr ${request.id} is requester, not sending protocol message");
        } else {
            log.debug("Sending protocol message with overdue status change from PatronRequest ${request.id}");
            reshareActionService.sendStatusChange(request, 'Overdue', eventResultDetails, 'Request has reached the due date and loan has expired')
        }

        return(eventResultDetails);
    }
}
