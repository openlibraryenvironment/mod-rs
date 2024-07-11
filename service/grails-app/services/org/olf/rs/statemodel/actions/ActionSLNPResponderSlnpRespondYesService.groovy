package org.olf.rs.statemodel.actions

import com.k_int.web.toolkit.settings.AppSetting
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.ActionResult
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.StateModel

/**
 * Performs an answer will supply action for the SLNP responder
 *
 */
public class ActionSLNPResponderSlnpRespondYesService extends ActionResponderService {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_RESPONDER_RESPOND_YES)
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        String shortcode = request.stateModel.shortcode
        String willSupplyMessage = "Will Supply"

        boolean isReturnableResponder = shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_RESPONDER)
        boolean isNonReturnableResponder = shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER)

        if (isReturnableResponder) {
            if (validatePickupLocationAndRoute(request, parameters, actionResultDetails).result == ActionResult.SUCCESS) {
                actionResultDetails.auditMessage = willSupplyMessage
            }
        } else if (isNonReturnableResponder) {
            log.debug('autoRespond....')
            String autoRespondStatus = AppSetting.findByKey('copy_auto_responder_status')?.value
            if (autoRespondStatus == "on:_loaned_and_cannot_supply") {
                reshareActionService.sendResponse(request, ActionEventResultQualifier.QUALIFIER_LOANED, parameters, actionResultDetails)
                actionResultDetails.auditMessage = willSupplyMessage
            }
        } else {
            actionResultDetails.auditMessage = willSupplyMessage
        }

        return actionResultDetails
    }
}
