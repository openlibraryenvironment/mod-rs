package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * Contains the methods and definitions required to process an incoming ISO18626 message
 * @author Chas
 *
 */
public class EventISO18626IncomingRequesterService extends EventISO18626IncomingAbstractService {

    @Override
    public String name() {
        return(Events.EVENT_SUPPLYING_AGENCY_MESSAGE_INDICATION);
    }

    @Override
    public String getRequestId(Map eventData) {
        return(eventData.header?.requestingAgencyRequestId);
    }

    @Override
    public String getPeerId(Map eventData) {
        return(null);
    }

    @Override
    public boolean isRequester() {
        return(true);
    }

    @Override
    public String getActionToPerform(Map eventData) {
        return(eventData.messageInfo?.reasonForMessage);
    }

    @Override
    public Map createResponseData(Map eventData, boolean success, String errorType, Object errorValue) {
        Map data = responseData(eventData, 'REQUESTING_AGENCY_MESSAGE', success, errorType, errorValue);
        data.reasonForMessage = getActionToPerform(eventData);
        return(data);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        return(processRequest(eventData, eventResultDetails));
    }
}
