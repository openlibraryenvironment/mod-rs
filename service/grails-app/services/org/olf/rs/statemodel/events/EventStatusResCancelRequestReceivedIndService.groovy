package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
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

    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip";


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
            }
        } else {
            // Set needs attention=true
            eventResultDetails.auditMessage = 'Cancellation Request Received';
            request.needsAttention = true;
        }

        return(eventResultDetails);
    }
}
