package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService
import org.olf.rs.iso18626.ReasonForMessage
import org.olf.rs.iso18626.TypeStatus;
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Action that deals with filling the request locally
 * @author Chas
 *
 */
public class ActionPatronRequestFillLocallyService extends AbstractAction {
    SettingsService settingsService

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_FILL_LOCALLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just set the status
        actionResultDetails.responseResult.status = true;
        actionResultDetails.auditMessage = 'Request locally completed'
        String requestRouterSetting = settingsService.getSettingValue('routing_adapter')
        if (requestRouterSetting == 'disabled') {
            String status = request.serviceType?.value == 'copy' ? TypeStatus.COPY_COMPLETED.value() : TypeStatus.LOAN_COMPLETED.value()
            reshareActionService.sendSupplyingAgencyMessage(request, ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE, status, [*: parameters], actionResultDetails)
        }
        return(actionResultDetails);
    }
}
