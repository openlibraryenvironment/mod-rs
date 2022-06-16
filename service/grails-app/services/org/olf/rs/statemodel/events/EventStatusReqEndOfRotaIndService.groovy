package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * Event service that is triggered when the end of rota is reached
 * @author Chas
 *
 */
public class EventStatusReqEndOfRotaIndService extends EventTriggerNoticesService {

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_END_OF_ROTA_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // all we need to do is call triggerNotice
        triggerNotice(request, 'End of rota');

        return(eventResultDetails);
    }
}
