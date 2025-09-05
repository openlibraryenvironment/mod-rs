package org.olf.rs

import groovy.json.JsonSlurper
import org.olf.rs.statemodel.StateModel;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.iso18626.NoteSpecials;
import org.olf.rs.iso18626.ReasonForMessage;
import org.olf.rs.logging.IIso18626LogDetails;
import org.olf.rs.logging.ProtocolAuditService;
import org.olf.rs.PatronRequestNotification;
import org.olf.rs.patronstore.PatronStoreActions;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.ActionEvent;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Status;

import groovy.json.JsonBuilder;
import groovy.time.Duration;

/**
 * Handle user events.
 *
 * wheras ReshareApplicationEventHandlerService is about detecting and handling
 * system generated events - incoming protocol messages etc this class is the
 * home for user triggered activities - checking an item into reshare, marking
 * the pull slip as printed etc.
 */
public class ReshareActionService {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd[ ]['T']HH:mm[:ss][.SSS][z][XXX][Z]";

    private static final String MESSAGE_NOTIFICATION = 'Notification';

    private static final String PROTOCOL_ERROR_UNABLE_TO_SEND   = 'Unable to send protocol message (';
    private static final String PROTOCOL_ERROR_UNABLE_TO_SEND_1 = ')';

    HostLMSPatronProfileService hostLMSPatronProfileService;
    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
    ProtocolAuditService protocolAuditService;
    ProtocolMessageService protocolMessageService;
    ProtocolMessageBuildingService protocolMessageBuildingService;
    HostLMSService hostLMSService;
    PatronStoreService patronStoreService;
    SettingsService settingsService;

    /**
     * Looks up a patron identifier to see if it is valid for requesting or not
     * @param request the patron request that any auditing should be associated with
     * @param actionParams the parameters that we use to make our decision
     *      patronIdentifier ... the id to be checked
     *      override ... if the patron turns out to be invalid, this allows us to say they are valid
     * @return a map containing the result of the call that can contain the following fields:
     *      callSuccess ... was the call a success or not
     *      patronDetails ... the details of the patron if the patron is a valid user
     *      patronValid ... can the patron create requests
     *      problems ... An array of reasons that explains either a FAIL or the patron is not valid
     *      status ... the status of the patron (FAIL or OK)
     *
     */
    private Map lookupPatronInternal(PatronRequest request, Map actionParams) {
        // The result object
        Map result = [callSuccess: false, patronValid: false ];
        Map patronDetails = hostLMSService.lookupPatron(request, actionParams.patronIdentifier);
        if (patronDetails != null) {
            if (patronDetails.result || actionParams.override) {
                result.callSuccess = true;

                // Ensure the patron details has a user id
                if (patronDetails.userid == null) {
                    patronDetails.userid = actionParams.patronIdentifier;
                }

                // Check the patron profile and record if we have not seen before
                HostLMSPatronProfile patronProfile = null
                if (patronDetails.userProfile != null) {
                    patronProfile = hostLMSPatronProfileService.ensureActive(patronDetails.userProfile, patronDetails.userProfile);
                }

                // Is it a valid patron or are we overriding the fact it is valid
                if (isValidPatron(patronDetails, patronProfile) || actionParams.override) {
                    result.patronValid = true;
                }
            }

            // If there are problems with the patron let the caller know
            if (patronDetails.problems) {
                result.problems = patronDetails.problems.toString();
            }

            // Set the status in the result
            result.status = patronDetails.status;
        }

        // Let the caller know the patron details
        result.patronDetails = patronDetails;

        return(result);
    }

    /**
     * Looks up the patron without an active request
     * @param actionParams The parameters required for the lookup
     * @return a Map containing the result of the lookup
     */
    public Map lookupPatron(Map actionParams) {
        return(lookupPatronInternal(null, actionParams));
    }

