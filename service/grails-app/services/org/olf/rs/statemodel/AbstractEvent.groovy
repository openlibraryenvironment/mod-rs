package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareApplicationEventHandlerService;

/**
 * This is the base class for the event handlers
 * @author Chas
 *
 */
public abstract class AbstractEvent {

    // We automatically inject this service as some if not all events use it
    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;

    /**
     * Method that all classes derive from this one that actually performs the event
     * @param request The request the event is being performed against
     * @param eventData The data that was supplied with the event
     * @param eventResultDetails The result of performing the event
     * @return The eventResultDetails
     */
    abstract EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails);

    /**
     * The name of the event
     * @return the event name
     */
    abstract String name();

    /**
     * The method to use to fetch the request data from the event data
     * @return How to fetch the request data
     */
    abstract EventFetchRequestMethod fetchRequestMethod();
}
