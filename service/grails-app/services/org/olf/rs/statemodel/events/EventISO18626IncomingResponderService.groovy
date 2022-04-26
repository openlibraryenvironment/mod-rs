package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * Contains the methods and definitions required to process an incoming ISO18626 message
 * @author Chas
 *
 */
public class EventISO18626IncomingResponderService extends EventISO18626IncomingAbstractService {

    @Override
    public String name() {
        return(Events.EVENT_REQUESTING_AGENCY_MESSAGE_INDICATION);
    }

    @Override
    public String getRequestId(Map eventData) {
        return(eventData.header?.supplyingAgencyRequestId);
    }

    @Override
    public String getPeerId(Map eventData) {
        return(eventData.header.requestingAgencyRequestId);
    }

    @Override
    public boolean isRequester() {
        return(false);
    }

    @Override
    public String getActionToPerform(Map eventData) {
        return(eventData.activeSection?.action);
    }

    @Override
    public Map createResponseData(Map eventData, boolean success, String errorType, Object errorValue) {
        Map data = responseData(eventData, 'REQUESTING_AGENCY_MESSAGE', success, errorType, errorValue);
        data.action = getActionToPerform(eventData);
        return(data);
    }

    @Override
    public boolean isForCurrentRotaLocation(Map eventData, PatronRequest request) {
        // As a responder we only have 1 rota location so it cannot be for any other
        return(true);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        return(processRequest(eventData, eventResultDetails));
    }
}