    /*
     * WARNING: this method is NOT responsible for saving or for managing state
     * changes. It simply performs the lookupAction and appends relevant info to the
     * patron request
     */
    public Map lookupPatron(PatronRequest pr, Map actionParams) {
        // Ensure actionParams exists as an object
        Map params = actionParams;
        if (params == null) {
            // Allocate an empty object so we can set the patronIdentifier
            params = [ : ];
        }

        // before we call lookupPatron we need to set the patronIdentifier on the actionParams
        params.patronIdentifier = pr.patronIdentifier;
        Map result = lookupPatronInternal(pr, params);

        if (result.patronDetails != null) {
            if (result.patronDetails.userid != null) {

                pr.resolvedPatron = lookupOrCreatePatronProxy(result.patronDetails);
                if (pr.patronSurname == null && (result.patronDetails.surname?.length() > 0)) {
                    pr.patronSurname = result.patronDetails.surname;
                }
                if (pr.patronGivenName == null && (result.patronDetails.givenName?.length() > 0)) {
                        pr.patronGivenName = result.patronDetails.givenName;
                }
                if (pr.patronEmail == null && (result.patronDetails.email?.length() > 0)) {
                    pr.patronEmail = result.patronDetails.email;
                }
            }

            if (result.patronDetails.patronUuid) {
                Map customIdentifiersMap = [:]
                if (pr.customIdentifiers) {
                    customIdentifiersMap = new JsonSlurper().parseText(pr.customIdentifiers)
                }
                customIdentifiersMap.put("patronUuid", result.patronDetails.patronUuid)
                pr.customIdentifiers = new JsonBuilder(customIdentifiersMap).toPrettyString()
            }

            // Is the patron is valid, add an audit entry
            if (result.patronValid) {
                String reason = result.patronDetails.reason == 'spoofed' ? '(No host LMS integration configured for borrower check call)' : 'Host LMS integration: borrower check call succeeded.';
                String outcome = actionParams?.override ? 'validation overriden' : 'validated';
                String message = "Patron ${outcome}. ${reason}";
                auditEntry(pr, pr.state, pr.state, message, null);
            }
        }

        // Do not pass the actual patron details back
        result.remove('patronDetails');
        return(result);
    }

    /**
     *  send a message.
     *  It appears this method can be called from multiple places including controllers and other services.
     *  Previously, we relied upon groovy magic to allow actionParams be a controller params object or a standard
     *  map. However, a standard map does not support isNull. In order to detect and tidy this, the method signture
     *  is changed to an explicit Map and the test for a note property is done via the map interface and not
     *  the special isNull method injected by the controller object (Which then breaks this method if called from another service).
     */
    public boolean sendMessage(PatronRequest pr, Map actionParams, EventResultDetails eventResultDetails) {
        log.debug("actionMessage(${pr})");
        boolean result = false;
    // Sending a message does not change the state of a request

        // If the actionParams does not contain a note then this method should do nothing
        if (actionParams.get('note') != null) {
            // This is for sending a REQUESTING AGENCY message to the SUPPLYING AGENCY
            if (pr.isRequester == true) {
                result = sendRequestingAgencyMessage(pr, MESSAGE_NOTIFICATION, actionParams, eventResultDetails)
            } else {
                // This is for sending a SUPPLYING AGENCY message to the REQUESTING AGENCY
                result = sendSupplyingAgencyMessage(pr, ReasonForMessage.MESSAGE_REASON_NOTIFICATION, null, actionParams, eventResultDetails)
            }

            if (result == false) {
                log.warn('Unable to send protocol notification message');
            }
        }

        return result;
    }

    public boolean sendSupplierCancelResponse(PatronRequest pr, Map actionParams, EventResultDetails eventResultDetails) {
        /* This method will send a cancellation response iso18626 message */

        log.debug("sendSupplierCancelResponse(${pr})");
        boolean result = false;
        String status;

        if (!actionParams.get('cancelResponse') != null) {
            switch (actionParams.cancelResponse) {
                case 'yes':
                    status = 'Cancelled';
                    break;

                case 'no':
                    break;

                default:
                    log.warn("sendSupplierCancelResponse received unexpected cancelResponse: ${actionParams.cancelResponse}")
                    break;
            }

            // Only the supplier should ever be able to send one of these messages, otherwise something has gone wrong.
            if (pr.isRequester == false) {
                result = sendSupplyingAgencyMessage(pr, ReasonForMessage.MESSAGE_REASON_CANCEL_RESPONSE, status, actionParams, eventResultDetails);
            } else {
                log.warn('The requesting agency should not be able to call sendSupplierConditionalWarning.');
            }
        } else {
            log.error('sendSupplierCancelResponse expected to receive a cancelResponse');
        }

        return result;
    }

