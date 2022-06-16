package org.olf.rs.statemodel.actions;

import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.settings.AppSetting;

/**
 * Action that performs the returned item action for the requester
 * @author Chas
 *
 */
public class ActionPatronRequestPatronReturnedItemService extends AbstractAction {

    HostLMSService hostLMSService;

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just set the status
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING);
        actionResultDetails.responseResult.status = true;

        AppSetting checkInOnReturn = AppSetting.findByKey('check_in_on_return');

        if (checkInOnReturn?.value != 'off') {
            log.debug("Attempting NCIP CheckInItem after setting item returned for volumes for request {$request?.id}");
            Map resultMap = [:];
            try {
                resultMap = hostLMSService.checkInRequestVolumes(request);
            } catch (Exception e) {
                log.error("Error attempting NCIP CheckinItem for request {$request.id}: {$e}");
                resultMap.result = false;
            }
            if (resultMap.result) {
                log.debug("Successfully checked in volumes for request {$request.id}");
            } else {
                log.debug("Failed to check in volumes for request {$request.id}");
            }
        } else {
            log.debug("NOT Attempting NCIP CheckInItem after setting item returned for volumes for request {$request?.id}");
        }

        return(actionResultDetails);
    }
}
