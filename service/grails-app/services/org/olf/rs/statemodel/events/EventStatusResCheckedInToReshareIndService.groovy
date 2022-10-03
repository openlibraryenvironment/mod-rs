package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * This event is triggered when the item is checked in to reshare
 * @author Chas
 *
 */
public class EventStatusResCheckedInToReshareIndService extends AbstractEvent {

    ReshareActionService reshareActionService;

    @Override
    String name() {
        return(Events.EVENT_STATUS_RES_CHECKED_IN_TO_RESHARE_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        /**
         * It's not clear if the system will ever need to differentiate between the status of checked in and
         * await shipping, so for now we leave the 2 states in place and just automatically transition  between them
         * this method exists largely as a place to put functions and workflows that diverge from that model
         */
        eventResultDetails.auditMessage = 'Request awaits shipping';

        return(eventResultDetails);
    }
}