    public boolean sendRequestingAgencyMessage(
        PatronRequest pr,
        String action,
        Map messageParams,
        EventResultDetails eventResultDetails,
        Map retryEventData = null
    ) {
        String requestRouterSetting = settingsService.getSettingValue('routing_adapter');
        boolean result = false;
        boolean isSlnpModel = StateModel.MODEL_SLNP_REQUESTER.equalsIgnoreCase(pr.stateModel.shortcode) ||
                StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER.equalsIgnoreCase(pr.stateModel.shortcode)
        boolean routingDisabled = (requestRouterSetting == 'disabled');
        boolean disregardRota = (isSlnpModel || routingDisabled);

        Long rotaPosition = pr.rotaPosition;
        // We check that it is sensible to send a message, ie that we have a non-empty rota and are pointing at an entry in that.
        if (!disregardRota && pr.rota.isEmpty()) {
            log.error('sendRequestingAgencyMessage has been given an empty rota');
        } else if (!disregardRota && rotaPosition == null) {
            log.error('sendRequestingAgencyMessage could not find current rota postition');
        } else if (!disregardRota && pr.rota.empty()) {
            log.error('sendRequestingAgencyMessage has been handed an empty rota');
        } else {

            Map eventData = retryEventData;
            Map symbols = null;
            /*
            Map symbols = isSlnpModel ? [ senderSymbol: pr.requestingInstitutionSymbol, receivingSymbol: pr.resolvedSupplier ? pr.supplyingInstitutionSymbol : pr.requestingInstitutionSymbol] :
                    requestingAgencyMessageSymbol(pr);

             */

            if (isSlnpModel) {
                symbols = [
                        senderSymbol: pr.requestingInstitutionSymbol,
                        receivingSymbol: pr.resolvedSupplier ? pr.supplyingInstitutionSymbol : pr.requestingInstitutionSymbol
                ];
            } else if (routingDisabled) {
                String defaultPeerSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_PEER_SYMBOL);
                String defaultRequestSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL);
                if (!defaultPeerSymbolString) {
                    log.error("No defaultPeerSymbol defined");
                }
                symbols = [
                        senderSymbol: defaultRequestSymbolString,
                        receivingSymbol: defaultPeerSymbolString
                ];
            } else {
                symbols = requestingAgencyMessageSymbol(pr);
            }

            // If we have not been supplied with the event data, we need to generate it
            if (eventData == null) {
                String note = messageParams?.note
                eventData = protocolMessageBuildingService.buildRequestingAgencyMessage(pr, symbols.senderSymbol, symbols.receivingSymbol, action, note);
                if (eventResultDetails != null) {
                    eventResultDetails.messageSequenceNo = pr.lastSequenceSent;
                }
            }

