package org.olf.rs.statemodel.actions

import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest
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

    HostLMSService hostLMSService;

    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip";

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
            Map resultMap = [:];
            try {
                resultMap = hostLMSService.checkInRequestVolumes(request);
            }
            catch (Exception e) {
                log.error('NCIP Problem', e);
                request.needsAttention = true;
                reshareApplicationEventHandlerService.auditEntry(
                    request,
                    request.state,
                    request.state,
                    "Host LMS integration: NCIP CheckinItem call failed for volumes in request: ${request.id} when responding to cancel. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message,
                    null);
                resultMap.result = false;
            }

            if (resultMap.result == false) {
                actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
                actionResultDetails.auditMessage = 'NCIP CheckinItem call failed when responding to cancel';
                actionResultDetails.responseResult.code = -3; // NCIP action failed
                actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
                actionResultDetails.responseResult.status = false;
            } else {
                log.debug("Responder accepted cancellation");
                reshareActionService.sendSupplierCancelResponse(request, parameters, actionResultDetails);
                actionResultDetails.responseResult.status = true;
                actionResultDetails.auditMessage = 'Cancellation accepted';
            }
        }

        return(actionResultDetails);
    }
}
