package org.olf.rs.statemodel.actions;

import org.olf.rs.Counter;
import org.olf.rs.DirectoryEntryService;
import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestAudit;
import org.olf.rs.RequestVolume;
import org.olf.rs.SettingsService;
import org.olf.rs.constants.Directory;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

import com.k_int.web.toolkit.custprops.CustomProperty;
import com.k_int.web.toolkit.refdata.RefdataValue;
import com.k_int.web.toolkit.settings.AppSetting;

/**
 * Action that occurs when the responder checjs the item into reshare from the LMS
 * @author Chas
 *
 */
public class ActionResponderSupplierCheckInToReshareService extends AbstractAction {

    private static final String VOLUME_STATUS_AWAITING_LMS_CHECK_OUT = 'awaiting_lms_check_out';

    private static final String REASON_SPOOFED = 'spoofed';

    ActionResponderSupplierCheckOutOfReshareService actionResponderSupplierCheckOutOfReshareService;
    HostLMSService hostLMSService;
    DirectoryEntryService directoryEntryService;
    SettingsService settingsService;

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        boolean result = false;

        if (parameters?.itemBarcodes.size() != 0) {
            // TODO For now we still use this, so just set to first item in array for now. Should be removed though
            request.selectedItemBarcode = parameters?.itemBarcodes[0]?.itemId;

            // We now want to update the patron request's "volumes" field to reflect the incoming params
            // In order to then use the updated list later, we mimic those actions on a dummy list,
            parameters?.itemBarcodes.each { ib ->
                RequestVolume rv = request.volumes.find { rv -> rv.itemId == ib.itemId };

                // If there's no rv and the delete is true then just skip creation
                if (!rv && !ib._delete) {
                    rv = new RequestVolume(
                        name: ib.name ?: request.volume ?: ib.itemId,
                        itemId: ib.itemId,
                        status: RequestVolume.lookupStatus(VOLUME_STATUS_AWAITING_LMS_CHECK_OUT)
                    );
                    request.addToVolumes(rv);
                }

                if (rv) {
                    if (ib._delete && rv.status.value == VOLUME_STATUS_AWAITING_LMS_CHECK_OUT) {
                        // Remove if deleted by incoming call and NCIP call hasn't succeeded yet
                        request.removeFromVolumes(rv);
                    } else if (ib.name && rv.name != ib.name) {
                        // Allow changing of label up to shipping
                        rv.name = ib.name;
                    }
                }

                // Why do we save at this point ?
                request.save(failOnError: true)
            }

            // At this point we should have an accurate list of the calls that need to run/have succeeded
            RequestVolume[] volumesNotCheckedIn = request.volumes.findAll { rv ->
                rv.status.value == VOLUME_STATUS_AWAITING_LMS_CHECK_OUT
            }

            if (volumesNotCheckedIn.size() > 0) {
                // Call the host lms to check the item out of the host system and in to reshare

                /*
                * The supplier shouldn't be attempting to check out of their host LMS with the requester's side patronID.
                * Instead use institutionalPatronID saved on DirEnt or default from settings.
                */

                /*
                * This takes the resolvedRequester symbol, then looks at its owner, which is a DirectoryEntry
                * We then feed that into extractCustomPropertyFromDirectoryEntry to get a CustomProperty.
                * Finally we can extract the value from that custprop.
                * Here that value is a string, but in the refdata case we'd need value?.value
                */
                CustomProperty institutionalPatronId = directoryEntryService.extractCustomPropertyFromDirectoryEntry(request.resolvedRequester?.owner, Directory.KEY_LOCAL_INSTITUTION_PATRON_ID);
                String institutionalPatronIdValue = institutionalPatronId?.value
                if (!institutionalPatronIdValue) {
                    // If nothing on the Directory Entry then fallback to the default in settings
                    AppSetting defaultInstitutionalPatronId = AppSetting.findByKey('default_institutional_patron_id')
                    institutionalPatronIdValue = defaultInstitutionalPatronId?.value
                }

                // At this point we have a list of NCIP calls to make.
                // We should make those calls and track which succeeded/failed

                // Store a string and a Date to save onto the request at the end
                Date parsedDate
                String stringDate

                // Iterate over volumes not yet checked in in for loop so we can break out if we need to
                for (def vol : volumesNotCheckedIn) {
                    /*
                     * Be aware that institutionalPatronIdValue here may well be blank or null.
                     * In the case that host_lms == ManualHostLMSService we don't care, we're just spoofing a positive result,
                     * so we delegate responsibility for checking this to the hostLMSService itself, with errors arising in the 'problems' block
                     */
                    Map checkoutResult = hostLMSService.checkoutItem(request, vol.itemId, institutionalPatronIdValue);

                    // Otherwise, if the checkout succeeded or failed, set appropriately
                    if (checkoutResult.result == true) {
                        RefdataValue volStatus = checkoutResult.reason == REASON_SPOOFED ? vol.lookupStatus('lms_check_out_(no_integration)') : vol.lookupStatus('lms_check_out_complete');
                        if (volStatus) {
                            vol.status = volStatus;
                        }
                        vol.save(failOnError: true);
                        reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, "Check in to ReShare completed for itemId: ${vol.itemId}. ${checkoutResult.reason == REASON_SPOOFED ? '(No host LMS integration configured for check out item call)' : 'Host LMS integration: CheckoutItem call succeeded.'}", null);

                        // Attempt to store any dueDate coming in from LMS iff it is earlier than what we have stored
                        String dateFormatSetting = settingsService.getSettingValue(SettingsData.SETTING_NCIP_DUE_DATE_FORMAT);
                        try {
                            Date tempParsedDate = reshareActionService.parseDateString(checkoutResult?.dueDate, dateFormatSetting);
                            if (!request.parsedDueDateFromLMS || parsedDate.before(request.parsedDueDateFromLMS)) {
                                parsedDate = tempParsedDate;
                                stringDate = checkoutResult?.dueDate;
                            }
                        } catch (Exception e) {
                            log.warn("Unable to parse ${checkoutResult?.dueDate} to date with format string ${dateFormatSetting}: ${e.getMessage()}");
                        }
                    } else {
                        reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, "Host LMS integration: NCIP CheckoutItem call failed for itemId: ${vol.itemId}. Review configuration and try again or deconfigure host LMS integration in settings. " + checkoutResult.problems?.toString(), null);
                    }
                }

