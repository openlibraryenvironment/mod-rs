package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

public class EventNonreturnableResponderNewPatronRequestIndService extends EventRespNewPatronRequestIndService {
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        log.debug("Calling processEvent from EventRespNewPatronRequestIndService");
        return super.processEvent(request, eventData, eventResultDetails);
    }

    @Override
    String name() {
        return Events.EVENT_NONRETURNABLE_RESPONDER_NEW_PATRON_REQUEST_INDICATION;
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return EventFetchRequestMethod.PAYLOAD_ID;
    }
}
