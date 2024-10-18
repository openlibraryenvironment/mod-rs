package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.custprops.CustomProperty
import org.olf.rs.DirectoryEntryService
import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService
import org.olf.rs.SettingsService
import org.olf.rs.constants.Directory
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StatusStage;

import com.k_int.web.toolkit.settings.AppSetting;

/**
 * Event triggered when a cancel request is received from the requester
 * @author Chas
 *
 */
public class EventStatusResCancelRequestReceivedIndService extends AbstractEvent {

    ReshareActionService reshareActionService;
    SettingsService settingsService;
    HostLMSService hostLMSService;
    DirectoryEntryService directoryEntryService;

    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip";
    private static final String SETTING_INSTITUTIONAL_ID = 'default_institutional_patron_id';


    @Override
    String name() {
        return(Events.EVENT_STATUS_RES_CANCEL_REQUEST_RECEIVED_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        String autoCancel = AppSetting.findByKey('auto_responder_cancel')?.value;
        if (autoCancel?.toLowerCase().startsWith('on')) {
            log.debug('Auto cancel is on');

            // System has auto-respond cancel on
            if (request.state?.stage == StatusStage.ACTIVE_SHIPPED) {
                // Revert the state to it's original before the cancel request was received - previousState
                eventResultDetails.auditMessage = 'AutoResponder:Cancel is ON - but item is SHIPPED. Responding NO to cancel, revert to previous state';
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_SHIPPED;
                reshareActionService.sendSupplierCancelResponse(request, [cancelResponse : 'no'], eventResultDetails);
            } else {
                // Just respond YES
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_CANCELLED;
                eventResultDetails.auditMessage =  'AutoResponder:Cancel is ON - responding YES to cancel request';
                reshareActionService.sendSupplierCancelResponse(request, [cancelResponse : 'yes'], eventResultDetails);

                //Are we using request item? If so, we need to instruct the host lms to send a cancel request item if necessary

                log.debug("Checking to see if we need to send a CancelRequestItem");

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
                        Map cancelRequestItemResult = hostLMSService.cancelRequestItem(request, request.externalHoldRequestId, institutionalPatronIdValue);
                        log.debug("Result of CancelRequestItem is ${cancelRequestItemResult}");
                    }
                }
            }
        } else {
            // Set needs attention=true
            eventResultDetails.auditMessage = 'Cancellation Request Received';
            request.needsAttention = true;
        }

        return(eventResultDetails);
    }
}
