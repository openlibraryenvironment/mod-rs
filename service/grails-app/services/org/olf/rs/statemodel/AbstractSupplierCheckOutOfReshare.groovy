package org.olf.rs.statemodel


import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest
/**
 * Abstract class incorporating common fields and methods for HostLMS actions.
 */
public abstract class AbstractSupplierCheckOutOfReshare extends AbstractAction {

    protected HostLMSService hostLMSService;

    protected ActionResultDetails performCommonAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
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
                    "Host LMS integration: NCIP CheckinItem call failed for volumes in request: ${request.id}. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message,
                    null);
            resultMap.result = false;
        }

        if (resultMap.result == false) {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            actionResultDetails.auditMessage = 'NCIP CheckinItem call failed';
            actionResultDetails.responseResult.code = -3; // NCIP action failed
            actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
            actionResultDetails.responseResult.status = false;
        } else {
            log.debug('supplierCheckOutOfReshare::transition and send status change');
            if (!parameters?.undo) {
                // We are not performing an undo of the checkInToReshare action
                reshareActionService.sendStatusChange(request, 'LoanCompleted', actionResultDetails, parameters?.note);
            }
            actionResultDetails.responseResult.status = true;
        }

        return(actionResultDetails);
    }
}