            // Now send the message
            result = sendProtocolMessage(pr, symbols.senderSymbol, symbols.receivingSymbol, eventData, false);
        }
        return result;
    }

    public Map requestingAgencyMessageSymbol(PatronRequest request) {
        Map symbols = [ senderSymbol: request.requestingInstitutionSymbol ];

        PatronRequestRota patronRota = request.rota.find({ rotaLocation -> rotaLocation.rotaPosition == request.rotaPosition });
        if (patronRota != null) {
            symbols.receivingSymbol = "${patronRota.peerSymbol.authority.symbol}:${patronRota.peerSymbol.symbol}".toString();
        }
        return(symbols);
    }

    /**
     * Creates outgoing notification based on eventData and protocol response
     * Replaces the original outgoingNotificationEntry calls that occurred during message building
     */
    private void createOutgoingNotification(PatronRequest pr, Map eventData, Map sendResult) {
        if (eventData.messageType == 'SUPPLYING_AGENCY_MESSAGE' || eventData.messageType == 'REQUESTING_AGENCY_MESSAGE') {
            PatronRequestNotification n = new PatronRequestNotification();
            n.patronRequest = pr;
            n.timestamp = Instant.now();
            n.seen = false;
            n.isSender = true;
            // Strip sequence tracking from end of note
            // For requester messages, note is in eventData.note
            // For supplier messages, note is in eventData.messageInfo.note
            String displayNote = eventData.note ?: eventData.messageInfo?.note;
            if (displayNote != null) {
                int seqIndex = displayNote.lastIndexOf(NoteSpecials.SEQUENCE_PREFIX);
                if (seqIndex >= 0) {
                    displayNote = displayNote.substring(0, seqIndex);
                }
            }
            n.messageContent = displayNote;
            n.messageStatus = sendResult?.response?.messageStatus;

            if (eventData.messageType == 'SUPPLYING_AGENCY_MESSAGE') {
                n.attachedAction = eventData.messageInfo?.reasonForMessage;
                n.actionStatus = eventData.statusInfo?.status;

                if (eventData.deliveryInfo?.loanCondition) {
                    n.actionStatus = "Conditional";
                    n.actionData = eventData.deliveryInfo.loanCondition;
                }

                if (eventData.messageInfo?.reasonUnfilled) {
                    n.actionData = eventData.messageInfo.reasonUnfilled;
                }

                n.messageSender = pr.resolvedSupplier;
                n.messageReceiver = pr.resolvedRequester;
                n.senderSymbol = pr.supplyingInstitutionSymbol;

            } else if (eventData.messageType == 'REQUESTING_AGENCY_MESSAGE') {
                n.attachedAction = eventData.action;

                n.messageSender = pr.resolvedRequester;
                n.messageReceiver = pr.resolvedSupplier;
                n.senderSymbol = pr.requestingInstitutionSymbol;
            }

            pr.addToNotifications(n);
        }
    }

    public boolean sendProtocolMessage(PatronRequest request, String sendingSymbol, String receivingSymbol, Map eventData, boolean resetSendAttempts = true) {
        boolean result = false;

        // Set the status to Waiting
        request.networkStatus = NetworkStatus.Waiting;

        // Set the lastSendAttempt
        request.lastSendAttempt = new Date();

        // Do we need to rest the number of send attempts
        if (resetSendAttempts) {
            request.numberOfSendAttempts = 0;
        }

        IIso18626LogDetails iso18626LogDetails = protocolAuditService.getIso18626LogDetails();
        Map sendResult  = protocolMessageService.sendProtocolMessage(sendingSymbol, receivingSymbol, eventData, iso18626LogDetails);
        protocolAuditService.save(request, iso18626LogDetails);

        switch (sendResult.status) {
            case ProtocolResultStatus.Sent:
                // TODO: Need to take into account a status of ERROR being returned in the protocol message, this assumed everything was received and processed without problems at the moment
                // Mark, it as sent, no longer need the eventData
                setNetworkStatus(request, NetworkStatus.Sent, null, false);
                result = true;
                break;

            case ProtocolResultStatus.Timeout:
                // Mark it down to a timeout, need to save the event data for a potential retry
                setNetworkStatus(request, NetworkStatus.Timeout, eventData, true);
                auditEntry(request, request.state, request.state, 'Encountered a network timeout while trying to send a message', null);
                log.warn('Hit a timeout trying to send protocol message: ' + sendResult.toString());
                break;

            case ProtocolResultStatus.Error:
                // Mark it as a retry, need to save the event data
                setNetworkStatus(request, NetworkStatus.Retry, eventData, true);
                auditEntry(request, request.state, request.state, 'Encountered a network error while trying to send message', null);
                log.warn(PROTOCOL_ERROR_UNABLE_TO_SEND + sendResult.toString() + PROTOCOL_ERROR_UNABLE_TO_SEND_1);
                break;

            case ProtocolResultStatus.ProtocolError:
                log.error('Encountered a protocol error for request: ' + request.id);
                setNetworkStatus(request, NetworkStatus.Error, eventData, false);
                auditEntry(request, request.state, request.state, 'Protocol error interpreting response', sendResult.response);
                // Should we set the status to error as it now requires manual intervention ?
                break;

            case ProtocolResultStatus.ValidationError:
                log.error('Encountered a protocol error for request: ' + request.id)
                setNetworkStatus(request, NetworkStatus.Error, eventData, false)
                auditEntry(request, request.state, request.state, sendResult.response, null)
                break
        }

        // For requester messages, note is in eventData.note
        // For supplier messages, note is in eventData.messageInfo.note
        String messageNote = eventData.note ?: eventData.messageInfo?.note
        
        // If we are notifying the other side of a field change then we do not want to record it
        if (messageNote != null && !messageNote.startsWith(NoteSpecials.UPDATE_FIELD)) {
            createOutgoingNotification(request, eventData, sendResult)
        }

        return(result);
    }

    /**
     * Sets the network status for the request, along with the next processing time if required
     * @param request The request that needs to be updated
     * @param networkStatus The network status to set it to
     * @param eventData The event data used to generate the protocol message
     * @param retry Whether we are going to retry or not
     */
    public void setNetworkStatus(PatronRequest request, NetworkStatus networkStatus, Map eventData, boolean retry) {
        // Set the network status
        request.networkStatus = networkStatus;

        // Set when we retry
        if (retry) {
            // Retry required, so we need to increment the number of send attempts
            if (request.numberOfSendAttempts == null) {
                request.numberOfSendAttempts = 1;
            } else {
                request.numberOfSendAttempts++;
            }

            // Have we reached the maximum number of retries
            int maxSendAttempts = settingsService.getSettingAsInt(SettingsData.SETTING_NETWORK_MAXIMUM_SEND_ATTEMPTS, 3, false);

            // Have we reached our maximum number
            if ((maxSendAttempts > 0) && (request.numberOfSendAttempts > maxSendAttempts)) {
                // We have so decrement our number of attempts as we have already incremented it
                request.numberOfSendAttempts--;

                // set the retry period to null
                request.nextSendAttempt = null;

                // Set the network status to error
                request.networkStatus = NetworkStatus.Error;

                // TODO: Should we set the status at this point to something like network error and introduce a new action ReSend to allow the user to recover ...

                // Finally add an audit record to say what we have done
                auditEntry(request, request.state, request.state, 'Maximum number of send attempts reached, setting network status to Error', null);
            } else {
                // We want to retry, get hold of the retry period
                int retryMinutes = settingsService.getSettingAsInt(SettingsData.SETTING_NETWORK_RETRY_PERIOD, 10, false);

                // We have a multiplier to the retry minutes based on the number of times we have attempted to send it
                // At a minimum the number of send attempts should be 1 (only includes those we have attempted and not the one we are about to do)
                int retryMultiplier =  (int)((request.numberOfSendAttempts + 5) / 6);

                // Now set when the next attempt will be
                Duration retryDuration = new Duration(0, 0, retryMinutes * retryMultiplier, 0, 0);
                request.nextSendAttempt = retryDuration.plus(new Date());
            }
        } else {
            // No retry required
            request.nextSendAttempt = null;
        }

        // Set the last protocol data
        if (eventData == null) {
            // Just set it to null as we have none
            request.lastProtocolData = null;
        } else {
            // Convert the event data into a json string
            request.lastProtocolData = (new JsonBuilder(eventData)).toString();
        }
    }

    public void sendResponse(
        PatronRequest pr,
        String status,
        Map responseParams,
        EventResultDetails eventResultDetails
    ) {
        sendSupplyingAgencyMessage(pr, determineISO18626ReasonForMessage(pr), status, responseParams, eventResultDetails);
    }

    private String determineISO18626ReasonForMessage(PatronRequest request) {
        // Default to status change
        String reasonForMessage = ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE;

        // Only the first response can be a RequestResponse
        if (!request.sentISO18626RequestResponse && (request.stateModel.shortcode != StateModel.MODEL_SLNP_RESPONDER &&
                request.stateModel.shortcode != StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER)) {
            // it has not previously been sent, so send the RequestResponse as the reason for the message
            reasonForMessage = ReasonForMessage.MESSAGE_REASON_REQUEST_RESPONSE;

            // And we need to mark it as being sent, so we do not send it again
            request.sentISO18626RequestResponse = true;
        }

        // Return it to the caller
        return(reasonForMessage);
    }

    // Unused ??
    public void sendStatusChange(
        PatronRequest pr,
        String status,
        EventResultDetails eventResultDetails,
        String note = null,
        boolean appendSequence = true
    ) {
        Map params = [:]
        if (note) {
            params = [note: note]
        }

        sendSupplyingAgencyMessage(pr, ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE, status, params, eventResultDetails, null, appendSequence);
    }

    // see
    // http://biblstandard.dk/ill/dk/examples/request-without-additional-information.xml
    // http://biblstandard.dk/ill/dk/examples/supplying-agency-message-delivery-next-day.xml
    // RequestReceived, ExpectToSupply, WillSupply, Loaned, Overdue, Recalled,
    // RetryPossible,
    // Unfilled, CopyCompleted, LoanCompleted, CompletedWithoutReturn, Cancelled
    public boolean sendSupplyingAgencyMessage(
        PatronRequest pr,
        String reasonForMessage,
        String status,
        Map messageParams,
        EventResultDetails eventResultDetails,
        Map retryEventData = null,
        boolean appendSequence = true
    ) {

        log.debug('sendResponse(....)');
        String requestRouterSetting = settingsService.getSettingValue('routing_adapter');
        boolean routingDisabled = (requestRouterSetting == 'disabled');
        boolean result = false;

        // pr.supplyingInstitutionSymbol
        // pr.peerRequestIdentifier
        if (routingDisabled) {

            String defaultPeerSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_PEER_SYMBOL);
            String defaultRequestSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL);
            Map supplyingMessageRequest;

            if (retryEventData != null) {
                supplyingMessageRequest = retryEventData;
            } else {
                supplyingMessageRequest = protocolMessageBuildingService.buildSupplyingAgencyMessage(
                        pr, reasonForMessage, status, messageParams, appendSequence);
            }
            if (eventResultDetails) {
                eventResultDetails.messageSequenceNo = pr.lastSequenceSent;
            }
            Map symbols = [
                    senderSymbol: defaultRequestSymbolString,
                    receivingSymbol: defaultPeerSymbolString
            ];
            result = sendProtocolMessage(pr, symbols.senderSymbol, symbols.receivingSymbol, supplyingMessageRequest, false);

        } else if ((pr.resolvedSupplier != null) &&
            (pr.resolvedRequester != null)) {
            Map supplyingMessageRequest = retryEventData;

            // If it is not a retry we need to generate the message
            if (supplyingMessageRequest == null) {
                supplyingMessageRequest = protocolMessageBuildingService.buildSupplyingAgencyMessage(pr, reasonForMessage, status, messageParams, appendSequence);
                if (eventResultDetails) {
                    eventResultDetails.messageSequenceNo = pr.lastSequenceSent;
                }
            }

            // Now send the message
            Map symbols = supplyingAgencyMessageSymbol(pr);
            result = sendProtocolMessage(
                pr,
                symbols.senderSymbol,
                symbols.receivingSymbol,
                supplyingMessageRequest,
                false
            );
        } else {
            log.error("Unable to send protocol message - supplier(${pr.resolvedSupplier}) or requester(${pr.resolvedRequester}) is missing in PatronRequest ${pr.id}Returned");
        }

        return result;
    }

    public Map supplyingAgencyMessageSymbol(PatronRequest request) {
        return([
             senderSymbol: request.supplyingInstitutionSymbol,
             receivingSymbol: request.requestingInstitutionSymbol
        ]);
    }


    protected Date parseDateString(String dateString, String dateFormat = DEFAULT_DATE_FORMAT) {
        Date date;

        log.debug("Attempting to parse input date string '${dateString}' with format string '${dateFormat}'")

        if (dateString == null) {
            throw new Exception('Attempted to parse null as date')
        }

        // Ensure we have a date format
        String dateFormatToUse = dateFormat;
        if (!dateFormatToUse?.trim()) {
            // It is null or is all whitespace, so use the default format
            dateFormatToUse = DEFAULT_DATE_FORMAT;
        }

        // If the format is less than 12 characters we assume it is just a date and has no time
        if (dateFormatToUse.length() < 12) {
            // Failed miserably to just convert a date without time elements using LocalDate, ZonedDateTime or LocalDateTime
            // So have fallen back on the SimpleDateFormat
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatToUse);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
            date = simpleDateFormat.parse(dateString);
        } else {
            // See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html#patterns
            // for the appropriate patterns
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatToUse);

            try {
                date = Date.from(ZonedDateTime.parse(dateString, formatter).toInstant());
            } catch (Exception e) {
                log.debug("Failed to parse ${dateString} as ZonedDateTime, falling back to LocalDateTime");
                date = Date.from(LocalDateTime.parse(dateString, formatter).toInstant(ZoneOffset.UTC));
            }
        }
        return date
    }

    private Patron lookupOrCreatePatronProxy(Map patronDetails) {
        Patron result = null;
        PatronStoreActions patronStoreActions;
        patronStoreActions = patronStoreService.getPatronStoreActions();
        log.debug("patronStoreService is currently ${patronStoreService}");
        try {
            patronStoreActions.updateOrCreatePatronStore(patronDetails.userid, patronDetails);
        } catch (Exception e) {
            log.error("Unable to update or create Patron Store: ${e}");
        }

        if ((patronDetails != null) &&
            (patronDetails.userid != null) &&
            (patronDetails.userid.trim().length() > 0)) {
            result = Patron.findByHostSystemIdentifier(patronDetails.userid);
            if (result == null) {
                result = new Patron(
                    hostSystemIdentifier: patronDetails.userid,
                               givenname: patronDetails.givenName,
                                 surname: patronDetails.surname,
                             userProfile: patronDetails.userProfile
                );
            } else {
                // update the patron record
                result.givenname = patronDetails.givenName;
                result.surname = patronDetails.surname;
                result.userProfile = patronDetails.userProfile;
            }

            // We have either created or updated, so save it now
            result.save(flush:true, failOnError:true);
        }
        return result;
    }

    private boolean isValidPatron(Map patronRecord, HostLMSPatronProfile patronProfile) {
        boolean result = false;
        log.debug("Check isValidPatron: ${patronRecord}");
        if (patronRecord != null) {
            /*
             *  They can request if
             * 1. It is a valid patron record (status = OK)
             * 2. There is no patron profile or the canCreateRequests field is not set or true
             */
            if (patronRecord.status == 'OK') {
                if ((patronProfile == null) ||
                    (patronProfile.canCreateRequests == null) ||
                    (patronProfile.canCreateRequests == true)) {
                    result = true;
                } else {
                    patronRecord.problems = ["Patron profile (${patronProfile.code}) is configured to not allow requesting in ReShare."];
                }
            } else if (patronRecord.problems == null) {
                patronRecord.problems = ['Record status is not valid.'];
            }
        }
        return result;
    }

    private void auditEntry(
        PatronRequest pr,
        Status from,
        Status to,
        String message,
        Map data,
        ActionEvent actionEvent  = null,
        Integer messageSequenceNo = null
    ) {
        // Just call the one in the event handler service
        reshareApplicationEventHandlerService.auditEntry(pr, from, to, message, data, actionEvent, messageSequenceNo);
    }
}
