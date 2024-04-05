package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

public class EventNonreturnableRequesterNewPatronRequestIndService extends AbstractEvent {
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        return eventResultDetails;
    }

    @Override
    String name() {
        return Events.EVENT_NONRETURNABLE_REQUESTER_NEW_PATRON_REQUEST_INDICATION;
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return EventFetchRequestMethod.PAYLOAD_ID;
    }
}
