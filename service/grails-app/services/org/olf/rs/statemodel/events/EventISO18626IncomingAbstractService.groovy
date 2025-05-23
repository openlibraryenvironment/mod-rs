package org.olf.rs.statemodel.events

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.DirectoryEntryService;
import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.ActionService
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.StateModel
import org.springframework.security.core.parameters.P;

/**
 * Contains the base methods and definitions required to interpret the 18626 protocol
 * @author Chas
 *
 */
public abstract class EventISO18626IncomingAbstractService extends AbstractEvent {

    public static final String STATUS_ERROR = 'ERROR';
    public static final String STATUS_OK    = 'OK';

    public static final String SERVICE_REQUEST_TYPE_RETRY = 'Retry'

    // A couple of additional status we use internally when we get an HTTP OK response
    public static final String STATUS_PROTOCOL_ERROR = 'PROTOCOL_ERROR';

    // ISO18626 Error codes
    public static final String ERROR_TYPE_BADLY_FORMED_MESSAGE                = 'BadlyFormedMessage';
    public static final String ERROR_TYPE_INVALID_CANCEL_VALUE                = 'InvalidCancelValue';
    public static final String ERROR_TYPE_NO_ACTION                           = 'ActionNotSupplied';
    public static final String ERROR_TYPE_NO_ACTIVE_REQUEST                   = 'NoActiveRequest';
    public static final String ERROR_TYPE_NO_CANCEL_VALUE                     = 'NoCancelValue';
    public static final String ERROR_TYPE_NO_CONFIRMATION_ELEMENT_IN_RESPONSE = 'NoConfirmationElementInResponse';
    public static final String ERROR_TYPE_NO_ERROR                            = 'NoError';
    public static final String ERROR_TYPE_NO_REASON_FOR_MESSAGE               = 'ReasonForMessageNotSupplied';
    public static final String ERROR_TYPE_NO_XML_SUPPLIED                     = 'NoXMLSupplied';
    public static final String ERROR_TYPE_UNABLE_TO_FIND_REQUEST              = 'UnableToFindRequest';
    public static final String ERROR_TYPE_UNABLE_TO_PROCESS                   = 'UnableToProcess';
    public static final String ERROR_TYPE_REQUEST_ID_ALREADY_EXISTS           = 'RequestIdAlreadyExists';
    public static final String ERROR_TYPE_INVALID_PATRON_REQUEST              = 'InvalidPatronRequest';

    // The actions, I assume these are only applicable for receiving by the responder
    public static final String ACTION_CANCEL          = 'Cancel';
    public static final String ACTION_NOTIFICATION    = 'Notification';
    public static final String ACTION_RECEIVED        = 'Received';
    public static final String ACTION_RENEW           = 'Renew';          // Not yet implemented
    public static final String ACTION_SHIPPED_FORWARD = 'ShippedForward'; // Not yet implemented
    public static final String ACTION_SHIPPED_RETURN  = 'ShippedReturn';
    public static final String ACTION_STATUS_REQUEST  = 'StatusRequest';

