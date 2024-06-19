package org.olf.rs.statemodel.actions

import com.k_int.web.toolkit.settings.AppSetting
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.StateModel

/**
 * SLNP StateModel should check auto loan setting before sending response to requester.
 * Otherwise send 'Loaned' message to requester.
 * @author EdiTim
 *
 */
public class ActionResponderSupplierMarkShippedService extends ActionResponderService {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED)
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        boolean shouldSendResponse = true

        if (request.stateModel.shortcode == StateModel.MODEL_SLNP_RESPONDER) {
            String autoLoanSetting = AppSetting.findByKey('auto_responder_status')?.value
            if (autoLoanSetting != null && autoLoanSetting.equalsIgnoreCase("on:_loaned_and_cannot_supply")) {
                shouldSendResponse = false
            }
        }

        if (shouldSendResponse) {
            reshareActionService.sendResponse(request, ActionEventResultQualifier.QUALIFIER_LOANED, parameters, actionResultDetails)
        }

        actionResultDetails.auditMessage = 'Shipped'
        return actionResultDetails
    }
}
