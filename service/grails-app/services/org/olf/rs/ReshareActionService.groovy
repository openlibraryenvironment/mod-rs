package org.olf.rs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.patronstore.PatronStoreActions;
import org.olf.rs.statemodel.Status;

/**
 * Handle user events.
 *
 * wheras ReshareApplicationEventHandlerService is about detecting and handling
 * system generated events - incoming protocol messages etc this class is the
 * home for user triggered activities - checking an item into reshare, marking
 * the pull slip as printed etc.
 */
public class ReshareActionService {

    private static final String MESSAGE_NOTIFICATION = 'Notification';

    private static final String PROTOCOL_ERROR_UNABLE_TO_SEND   = 'Unable to send protocol message (';
    private static final String PROTOCOL_ERROR_UNABLE_TO_SEND_1 = ')';

    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
    ProtocolMessageService protocolMessageService;
    ProtocolMessageBuildingService protocolMessageBuildingService;
    HostLMSService hostLMSService;
    PatronNoticeService patronNoticeService;
    PatronStoreService patronStoreService;

    /**
     * Looks up a patron identifier to see if it is valid for requesting or not
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
    public Map lookupPatron(Map actionParams) {
        // The result object
        Map result = [callSuccess: false, patronValid: false ];
        Map patronDetails = hostLMSService.getHostLMSActions().lookupPatron(actionParams.patronIdentifier);
        if (patronDetails != null) {
            if (patronDetails.result) {
                result.callSuccess = true;

                // Ensure the patron details has a user id
                if (patronDetails.userid == null) {
                    patronDetails.userid = actionParams.patronIdentifier;
                }

                // Check the patron profile and record if we have not seen before
                HostLMSPatronProfile patronProfile = null
                if (patronDetails.userProfile != null) {
                    patronProfile = HostLMSPatronProfile.findByCode(patronDetails.userProfile);
                    if (patronProfile == null) {
                        patronProfile = new HostLMSPatronProfile(code:patronDetails.userProfile, name:patronDetails.userProfile);
                        patronProfile.save(flush:true, failOnError:true);

                        // Trigger a notice to be sent if it has been configured
                        patronNoticeService.triggerNotices(patronProfile);
                    } else if (patronProfile.hidden == true) {
                        // Unhide it as it is active again
                        patronProfile.hidden = false;
                        patronProfile.save(flush:true, failOnError:true);
                    }
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
        Map result = lookupPatron(params);

        if (result.patronDetails != null) {
            if (result.patronDetails.userid != null) {
                pr.resolvedPatron = lookupOrCreatePatronProxy(result.patronDetails);
                if (pr.patronSurname == null) {
                    pr.patronSurname = result.patronDetails.surname;
                }
                if (pr.patronGivenName == null) {
                    pr.patronGivenName = result.patronDetails.givenName;
                }
                if (pr.patronEmail == null) {
                    pr.patronEmail = result.patronDetails.email;
                }
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
    public boolean sendMessage(PatronRequest pr, Map actionParams) {
        log.debug("actionMessage(${pr})");
        boolean result = false;
    // Sending a message does not change the state of a request

        // If the actionParams does not contain a note then this method should do nothing
        if (actionParams.get('note') != null) {
            // This is for sending a REQUESTING AGENCY message to the SUPPLYING AGENCY
            if (pr.isRequester == true) {
                result = sendRequestingAgencyMessage(pr, MESSAGE_NOTIFICATION, actionParams)
            } else {
                // This is for sending a SUPPLYING AGENCY message to the REQUESTING AGENCY
                result = sendSupplyingAgencyMessage(pr, MESSAGE_NOTIFICATION, null, actionParams)
            }

            if (result == false) {
                log.warn('Unable to send protocol notification message');
            }
        }

        return result;
    }

    public boolean sendSupplierCancelResponse(PatronRequest pr, Map actionParams) {
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
                result = sendSupplyingAgencyMessage(pr, 'CancelResponse', status, actionParams);
            } else {
                log.warn('The requesting agency should not be able to call sendSupplierConditionalWarning.');
            }
        } else {
            log.error('sendSupplierCancelResponse expected to receive a cancelResponse');
        }

        return result;
    }

    public boolean sendRequestingAgencyMessage(PatronRequest pr, String action, Map messageParams) {
        String note = messageParams?.note
        boolean result = false;

        Long rotaPosition = pr.rotaPosition;
        // We check that it is sensible to send a message, ie that we have a non-empty rota and are pointing at an entry in that.
        if (pr.rota.isEmpty()) {
            log.error('sendRequestingAgencyMessage has been given an empty rota');
        } else if (rotaPosition == null) {
            log.error('sendRequestingAgencyMessage could not find current rota postition');
        } else if (pr.rota.empty()) {
            log.error('sendRequestingAgencyMessage has been handed an empty rota');
        } else {
            String messageSenderSymbol = pr.requestingInstitutionSymbol;

            log.debug("ROTA: ${pr.rota}")
            log.debug("ROTA TYPE: ${pr.rota.getClass()}");
            PatronRequestRota prr = pr.rota.find({ rotaLocation -> rotaLocation.rotaPosition == rotaPosition });
            log.debug("ROTA at position ${pr.rotaPosition}: ${prr}");
            String peerSymbol = "${prr.peerSymbol.authority.symbol}:${prr.peerSymbol.symbol}";

            Map eventData = protocolMessageBuildingService.buildRequestingAgencyMessage(pr, messageSenderSymbol, peerSymbol, action, note);

            // Now send the message
            result = sendProtocolMessage(pr, messageSenderSymbol, peerSymbol, eventData, action);
        }
        return result;
    }

    public boolean sendProtocolMessage(PatronRequest request, String sendingSymbol, String receivingSymbol, Map eventData, String protocolAction, boolean resetSendAttempts = true) {
        boolean result = false;

        // Set the status to Waiting
        request.networkStatus = NetworkStatus.Waiting;

        // Set the lastSendAttempt
        request.lastSendAttempt = new Date();

        // Set the protocol action
        request.lastProtocolAction = protocolAction;

        // Do we need to rest the number of send attempts
        if (resetSendAttempts) {
            request.numberOfSendAttempts = 1;
        }

        Map sendResult  = protocolMessageService.sendProtocolMessage(sendingSymbol, receivingSymbol, eventData);
        switch (sendResult.status) {
            case ProtocolResultStatus.Sent:
                request.networkStatus = NetworkStatus.Sent;
                result = true;
                break;

            case ProtocolResultStatus.Timeout:
                // Mark it down to a timeout
                request.networkStatus = NetworkStatus.Timeout;
                log.warn('Hit a timeout trying to send protocol message: ' + sendResult);
                break;

            case ProtocolResultStatus.Error:
                // Mark it as a retry
                request.networkStatus = NetworkStatus.Retry;
                log.warn(PROTOCOL_ERROR_UNABLE_TO_SEND + sendResult + PROTOCOL_ERROR_UNABLE_TO_SEND_1);
                break;
        }

        return(result);
    }

    public void sendResponse(
        PatronRequest pr,
        String status,
        Map responseParams
    ) {
        sendSupplyingAgencyMessage(pr, 'RequestResponse', status, responseParams);
    }

    // Unused ??
    public void sendStatusChange(
        PatronRequest pr,
        String status,
        String note = null
    ) {
        Map params = [:]
        if (note) {
            params = [note: note]
        }

        sendSupplyingAgencyMessage(pr, 'StatusChange', status, params);
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
        Map messageParams
    ) {

        log.debug('sendResponse(....)');
        boolean result = false;

        // pr.supplyingInstitutionSymbol
        // pr.peerRequestIdentifier
        if ((pr.resolvedSupplier != null) &&
            (pr.resolvedRequester != null)) {
            Map supplyingMessageRequest = protocolMessageBuildingService.buildSupplyingAgencyMessage(pr, reasonForMessage, status, messageParams);

            // Now send the message
            result = sendProtocolMessage(
                pr,
                pr.supplyingInstitutionSymbol,
                pr.requestingInstitutionSymbol,
                supplyingMessageRequest,
                reasonForMessage
            );
        } else {
            log.error("Unable to send protocol message - supplier(${pr.resolvedSupplier}) or requester(${pr.resolvedRequester}) is missing in PatronRequest ${pr.id}Returned");
        }

        return result;
    }

    public void outgoingNotificationEntry(
        PatronRequest pr,
        String note,
        Map actionMap,
        Symbol messageSender,
        Symbol messageReceiver,
        Boolean isRequester
    ) {
        String attachedAction = actionMap.action;
        String actionStatus = actionMap.status;
        String actionData = actionMap.data;

        PatronRequestNotification outboundMessage = new PatronRequestNotification();
        outboundMessage.patronRequest = pr;
        outboundMessage.timestamp = Instant.now();
        outboundMessage.messageSender = messageSender;
        outboundMessage.messageReceiver = messageReceiver;
        outboundMessage.isSender = true;

        outboundMessage.attachedAction = attachedAction;
        outboundMessage.actionStatus = actionStatus;
        outboundMessage.actionData = actionData;

        outboundMessage.messageContent = note;

        log.debug("Outbound Message: ${outboundMessage.messageContent}");
        pr.addToNotifications(outboundMessage);
        //outboundMessage.save(flush:true, failOnError:true);
    }

    protected Date parseDateString(String dateString) {
        if (dateString == null) {
            throw new Exception('Attempted to parse null as date')
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd[ ]['T']HH:mm[:ss][.SSS][z][XXX][Z]");
        Date date;
        try {
            date = Date.from(ZonedDateTime.parse(dateString, formatter).toInstant());
        } catch (Exception e) {
            log.debug("Failed to parse ${dateString} as ZonedDateTime, falling back to LocalDateTime");
            date = Date.from(LocalDateTime.parse(dateString, formatter).toInstant(ZoneOffset.UTC));
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

    private void auditEntry(PatronRequest pr, Status from, Status to, String message, Map data) {
        reshareApplicationEventHandlerService.auditEntry(pr, from, to, message, data);
    }
}