    // The service used to run the actions
    ActionService actionService;
    SettingsService settingsService;

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        // We are dealing with the transaction directly
        return(EventFetchRequestMethod.HANDLED_BY_EVENT_HANDLER);
    }

    /**
     * Retrieves the request id from the event data
     * @param eventData The event data that holds the incoming message
     * @return The request is if there was one
     */
    public abstract String getRequestId(Map eventData);

    /**
     * Retrieves the peer id if the request has one
     * @param eventData The event data to retrieve the id from
     * @return The peer id or null if there is not one
     */

    public abstract String getPeerId(Map eventData);

    /**
     * Are we interested in requester or responder requests
     * @return true if we are just interested in requester side requests otherwise false for responder side requests
     */
    public abstract boolean isRequester();

    /**
     * For some reason they have the action to perform in different places for requester and responder messages
     * @param eventData The event data to retrieve the action from
     * @return the action that is to be performed
     */
    public abstract String getActionToPerform(Map eventData);

    /**
     * Creates the response map required for generating a response to the message
     * @param eventData The event data to get the header information from
     * @param success Was this a success or not
     * @param errorType If it failed, this is the error it failed with
     * @param errorValue If applicable the value it failed to interpret
     * @return The map to be used to generate the response with
     */
    public abstract Map createResponseData(Map eventData, boolean success, String errorType, Object errorValue);

    /**
     * Checks to ensure the incoming message is for the current rota location
     * @param eventData The incoming message
     * @param request The request we have found
     * @return true if the message is for the current rota location
     */
    public abstract boolean isForCurrentRotaLocation(Map eventData, PatronRequest request);

    public String requestingSymbolFromEventData(Map eventData) {
        String agencyIdType = eventData?.header?.requestingAgencyId?.agencyIdType;
        String agencyIdValue = eventData?.header?.requestingAgencyId?.agencyIdValue;
        if (!agencyIdType || !agencyIdValue) {
            return null;
        }
        return "${agencyIdType}:${agencyIdValue}";
    }

    public String supplyingSymbolFromEventData(Map eventData) {
        String agencyIdType = eventData?.header?.supplyingAgencyId?.agencyIdType;
        String agencyIdValue = eventData?.header?.supplyingAgencyId?.agencyIdValue;
        if (!agencyIdType || !agencyIdValue) {
            return null;
        }
        return "${agencyIdType}:${agencyIdValue}";
    }

    /**
     * Processes the data received from an ISO18626 sender
     * @param eventData The message that was sent
     * @return map containing the details to be returned
     */
    public EventResultDetails processRequest(Map eventData, EventResultDetails eventResultDetails) {
        // The status and error variables
        boolean processedSuccessfully = true;
        String errorType = null;
        Object errorValue = null;
        String requestId = getRequestId(eventData);
        String requestUuid = null;
        settingsService = new SettingsService();
        String requestRouterSetting = settingsService.getSettingValue('routing_adapter');
        Boolean routingDisabled = (requestRouterSetting == 'disabled');


        try {
            // Do we have a request id
            if (requestId == null) {
                // We do not so it is an error
                processedSuccessfully = false;
                errorType = ERROR_TYPE_BADLY_FORMED_MESSAGE;
            } else {
                // We do have a request id so start a new transaction
                PatronRequest.withTransaction { status ->
                    // Lookup the request
                    PatronRequest request = lookupPatronRequestWithRole(requestId, true);

                    // Did we find the request
                    if (request == null) {
                        log.warn("Unable to locate PatronRequest corresponding to ID or Hrid \"${requestId}\".");
                        String peerId = getPeerId(eventData);
                        if (peerId != null) {
                            log.warn("Looking to see if we can find the request by the requester id \"${peerId}\".");
                            request = lookupPatronRequestByPeerId(peerId, true);
                        }
                    }

                    // Did we manage to find the request
                    if (request == null) {
                        // We do not so it is an error
                        processedSuccessfully = false;
                        errorType = ERROR_TYPE_UNABLE_TO_FIND_REQUEST;
                    } else {
                        if (routingDisabled) {
                            String requestingSymbol = requestingSymbolFromEventData(eventData);
                            String supplyingSymbol = supplyingSymbolFromEventData(eventData);
                            if (requestingSymbol) {
                                request.requestingInstitutionSymbol = requestingSymbol;
                            }
                            if (supplyingSymbol) {
                                if (request.supplyingInstitutionSymbol != supplyingSymbol) {
                                    request.stateHasChanged = true
                                }
                                request.supplyingInstitutionSymbol = supplyingSymbol;
                            }
                        }  else if (((request.supplyingInstitutionSymbol == null ||
                                request.getSupplyingInstitutionSymbol().contains("null") ||
                                    request.getSupplyingInstitutionSymbol() == ":") &&
                                        eventData.header?.supplyingAgencyId?.agencyIdType != null &&
                                            eventData.header?.supplyingAgencyId?.agencyIdValue != null) ||
                                EventMessageRequestIndService.isSlnpRequesterStateModel(request)) {
                            Symbol resolvedSupplyingAgency = null
                            String symbol = ""
                            if (eventData.header?.supplyingAgencyId?.agencyIdType != null &&
                                    eventData.header?.supplyingAgencyId?.agencyIdValue != null) {
                                resolvedSupplyingAgency = DirectoryEntryService.resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue)
                                symbol = "${eventData.header.supplyingAgencyId.agencyIdType.toString()}:${eventData.header.supplyingAgencyId.agencyIdValue.toString()}"
                            }
                            request.resolvedSupplier = resolvedSupplyingAgency
                            request.setSupplyingInstitutionSymbol(symbol)
                        }
                        // We need to determine if this request is for the current rota position
                        if (isForCurrentRotaLocation(eventData, request) ||
                                (StateModel.MODEL_SLNP_REQUESTER == request.stateModel.shortcode ||
                                        StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER == request.stateModel.shortcode)) {
                            // We now need to execute the action for the message
                            String actionToPerform = getActionToPerform(eventData);

                            // Ensure we have an action
                            if (actionToPerform == null) {
                                // We have not been supplied an action
                                processedSuccessfully = false;
                                errorType = isRequester() ? ERROR_TYPE_NO_REASON_FOR_MESSAGE : ERROR_TYPE_NO_ACTION;
                            } else {
                                // Now perform the action
                                ActionResultDetails actionResults = actionService.performAction('ISO18626' + actionToPerform, request, eventData);

                                // Deal with what we have been returned
                                processedSuccessfully = actionResults.result == ActionResult.SUCCESS;
                                errorType = actionResults.responseResult.errorType;
                                errorValue = actionResults.responseResult.errorValue;
                            }
                        } else {
                            // We treat this as no active request
                            processedSuccessfully = false;
                            errorType = ERROR_TYPE_NO_ACTIVE_REQUEST;
                            errorValue = 'requestingAgencyRequestId: ' + eventData.header?.requestingAgencyRequestId +
                                         ', supplyingAgencyRequestId: ' + eventData.header?.supplyingAgencyRequestId;
                        }
                        requestUuid = request.id
                    }
                }
            }
        } catch (Exception e) {
            // Just let the caller know we were unable to process it
            processedSuccessfully = false;
            errorType = ERROR_TYPE_UNABLE_TO_PROCESS;
            log.error('Exception thrown while processing ISO18626 message', e);
        }

        // Log our response to processing this message
        log.info('Processed incoming ISO-18626 message, result: ' +  (processedSuccessfully ? 'OK' : 'ERROR') +
                 ((errorType == null) ? '' : (', Error Type: ' + errorType)) +
                 ((errorValue == null) ? '' : (', Error Value: ' + errorValue.toString())));

        // Build up the response
        eventResultDetails.responseResult = createResponseData(eventData, processedSuccessfully, errorType, errorValue);
        eventResultDetails.responseResult.requestId = requestUuid
        return(eventResultDetails);
    }

    public Map responseData(Map eventData, String messageType, boolean success, String errorType, Object errorValue) {
        Map data = [ : ];
        data.messageType = messageType;
        data.status = success ? STATUS_OK : STATUS_ERROR;
        if (errorType != null) {
            data.errorType = errorType;
            if (errorValue != null) {
                data.errorValue = errorValue;
            }
        }

        // Now for all the header details
        data.supIdType = eventData.header.supplyingAgencyId.agencyIdType;
        data.supId = eventData.header.supplyingAgencyId.agencyIdValue;
        data.reqAgencyIdType = eventData.header.requestingAgencyId.agencyIdType;
        data.reqAgencyId = eventData.header.requestingAgencyId.agencyIdValue;
        data.reqId = eventData.header.requestingAgencyRequestId;
        data.timeRec = eventData.header.timestamp;
        return(data);
    }

    public PatronRequest lookupPatronRequestWithRole(String id, boolean withLock = false) {
        log.debug("LOCKING ReshareApplicationEventHandlerService::lookupPatronRequestWithRole(${id},${withLock})");
        PatronRequest result = PatronRequest.createCriteria().get {
            and {
                or {
                    eq('id', id)
                    eq('hrid', id)
                }
                eq('isRequester', isRequester())
            }
            lock withLock
        }

        log.debug("LOCKING ReshareApplicationEventHandlerService::lookupPatronRequestWithRole located ${result?.id}/${result?.hrid}");

        return result;
    }

    public PatronRequest lookupPatronRequestByPeerId(String id, boolean withLock) {
        PatronRequest result = PatronRequest.createCriteria().get {
            eq('peerRequestIdentifier', id)
            lock withLock
        };
        return result;
    }
}