                // Save the earliest Date we found as the dueDate
                request.dueDateFromLMS = stringDate;
                request.parsedDueDateFromLMS = parsedDate;
                request.save(flush:true, failOnError:true);

                // At this point we should have all volumes checked out. Check that again
                volumesNotCheckedIn = request.volumes.findAll { rv ->
                    rv.status.value == VOLUME_STATUS_AWAITING_LMS_CHECK_OUT;
                }

                if (volumesNotCheckedIn.size() == 0) {
                    statisticsService.incrementCounter(Counter.COUNTER_ACTIVE_LOANS);
                    request.activeLoan = true;
                    request.needsAttention = false;
                    if (!settingsService.hasSettingValue(SettingsData.SETTING_NCIP_USE_DUE_DATE, 'off')) {
                        request.dueDateRS = request.dueDateFromLMS;
                    }

                    try {
                        request.parsedDueDateRS = reshareActionService.parseDateString(request.dueDateRS);
                    } catch (Exception e) {
                        log.warn("Unable to parse ${request.dueDateRS} to date: ${e.getMessage()}");
                    }

                    request.overdue = false;
                    actionResultDetails.auditMessage = 'Items successfully checked in to ReShare';
                    result = true;
                } else {
                    actionResultDetails.auditMessage = 'One or more items failed to be checked into ReShare. Review configuration and try again or deconfigure host LMS integration in settings.';
                    request.needsAttention = true;
                }
            } else {
                // If we have deleted all failing requests, we can move to next state
                actionResultDetails.auditMessage = 'Fill request completed.';

                // Result is successful
                result = true
                log.info('No item ids remain not checked into ReShare, return true');
            }
        }

        if (result == false) {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            actionResultDetails.responseResult.code = -3; // NCIP action failed

            // Ensure we have a message
            if (actionResultDetails.responseResult.message == null) {
                actionResultDetails.responseResult.message = 'NCIP CheckoutItem call failed.';
            }
        }

        return(actionResultDetails);
    }

    @Override
    ActionResultDetails undo(PatronRequest request, PatronRequestAudit audit, ActionResultDetails actionResultDetails) {

        // Call the checkout of reshare action
        actionResultDetails = actionResponderSupplierCheckOutOfReshareService.performAction(request, [ undo : true ], actionResultDetails);

        // If we were successful, remove the volumes from the request
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            request.volumes.collect().each { volume ->
                request.removeFromVolumes(volume);
            }
        }

        // Remove any LMS due date
        request.dueDateFromLMS = null;
        request.parsedDueDateFromLMS = null;

        // Remove the RS due date too if that was likely set from the LMS one,
        if (!settingsService.hasSettingValue(SettingsData.SETTING_NCIP_USE_DUE_DATE, 'off')) {
            request.dueDateRS = null;
            request.parsedDueDateRS = null;
        }

        // Let the caller know the result
        return(actionResultDetails);
    }
}
