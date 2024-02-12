package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

public class EventSLNPRequesterISO18626AbortedIndService extends AbstractEvent {

    @Override
    String name() {
        return(Events.EVENT_SLNP_REQUESTER_ISO_18626_ABORTED_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // TODO: Implement this event

        return(eventResultDetails);
    }
}
