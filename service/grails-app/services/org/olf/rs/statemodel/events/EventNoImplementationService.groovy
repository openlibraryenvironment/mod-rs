package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * This event service id for those events that have not been implemented
 * @author Chas
 *
 */
public class EventNoImplementationService extends AbstractEvent {

    private static final String[] FROM_STATES = [
    ];

    private static final String[] TO_STATES = [
    ];

    @Override
    String name() {
        return(Events.EVENT_NO_IMPLEMENTATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        // We assume anything that is not implemented will have the request id in the payload
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
        // As this is the fallback for all those not implemented, we support all models
        return(true);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // There is nothing to do
        //log.error('Event ' + eventData.event + ' has not been implemented');

        // No need to save the request or add an audit entry
        eventResultDetails.saveData = false;
        return(eventResultDetails);
    }
}
