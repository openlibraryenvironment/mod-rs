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

    /**
     * Can this event lead to the state it came from ? We default to True, so override to return False if the action does not
     * @return True if it can lead to the same state it came from, otherwise False
     */
    Boolean canLeadToSameState() {
        return(true);
    }

    /**
     * The states this event could lead to that are different from the state that it came from
     * @param model The model you want the states for
     * @return The states it could lead to
     */
    abstract String[] toStates(String model);

    /**
     * All the possible states this event could lead to
     *
     * @param model The model you want the states for
     * @return the possible states
     */
    String[] possibleToStates(String model) {
        String[] states = canLeadToSameState() ? fromStates(model) : [];
        return(states + toStates(model));
    }

    /**
     * The possible states that could lead to this event, these are not currently in the database, so they need to be specified on each Event
     * @param model The model you want the states for
     * @return The possible states that could lead to this event
     */
    abstract String[] fromStates(String model);

    /**
     * Does this Event support the supplied model
     * @param model The model that they want to know whether we support or not
     * @return true if we support the model, false if we do not
     */
    abstract boolean supportsModel(String model);
}
