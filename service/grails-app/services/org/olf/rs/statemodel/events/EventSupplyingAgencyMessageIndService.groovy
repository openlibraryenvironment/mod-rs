package org.olf.rs.statemodel.events;

import java.util.regex.Matcher;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.RequestVolume;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Event that is called when a messages arrives for the supplier
 * @author Chas
 *
 */
public class EventSupplyingAgencyMessageIndService extends AbstractEvent {

    private static final String STATUS_ERROR = 'ERROR';
    private static final String STATUS_OK    = 'OK';

    private static final String VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION = 'awaiting_temporary_item_creation';

    private static final String ERROR_TYPE_BADLY_FORMED_MESSAGE = 'BadlyFormedMessage';

    private static final String[] FROM_STATES = [
    ];

    private static final String[] TO_STATES = [
        Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
        Status.PATRON_REQUEST_UNFILLED,
        Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
        Status.PATRON_REQUEST_SHIPPED,
        Status.PATRON_REQUEST_OVERDUE,
        Status.PATRON_REQUEST_RECALLED,
        Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER,
        Status.PATRON_REQUEST_REQUEST_COMPLETE
    ];

    ReshareActionService reshareActionService;

    @Override
    String name() {
        return(Events.EVENT_SUPPLYING_AGENCY_MESSAGE_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        // We are dealing with the transaction directly
        return(EventFetchRequestMethod.HANDLED_BY_EVENT_HANDLER);
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
        // We do not want want this event to appear anywhere
        return(model == StateModel.MODEL_REQUESTER);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // In our scenario the request will be null, as we do everything ourselves, so never reference that parameter
        // We use the responseResult field for returning data back to the caller

        /**
         * An incoming message to the requesting agency FROM the supplying agency - so we look in
         * eventData.header?.requestingAgencyRequestId to find our own ID for the request.
         * This should return everything that ISO18626Controller needs to build a confirmation message
         */

        Map result = [:];

        /* Occasionally the incoming status is not granular enough, so we deal with it separately in order
         * to be able to cater to "in-between" statuses, such as Conditional--which actually comes in as "ExpectsToSupply"
        */
        Map incomingStatus = eventData.statusInfo;

        try {
            if (eventData.header?.requestingAgencyRequestId == null) {
                result.status = STATUS_ERROR;
                result.errorType = ERROR_TYPE_BADLY_FORMED_MESSAGE;
                throw new Exception('requestingAgencyRequestId missing');
            }

            PatronRequest.withTransaction { status ->
                PatronRequest pr = lookupPatronRequestWithRole(eventData.header.requestingAgencyRequestId, true, true);

                if (pr == null) {
                    throw new Exception("Unable to locate PatronRequest corresponding to ID or hrid in requestingAgencyRequestId \"${eventData.header.requestingAgencyRequestId}\"");
                }

                // Save the current status
                Status currentState = pr.state;

                // if eventData.deliveryInfo.itemId then we should stash the item id
                if (eventData?.deliveryInfo) {
                    if (eventData?.deliveryInfo?.loanCondition) {
                        log.debug("Loan condition found: ${eventData?.deliveryInfo?.loanCondition}")
                        incomingStatus = [status: 'Conditional']

                        // Save the loan condition to the patron request
                        String loanCondition = eventData?.deliveryInfo?.loanCondition;
                        Symbol relevantSupplier = reshareApplicationEventHandlerService.resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue);
                        String note = eventData.messageInfo?.note;

                        reshareApplicationEventHandlerService.addLoanConditionToRequest(pr, loanCondition, relevantSupplier, note);
                    }

                    // If we're being told about the barcode of the selected item (and we don't already have one saved), stash it in selectedItemBarcode on the requester side
                    if (!pr.selectedItemBarcode && eventData.deliveryInfo.itemId) {
                        pr.selectedItemBarcode = eventData.deliveryInfo.itemId;
                    }

                    // Could recieve a single string or an array here as per the standard/our profile
                    Object itemId = eventData?.deliveryInfo?.itemId;
                    if (itemId) {
                        if (itemId instanceof Collection) {
                            // Item ids coming in, handle those
                            itemId.each { iid ->
                                Matcher matcher = iid =~ /multivol:(.*),((?!\s*$).+)/;
                                if (matcher.size() > 0) {
                                    // At this point we have an itemId of the form "multivol:<name>,<id>"
                                    String iidId = matcher[0][2];
                                    String iidName = matcher[0][1];

                                    // Check if a RequestVolume exists for this itemId, and if not, create one
                                    RequestVolume rv = pr.volumes.find { rv -> rv.itemId == iidId };
                                    if (!rv) {
                                        rv = new RequestVolume(
                                            name: iidName ?: pr.volume ?: iidId,
                                            itemId: iidId,
                                            status: RequestVolume.lookupStatus(VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION)
                                        );
                                        pr.addToVolumes(rv);
                                    }
                                }
                            }
                        } else {
                            // We have a single string, this is the usual standard case and should be handled as a single request volume
                            // Check if a RequestVolume exists for this itemId, and if not, create one
                            RequestVolume rv = pr.volumes.find { rv -> rv.itemId == itemId };
                            if (!rv) {
                                rv = new RequestVolume(
                                    name: pr.volume ?: itemId,
                                    itemId: itemId,
                                    status: RequestVolume.lookupStatus(VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION)
                                );
                                pr.addToVolumes(rv);
                            }
                        }
                    }
                }

                // Get hold of what message type this is
                String messageType = eventData.messageInfo?.reasonForMessage;

                // Awesome - managed to look up patron request - see if we can action
                if (messageType != null) {
                    // If there is a note, create notification entry
                    if (eventData.messageInfo?.note) {
                        reshareApplicationEventHandlerService.incomingNotificationEntry(pr, eventData, true);
                    }

                    switch (messageType) {
                        case 'RequestResponse':
                            break;
                        case 'StatusRequestResponse':
                            break;
                        case 'RenewResponse':
                            break;
                        case 'CancelResponse':
                            switch (eventData.messageInfo.answerYesNo) {
                                case 'Y':
                                    log.debug('Affirmative cancel response received')
                                    // The cancel response ISO18626 message should contain a status of "Cancelled", and so this case will be handled by handleStatusChange
                                    break;
                                case 'N':
                                    log.debug('Negative cancel response received')
                                    Status previousState = reshareApplicationEventHandlerService.lookupStatus('PatronRequest', pr.previousStates[pr.state.code]);
                                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, previousState, 'Supplier denied cancellation.', null);
                                    pr.previousStates[pr.state.code] = null;
                                    pr.state = previousState
                                    break;
                                default:
                                    log.error("handleSupplyingAgencyMessage does not know how to deal with a CancelResponse answerYesNo of ${eventData.messageInfo.answerYesNo}")
                            }
                            break;
                        case 'StatusChange':
                            break;
                        case 'Notification':
                            // If this note starts with #ReShareAddLoanCondition# then we know that we have to add another loan condition to the request -- might just work automatically.
                            reshareApplicationEventHandlerService.auditEntry(pr, pr.state, pr.state, "Notification message received from supplying agency: ${eventData.messageInfo.note}", null);
                            break;
                        default:
                            result.status = STATUS_ERROR;
                            result.errorType = 'UnsupportedReasonForMessageType';
                            result.errorValue = eventData.messageInfo.reasonForMessage;
                            throw new Exception("Unhandled reasonForMessage: ${eventData.messageInfo.reasonForMessage}");
                    }
                } else {
                    result.status = STATUS_ERROR;
                    result.errorType = ERROR_TYPE_BADLY_FORMED_MESSAGE;
                    throw new Exception('No reason for message');
                }

                if (eventData.statusInfo?.dueDate) {
                    pr.dueDateRS = eventData.statusInfo.dueDate;
                    try {
                        pr.parsedDueDateRS = reshareActionService.parseDateString(pr.dueDateRS);
                    } catch (Exception e) {
                        log.warn("Unable to parse ${pr.dueDateRS} to date: ${e.getMessage()}");
                    }
                } else {
                    log.debug('No duedate found in eventData.statusInfo');
                }

                if (incomingStatus != null) {
                    handleStatusChange(pr, incomingStatus);
                }

                // Adding an audit entry so we can see what states we are going to for the event
                // Do not commit this uncommented, here to aid seeing what transition changes we allow
//                reshareApplicationEventHandlerService.auditEntry(pr, currentState, pr.state, 'Incomnig message: ' + messageType + ', State change: ' + currentState.code + ' -> '  + pr.state.code, null);

                pr.save(flush:true, failOnError:true);
            }

            log.debug('LOCKING: handleSupplyingAgencyMessage transaction has completed');
        } catch (Exception e) {
            log.error("Problem processing SupplyingAgencyMessage: ${e.message}", e);
        }

