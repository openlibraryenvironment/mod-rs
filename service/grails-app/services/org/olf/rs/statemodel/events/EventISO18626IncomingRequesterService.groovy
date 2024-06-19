package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.ProtocolMessageService;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * Contains the methods and definitions required to process an incoming ISO18626 message
 * @author Chas
 *
 */
public class EventISO18626IncomingRequesterService extends EventISO18626IncomingAbstractService {

    ProtocolMessageService protocolMessageService;
    ReshareActionService reshareActionService;

    @Override
    public String name() {
        return(Events.EVENT_SUPPLYING_AGENCY_MESSAGE_INDICATION);
    }

    @Override
    public String getRequestId(Map eventData) {
        // We need to remove the rota position from it
        return(protocolMessageService.extractIdFromProtocolId(eventData.header?.requestingAgencyRequestId));
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
        Map data = responseData(eventData, 'SUPPLYING_AGENCY_MESSAGE', success, errorType, errorValue);
        data.reasonForMessage = getActionToPerform(eventData);
        return(data);
    }

    @Override
    public boolean isForCurrentRotaLocation(Map eventData, PatronRequest request) {
        // By default we assume it is not
        boolean isCorrectRotaLocation = false;

        // First of all we will see if the rota position is in the id field
        long idRotaPosition = protocolMessageService.extractRotaPositionFromProtocolId(eventData.header?.requestingAgencyRequestId);
        if (idRotaPosition < 0) {
            // We failed to find the rota position, so we need to fallback on checking symbols, this can still fail as the same location can be in the rota multiple times
            Map symbols = reshareActionService.requestingAgencyMessageSymbol(request);
            if (symbols.receivingSymbol != null) {
                // Just compare the symbols
                Map agencyId = eventData.header?.supplyingAgencyId;
                isCorrectRotaLocation = symbols.receivingSymbol.equals(agencyId.agencyIdType  + ':' + agencyId.agencyIdValue);
            }
        } else {
            // That makes life nice and easy, just need to compare it with the rotaPosition
            isCorrectRotaLocation = request.rotaPosition.equals(idRotaPosition);
        }

        return(isCorrectRotaLocation);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        return(processRequest(eventData, eventResultDetails));
    }
}
