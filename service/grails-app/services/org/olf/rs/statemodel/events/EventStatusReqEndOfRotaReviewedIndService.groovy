package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails

class EventStatusReqEndOfRotaReviewedIndService extends EventTriggerNoticesService {

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_END_OF_ROTA_REVIEWED_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        triggerNotice(request, RefdataValueData.NOTICE_TRIGGER_END_OF_ROTA_REVIEWED);

        return eventResultDetails;
    }
}
