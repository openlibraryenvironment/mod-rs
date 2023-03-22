package org.olf.rs.statemodel.actions;

import org.olf.rs.Counter;
import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.RequestVolume;
import org.olf.rs.SettingsService;
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

import com.k_int.web.toolkit.refdata.RefdataValue;

/**
 * The requester has received the item
 * @author Chas
 *
 */
public class ActionPatronRequestRequesterReceivedService extends AbstractAction {

    private static final String VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION = 'awaiting_temporary_item_creation';

    private static final String REASON_SPOOFED = 'spoofed';

    HostLMSService hostLMSService;
    SettingsService settingsService;

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_REQUESTER_RECEIVED);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        boolean ncipResult = false;

        // Increment the active borrowing counter
        statisticsService.incrementCounter(Counter.COUNTER_ACTIVE_BORROWING);

        // Check the item in to the local LMS
        HostLMSActions hostLMS = hostLMSService.getHostLMSActions();
        if (hostLMS) {
            RequestVolume[] volumesWithoutTemporaryItem = request.volumes.findAll { rv ->
                rv.status.value == VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION
            }
            // Iterate over volumes without temp item in for loop so we can break out if we need to
            for (RequestVolume vol : volumesWithoutTemporaryItem) {
                try {
                    // Call the host lms to check the item out of the host system and in to reshare
                    Map acceptResult = hostLMS.acceptItem(
                        settingsService,
                        vol.temporaryItemBarcode,
                        request.hrid,
                        request.patronIdentifier, // user_idA
                        request.author, // author,
                        request.title, // title,
                        request.isbn, // isbn,
                        request.localCallNumber, // call_number,
                        request.resolvedPickupLocation?.lmsLocationCode, // pickup_location,
                        null); // requested_action

                    if (acceptResult?.result == true) {
                        // Let the user know if the success came from a real call or a spoofed one
                        String message = "Receive succeeded for item id: ${vol.itemId} (temporaryItemBarcode: ${vol.temporaryItemBarcode}). ${acceptResult.reason == REASON_SPOOFED ? '(No host LMS integration configured for accept item call)' : 'Host LMS integration: AcceptItem call succeeded.'}";
                        RefdataValue newVolState = acceptResult.reason == REASON_SPOOFED ? vol.lookupStatus('temporary_item_creation_(no_integration)') : vol.lookupStatus('temporary_item_created_in_host_lms');

                        reshareApplicationEventHandlerService.auditEntry(request,
                            request.state,
                            request.state,
                            message,
                            null);

                        log.debug("State for volume ${vol.itemId} set to ${newVolState}");
                        vol.status = newVolState;
                        vol.save(failOnError: true);
                    } else {
                        String message = "Host LMS integration: NCIP AcceptItem call failed for temporary item barcode: ${vol.temporaryItemBarcode}. Review configuration and try again or deconfigure host LMS integration in settings. ";
                        // PR-658 wants us to set some state here but doesn't say what that state is. Currently we leave the state as is.
                        // IF THIS NEEDS TO GO INTO ANOTHER STATE, WE SHOULD DO IT AFTER ALL VOLS HAVE BEEN ATTEMPTED
                        reshareApplicationEventHandlerService.auditEntry(request,
                            request.state,
                            request.state,
                            message + acceptResult?.problems,
                            null);
                    }
                } catch (Exception e) {
                    log.error('NCIP Problem', e);
                    reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, "Host LMS integration: NCIP AcceptItem call failed for temporary item barcode: ${vol.temporaryItemBarcode}. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message, null);
                }
            }
            request.save(flush:true, failOnError:true);

            // At this point we should have all volumes' temporary items created. Check that again
            volumesWithoutTemporaryItem = request.volumes.findAll { rv ->
                rv.status.value == VOLUME_STATUS_AWAITING_TEMPORARY_ITEM_CREATION
            }

            if (volumesWithoutTemporaryItem.size() == 0) {
                // Let the user know if the success came from a real call or a spoofed one
                actionResultDetails.auditMessage = 'Host LMS integration: AcceptItem call succeeded for all items.';

                request.needsAttention = false;
                ncipResult = true;
                reshareActionService.sendRequestingAgencyMessage(request, 'Received', parameters, actionResultDetails);
            } else {
                actionResultDetails.auditMessage = 'Host LMS integration: AcceptItem call failed for some items.';
                request.needsAttention = true;
            }
        } else {
            actionResultDetails.auditMessage = 'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.';
            request.needsAttention = true;
        }

        // Take into account if we failed on the ncip message
        if (!ncipResult) {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            if (actionResultDetails.auditMessage != null) {
                actionResultDetails.auditMessage = 'NCIP AcceptItem call failed';
            }
            actionResultDetails.responseResult.code = -3; // NCIP action failed
            actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
        }

        return(actionResultDetails);
    }
}
