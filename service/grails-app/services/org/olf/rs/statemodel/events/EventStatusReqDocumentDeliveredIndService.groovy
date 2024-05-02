package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events;

/**
 * Event triggered when the requester status becomes "document delivered"
 */
public class EventStatusReqDocumentDeliveredIndService extends AbstractEvent {

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        log.debug("Request status is 'Document Delivered'");
        return eventResultDetails;
    }

    @Override
    String name() {
        return Events.EVENT_STATUS_REQ_DOCUMENT_DELIVERED_INDICATION;
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return EventFetchRequestMethod.PAYLOAD_ID;
    }
}
