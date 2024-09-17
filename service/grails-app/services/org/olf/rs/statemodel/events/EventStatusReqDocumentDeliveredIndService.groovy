package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events;

/**
 * Event triggered when the requester status becomes "document delivered"
 */
public class EventStatusReqDocumentDeliveredIndService extends EventTriggerNoticesService {

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        log.debug("Request status is 'Document Delivered'");
        //trigger the notice
        triggerNotice(request, RefdataValueData.NOTICE_TRIGGER_DOCUMENT_DELIVERED);
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
