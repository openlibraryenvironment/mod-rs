package org.olf.rs.statemodel.actions;

import org.olf.rs.DirectoryEntryService;
import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Requester has returned the item so we therefore need to check it out of reshare
 * @author Chas
 *
 */
public class ActionResponderSupplierCheckOutOfReshareService extends AbstractAction {

    HostLMSService hostLMSService;
    DirectoryEntryService directoryEntryService;

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
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
            reshareActionService.sendStatusChange(request, 'LoanCompleted', actionResultDetails, parameters?.note);
            actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_COMPLETE);
            actionResultDetails.responseResult.status = true;
        }

        return(actionResultDetails);
    }
}