        if (result.status != STATUS_ERROR) {
            result.status = STATUS_OK;
        }

        result.messageType = 'SUPPLYING_AGENCY_MESSAGE';
        result.supIdType = eventData.header.supplyingAgencyId.agencyIdType;
        result.supId = eventData.header.supplyingAgencyId.agencyIdValue;
        result.reqAgencyIdType = eventData.header.requestingAgencyId.agencyIdType;
        result.reqAgencyId = eventData.header.requestingAgencyId.agencyIdValue;
        result.reqId = eventData.header.requestingAgencyRequestId;
        result.timeRec = eventData.header.timestamp;
        result.reasonForMessage = eventData.messageInfo.reasonForMessage;

        // I didn't go through changing everywhere result was mentioned to eventResultDetails.responseResult
        eventResultDetails.responseResult = result;
        return(eventResultDetails);
    }

    private PatronRequest lookupPatronRequestWithRole(String id, boolean isRequester, boolean withLock=false) {
        log.debug("LOCKING ReshareApplicationEventHandlerService::lookupPatronRequestWithRole(${id},${isRequester},${withLock})");
        PatronRequest result = PatronRequest.createCriteria().get {
            and {
                or {
                    eq('id', id)
                    eq('hrid', id)
                }
                eq('isRequester', isRequester)
            }
            lock withLock
        }

        log.debug("LOCKING ReshareApplicationEventHandlerService::lookupPatronRequestWithRole located ${result?.id}/${result?.hrid}");

        return result;
    }

    // ISO18626 states are RequestReceived ExpectToSupply WillSupply Loaned Overdue Recalled RetryPossible Unfilled CopyCompleted LoanCompleted CompletedWithoutReturn Cancelled
    private void handleStatusChange(PatronRequest pr, Map statusInfo) {
        log.debug("handleStatusChange(${pr.id},${statusInfo})");

        // Get the rota entry for the current peer
        PatronRequestRota prr = pr.rota.find({ rotaEntry -> rotaEntry.rotaPosition == pr.rotaPosition });

        if (statusInfo.status) {
            switch (statusInfo.status) {
                case 'ExpectToSupply':
                    Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY);
                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, 'ExpectToSupply Protocol message', null);
                    pr.state = newState;
                    if (prr != null) {
                        prr.state = newState;
                    }
                    break;
                case 'Unfilled':
                    Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNFILLED);
                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, 'Unfilled Protocol message', null);
                    pr.state = newState;
                    if (prr != null) {
                        prr.state = newState;
                    }
                    break;
                case 'Conditional':
                    log.debug('Moving to state REQ_CONDITIONAL_ANSWER_RECEIVED')
                    Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED);
                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, 'Conditional Protocol message', null);
                    pr.state = newState;
                    if (prr != null) {
                        prr.state = newState;
                    }
                    break;
                case 'Loaned':
                    Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED);
                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, 'Loaned Protocol message', null);
                    pr.state = newState;
                    if (prr != null) {
                        prr.state = newState;
                    }
                    break;
                case 'Overdue':
                    Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE);
                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, 'Overdue Protocol message', null);
                    pr.state = newState;
                    if (prr != null) {
                        prr.state = newState;
                    }
                    break;
                case 'Recalled':
                    Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_RECALLED);
                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, 'Recalled Protocol message', null);
                    pr.state = newState;
                    if (prr != null) {
                        prr.state = newState;
                    }
                    break;
                case 'Cancelled':
                    Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER);
                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, 'Protocol message', null);
                    pr.state = newState;
                    if (prr != null) {
                        prr.state = newState;
                    }
                    break;
                case 'LoanCompleted':
                    Status newState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_COMPLETE);
                    reshareApplicationEventHandlerService.auditEntry(pr, pr.state, newState, 'LoanCompleted Protocol message', null);
                    pr.state = newState;
                    if (prr != null) {
                        prr.state = newState;
                    }
                    break;
                default:
                    log.error("Unhandled statusInfo.status ${statusInfo.status}");
                    break;
            }
        }
    }
}
