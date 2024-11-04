package org.olf.rs.statemodel.actions

import com.k_int.web.toolkit.custprops.CustomProperty;
import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.rs.constants.Directory;
import org.olf.rs.DirectoryEntryService;
import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.SettingsService;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Responder is replying to a cancel request from the requester
 * @author Chas
 *
 */
public class ActionResponderSupplierRespondToCancelService extends ActionResponderService {

    DirectoryEntryService directoryEntryService;
    HostLMSService hostLMSService;
    SettingsService settingsService;

    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip";
    private static final String SETTING_INSTITUTIONAL_ID = 'default_institutional_patron_id';

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // If the cancellation is denied, switch the cancel flag back to false,
        // otherwise check in and send request to complete
        if (parameters?.cancelResponse == 'no') {
            reshareActionService.sendSupplierCancelResponse(request, parameters, actionResultDetails);
            actionResultDetails.auditMessage = 'Cancellation denied';
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO;
        } else {
            // Check volumes back in
            Map checkInResult = [:];
            try {
                checkInResult = hostLMSService.checkInRequestVolumes(request);
            }
            catch (Exception e) {
                log.error('NCIP problem sending CheckinItem', e);
                request.needsAttention = true;
                reshareApplicationEventHandlerService.auditEntry(
                    request,
                    request.state,
                    request.state,
                    "Host LMS integration: NCIP CheckinItem call failed for volumes in request: ${request.id} when responding to cancel. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message,
                    null);
                checkInResult.result = false;
            }

            // Are we using request item? If so, we need to instruct the host lms to send a cancel request item if necessary
            log.debug("Checking to see if we need to send a CancelRequestItem");
            Map cancelRequestItemResult = [:];
            if (settingsService.hasSettingValue(SettingsData.SETTING_USE_REQUEST_ITEM, SETTING_REQUEST_ITEM_NCIP)) {
                if (hostLMSService.isManualCancelRequestItem()) {
                    log.debug("Resolved requester ${request.resolvedRequester?.owner?.name}")
                    CustomProperty institutionalPatronId = directoryEntryService.extractCustomPropertyFromDirectoryEntry(
                            request.resolvedRequesterDirectoryEntry, Directory.KEY_LOCAL_INSTITUTION_PATRON_ID);
                    String institutionalPatronIdValue = institutionalPatronId?.value;
                    if (!institutionalPatronIdValue) {
                        // If nothing on the Directory Entry then fallback to the default in settings
                        AppSetting defaultInstitutionalPatronId = AppSetting.findByKey(SETTING_INSTITUTIONAL_ID);
                        institutionalPatronIdValue = defaultInstitutionalPatronId?.value;
                    }
                    log.debug("Sending CancelRequestItem");
                    try {
                        cancelRequestItemResult = hostLMSService.cancelRequestItem(request, request.externalHoldRequestId, institutionalPatronIdValue);
                    }
                    catch (Exception e) {
                        log.error('NCIP problem sending CancelRequestItem', e);
                        request.needsAttention = true;
                        reshareApplicationEventHandlerService.auditEntry(
                            request,
                            request.state,
                            request.state,
                            "Host LMS integration: NCIP CancelRequestItem call failed for request: ${request.id} when responding to cancel. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message,
                            null);
                        cancelRequestItemResult.result = false;
                    }
                    log.debug("Result of CancelRequestItem is ${cancelRequestItemResult}");
                }
            }

            if (checkInResult.result == false || cancelRequestItemResult.result == false) {
                actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
                if (checkInResult.result == false) {
                  actionResultDetails.auditMessage = 'NCIP CheckinItem call failed when responding to cancel';
                } else {
                  actionResultDetails.auditMessage = 'NCIP CancelRequestItem call failed when responding to cancel';
                }
                actionResultDetails.responseResult.code = -3; // NCIP action failed
                actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
                actionResultDetails.responseResult.status = false;
            } else {
                log.debug("Responder accepted cancellation");
                reshareActionService.sendSupplierCancelResponse(request, parameters, actionResultDetails);
                actionResultDetails.auditMessage = 'Cancellation accepted';
            }

        }

        return(actionResultDetails);
    }
}
