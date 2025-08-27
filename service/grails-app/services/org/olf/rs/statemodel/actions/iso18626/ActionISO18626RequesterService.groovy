package org.olf.rs.statemodel.actions.iso18626

import org.olf.rs.DirectoryEntryService
import org.olf.rs.ReferenceDataService
import org.olf.rs.RerequestService
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.statemodel.StateModel
import org.olf.rs.statemodel.events.EventMessageRequestIndService;

import java.util.regex.Matcher;

import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.PatronRequest;
import org.olf.rs.RequestVolume;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StatusService;
import org.olf.rs.referenceData.SettingsData;

/**
 * Action that deals with interpreting ISO18626 on the requester side
 * @author Chas
 *
 */
public abstract class ActionISO18626RequesterService extends ActionISO18626Service {

    private static final String VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION = 'awaiting_temporary_item_creation';
    private static final String SETTING_YES = 'yes';

    ReferenceDataService referenceDataService;
    RerequestService rerequestService;
    SettingsService settingsService;
    StatusService statusService;

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        log.debug("ISO18626RequesterService with request ${request} in status ${request?.state.code} and parameters ${parameters}");
        // Grab hold of the statusInfo as we may want to override it
        Map incomingStatus = parameters.statusInfo;

        // Extract the sequence from the note
        Map sequenceResult = protocolMessageBuildingService.extractSequenceFromNote(parameters.messageInfo?.note);
        String note = sequenceResult.note;
        request.lastSequenceReceived = sequenceResult.sequence;

        // if parameters.deliveryInfo.itemId then we should stash the item id
        if (parameters?.deliveryInfo) {
            if (parameters?.deliveryInfo?.loanCondition) {
                // Are we in a valid state for loan conditions ?
                log.debug("Loan condition found: ${parameters?.deliveryInfo?.loanCondition}")

                // Don't override status to Conditional for supplied items
                boolean isSupplied = incomingStatus?.status in ['Loaned', 'CopyCompleted'];

                if (!isSupplied) {
                    incomingStatus = [status: 'Conditional']
                }

                // Save the loan condition to the patron request
                String loanCondition = parameters?.deliveryInfo?.loanCondition;
                Symbol relevantSupplier = DirectoryEntryService.resolveSymbol(parameters.header.supplyingAgencyId.agencyIdType, parameters.header.supplyingAgencyId.agencyIdValue);

                // Conditions on supplied items are presumed to be accepted but not explicitly so ergo null
                Boolean accepted = isSupplied ? null : false;
                reshareApplicationEventHandlerService.addLoanConditionToRequest(request, loanCondition, relevantSupplier, note, parameters?.messageInfo?.offeredCosts?.monetaryValue, parameters?.messageInfo?.offeredCosts?.currencyCode, accepted);

                if (isSupplied && parameters?.messageInfo?.offeredCosts?.monetaryValue && parameters?.messageInfo?.offeredCosts?.currencyCode) {
                    request.cost = new BigDecimal(parameters.messageInfo.offeredCosts.monetaryValue);
                    request.costCurrency = referenceDataService.lookup(RefdataValueData.VOCABULARY_CURRENCY_CODES, parameters.messageInfo.offeredCosts.currencyCode);
                }
            }

            // Could receive a single string or an array here as per the standard/our profile
            Object itemId = parameters?.deliveryInfo?.itemId
            if (itemId && parameters?.deliveryInfo?.sentVia != 'URL') {
                def useBarcodeSetting = AppSetting.findByKey(SettingsData.SETTING_NCIP_USE_BARCODE);
                String useBarcodeValue = useBarcodeSetting?.value ?: "No";
                log.debug("Value for setting ${SettingsData.SETTING_NCIP_USE_BARCODE} is ${useBarcodeValue}");
                Boolean useBarcode = "Yes".equalsIgnoreCase(useBarcodeValue);
                if (itemId instanceof Collection || (itemId instanceof  String && ((String)itemId).contains("multivol"))) {
                    if (itemId instanceof  String && ((String)itemId).contains("multivol")) {
                        String[] transformToList = ((String)itemId).split(",multivol:")
                        transformToList = transformToList.collect { str ->
                            if (!str.startsWith("multivol:")) {
                                "multivol:" + str
                            } else {
                                str
                            }
                        }
                        itemId = transformToList
                    }
                    if (!request.selectedItemBarcode && itemId) {
                        request.selectedItemBarcode = itemId
                    }
                    // Item ids coming in, handle those
                    itemId.each { iid ->
                        Matcher matcher = iid =~ /multivol:(.*),(.*),(.*)/
                        if (matcher.size() > 0) {
                            // At this point we have an itemId of the form "multivol:<name>,<callNumber>,<id>"
                            String iidId = matcher[0][3]
                            String iidName = matcher[0][1]
                            String iidCallNumber = matcher[0][2]

                            // Check if a RequestVolume exists for this itemId, and if not, create one
                            RequestVolume rv = request.volumes.find { rv -> rv.itemId == iidId };
                            if (!rv) {
                                rv = new RequestVolume(
                                        name: iidName ?: request.volume ?: iidId,
                                        itemId: iidId,
                                        status: RequestVolume.lookupStatus(VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION)
                                )
                                rv.callNumber = iidCallNumber

                                request.addToVolumes(rv)

                                /*
                                    This _should_ be handled on the following save,
                                    but there seems to not be an intial save which
                                    adds the temporary barcode necessary for acceptItem.
                                    Since these are added sequentially, in known multivol cases
                                    we can enforce the multivolume rule so that the first item
                                    does not rely on `volumes.size() > 1`
                                */
                                rv.temporaryItemBarcode = rv.generateTemporaryItemBarcode(true, useBarcode)
                            }
                        }
                    }
                } else {
                    // We have a single string, this is the usual standard case and should be handled as a single request volume
                    // Check if a RequestVolume exists for this itemId, and if not, create one
                    // At this point we have an itemId of the form "<name>,<id>,<callNumber>"
                    Matcher matcher = itemId =~ /(.*),(.*),(.*)/
                    String iidId = itemId
                    String iidName = itemId
                    String iidCallNumber = null
                    if (matcher.size() > 0) {
                        iidId = matcher[0][3]
                        iidName = matcher[0][1]
                        iidCallNumber = matcher[0][2]
                    }

                    if (!request.selectedItemBarcode && iidId) {
                        request.selectedItemBarcode = iidId
                    }

                    RequestVolume rv = request.volumes.find { rv -> rv.itemId == iidId }
                    if (!rv) {
                        rv = new RequestVolume(
                                name: request.volume ?: iidName,
                                itemId: iidId,
                                status: RequestVolume.lookupStatus(VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION)
                        )
                        rv.callNumber = iidCallNumber

                        request.addToVolumes(rv)

                        /*
                            This _should_ be handled on the following save,
                            but there seems to not be an initial save which
                            adds the temporary barcode necessary for acceptItem.
                        */
                        rv.temporaryItemBarcode = rv.generateTemporaryItemBarcode(false, useBarcode)
                    }
                }
            }

            // If the deliveredFormat is URL and a URL is present, store it on the request
            if (parameters.deliveryInfo?.sentVia == 'URL') {
                def url = parameters.deliveryInfo?.itemId
                if (url) {
                    request.pickupURL = url
                }
            }
        }

