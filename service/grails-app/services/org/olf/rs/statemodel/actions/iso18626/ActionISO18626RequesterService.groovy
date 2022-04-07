package org.olf.rs.statemodel.actions.iso18626;

import java.util.regex.Matcher;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.RequestVolume;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Action that deals with interpreting ISO18626 on the requester side
 * @author Chas
 *
 */
public abstract class ActionISO18626RequesterService extends AbstractAction {

    private static final String VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION = 'awaiting_temporary_item_creation';

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Grab hold of the statusInfo as we may want to override it
        Map incomingStatus = parameters.statusInfo;

        // if parameters.deliveryInfo.itemId then we should stash the item id
        if (parameters?.deliveryInfo) {
            if (parameters?.deliveryInfo?.loanCondition) {
                // Are we in a valid state for loan conditions ?
                log.debug("Loan condition found: ${parameters?.deliveryInfo?.loanCondition}")
                incomingStatus = [status: 'Conditional']

                // Save the loan condition to the patron request
                String loanCondition = parameters?.deliveryInfo?.loanCondition;
                Symbol relevantSupplier = reshareApplicationEventHandlerService.resolveSymbol(parameters.header.supplyingAgencyId.agencyIdType, parameters.header.supplyingAgencyId.agencyIdValue);
                String note = parameters.messageInfo?.note;

                reshareApplicationEventHandlerService.addLoanConditionToRequest(request, loanCondition, relevantSupplier, note);
            }

            // If we're being told about the barcode of the selected item (and we don't already have one saved), stash it in selectedItemBarcode on the requester side
            if (!request.selectedItemBarcode && parameters.deliveryInfo.itemId) {
                request.selectedItemBarcode = parameters.deliveryInfo.itemId;
            }

            // Could receive a single string or an array here as per the standard/our profile
            Object itemId = parameters?.deliveryInfo?.itemId;
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
                            RequestVolume rv = request.volumes.find { rv -> rv.itemId == iidId };
                            if (!rv) {
                                rv = new RequestVolume(
                                    name: iidName ?: request.volume ?: iidId,
                                    itemId: iidId,
                                    status: RequestVolume.lookupStatus(VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION)
                                );

                                request.addToVolumes(rv);
                                
                                /*
                                    This _should_ be handled on the following save,
                                    but there seems to not be an intial save which
                                    adds the temporary barcode necessary for acceptItem.
                                    Since these are added sequentially, in known multivol cases
                                    we can enforce the multivolume rule so that the first item
                                    does not rely on `volumes.size() > 1`
                                */
                                rv.temporaryItemBarcode = rv.generateTemporaryItemBarcode(true)
                            }
                        }
                    }
                } else {
                    // We have a single string, this is the usual standard case and should be handled as a single request volume
                    // Check if a RequestVolume exists for this itemId, and if not, create one
                    RequestVolume rv = request.volumes.find { rv -> rv.itemId == itemId };
                    if (!rv) {
                        rv = new RequestVolume(
                            name: request.volume ?: itemId,
                            itemId: itemId,
                            status: RequestVolume.lookupStatus(VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION)
                        );

                        request.addToVolumes(rv);

                        /*
                            This _should_ be handled on the following save,
                            but there seems to not be an intial save which
                            adds the temporary barcode necessary for acceptItem.
                        */
                        rv.temporaryItemBarcode = rv.generateTemporaryItemBarcode()
                    }
                }
            }
        }

        // If there is a note, create notification entry
        if (parameters.messageInfo?.note) {
            reshareApplicationEventHandlerService.incomingNotificationEntry(request, parameters, true);
        }

        // Is there a due date
        if (parameters.statusInfo?.dueDate) {
            request.dueDateRS = parameters.statusInfo.dueDate;
            try {
                request.parsedDueDateRS = reshareActionService.parseDateString(request.dueDateRS);
            } catch (Exception e) {
                log.warn("Unable to parse ${request.dueDateRS} to date: ${e.getMessage()}");
            }
        }

        if (incomingStatus != null) {
            handleStatusChange(request, incomingStatus, actionResultDetails);
        }

        return(actionResultDetails);
    }

    // ISO18626 states are RequestReceived, ExpectToSupply, WillSupply, Loaned Overdue, Recalled, RetryPossible, Unfilled, CopyCompleted, LoanCompleted, CompletedWithoutReturn and Cancelled
    private void handleStatusChange(PatronRequest request, Map statusInfo, ActionResultDetails actionResultDetails) {
        log.debug("handleStatusChange(${request.id},${statusInfo})");

        if (statusInfo.status) {
            switch (statusInfo.status) {
                case 'ExpectToSupply':
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY);
                    break;

                case 'Unfilled':
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNFILLED);
                    break;

                case 'Conditional':
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED);
                    break;

                case 'Loaned':
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED);
                    break;

                case 'Overdue':
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE);
                    break;

                case 'Recalled':
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_RECALLED);
                    break;

                case 'Cancelled':
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER);
                    break;

                case 'LoanCompleted':
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_COMPLETE);
                    break;

                default:
                    log.error("Unhandled statusInfo.status ${statusInfo.status}");
                    break;
            }

            // Get the rota entry for the current peer
            PatronRequestRota prr = request.rota.find({ rotaEntry -> rotaEntry.rotaPosition == request.rotaPosition });
            if (prr != null) {
                prr.state = actionResultDetails.newStatus;
            }
        }
    }
}
