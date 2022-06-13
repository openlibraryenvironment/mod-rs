package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StatusStage;

import com.k_int.web.toolkit.settings.AppSetting;

/**
 * Event triggered when a cancel request is received from the requester
 * @author Chas
 *
 */
public class EventStatusResCancelRequestReceivedIndService extends AbstractEvent {

    private static final String[] FROM_STATES = [
    ];

    private static final String[] TO_STATES = [
    ];

    ReshareActionService reshareActionService;

    @Override
    String name() {
        return(Events.EVENT_STATUS_RES_CANCEL_REQUEST_RECEIVED_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
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
        // This event
        return(model == StateModel.MODEL_RESPONDER);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        String autoCancel = AppSetting.findByKey('auto_responder_cancel')?.value;
        if (autoCancel?.toLowerCase().startsWith('on')) {
            log.debug('Auto cancel is on');

            // System has auto-respond cancel on
            if (request.state?.stage == StatusStage.ACTIVE) {
                // Revert the state to it's original before the cancel request was received - previousState
                eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus('Responder', request.previousStates[Status.RESPONDER_CANCEL_REQUEST_RECEIVED]);
                eventResultDetails.auditMessage = 'AutoResponder:Cancel is ON - but item is SHIPPED. Responding NO to cancel, revert to previous state';
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_SHIPPED;
                reshareActionService.sendSupplierCancelResponse(request, [cancelResponse : 'no']);
            } else {
                // Just respond YES
                eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCELLED);
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_CANCELLED;
                eventResultDetails.auditMessage =  'AutoResponder:Cancel is ON - responding YES to cancel request';
                reshareActionService.sendSupplierCancelResponse(request, [cancelResponse : 'yes']);
            }
        } else {
            // Set needs attention=true
            eventResultDetails.auditMessage = 'Cancellation Request Recieved';
            request.needsAttention = true;
        }

        return(eventResultDetails);
    }
}