        // If there is a note, create notification entry
        if (note) {
            reshareApplicationEventHandlerService.incomingNotificationEntry(request, parameters, true, note);
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
            handleStatusChange(request, incomingStatus, parameters, actionResultDetails);
        }

        return(actionResultDetails);
    }

    // ISO18626 states are RequestReceived, ExpectToSupply, WillSupply, Loaned Overdue, Recalled, RetryPossible, Unfilled, CopyCompleted, LoanCompleted, CompletedWithoutReturn and Cancelled
    private void handleStatusChange(PatronRequest request, Map statusInfo, Object parameters, ActionResultDetails actionResultDetails) {
        log.debug("handleStatusChange(${request.id},${statusInfo})");

        if (statusInfo.status) {
            // Set the qualifier on the result
            actionResultDetails.qualifier = statusInfo.status;

            // Special case for Unfilled
            if (request.stateModel.shortcode.equalsIgnoreCase(StateModel.MODEL_REQUESTER) ||
                    request.stateModel.shortcode.equalsIgnoreCase(StateModel.MODEL_NR_REQUESTER)) {
                if (statusInfo.status == "Unfilled") {
                    log.debug("Handling Unfilled status");
                    if (parameters.messageInfo.reasonUnfilled == "transfer") {
                        log.debug("Unfilled result with reason 'transfer'");
                        String pattern = /transferToCluster:(.+?)(#seq:.+#)?/
                        String note = parameters.messageInfo.note;
                        if (note) {
                            def matcher = note =~ pattern;
                            if (matcher.matches()) {
                                String newCluster = matcher.group(1);
                                log.debug("Pattern match for transferring request to new cluster ${newCluster}");
                                if (settingsService.hasSettingValue(SettingsData.SETTING_AUTO_REREQUEST, SETTING_YES)) {
                                    //Trigger Re-Request here
                                    actionResultDetails.qualifier = "UnfilledTransfer"; //To transition to Rerequested state
                                    PatronRequest newRequest = rerequestService.createNewRequestFromExisting(request, RerequestService.preserveFields, ["systemInstanceIdentifier": newCluster], true);
                                }
                            } else {
                                log.debug("reasonUnfilled was 'transfer', but a valid cluster id was not found in note: ${note}");
                            }
                        }
                    }
                }
            }
        }
    }
}
