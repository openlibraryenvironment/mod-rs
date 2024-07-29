package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService;
import org.olf.rs.iso18626.ReasonForMessage
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.StateModel;

/**
 * Action that deals with the ISO18626 StatusChange message
 * @author Chas
 *
 */
public class ActionPatronRequestISO18626StatusChangeService extends ActionISO18626RequesterService {

    SettingsService settingsService

    @Override
    String name() {
        return(ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // We have a hack where we use this  message to verify that the last one sent was actually received or not
        if (!checkForLastSequence(request, parameters.messageInfo?.note, actionResultDetails)) {
            // A normal message
            // Call the base class first
            actionResultDetails = super.performAction(request, parameters, actionResultDetails);

            // Only continue if successful
            if (actionResultDetails.result == ActionResult.SUCCESS) {
                String auditMessage = "Status Change message received"

                // Add an audit entry
                actionResultDetails.auditMessage = auditMessage
                if (actionResultDetails.qualifier.equalsIgnoreCase(ActionEventResultQualifier.QUALIFIER_CANCELLED) &&
                        'ABORT'.equalsIgnoreCase(parameters.messageInfo?.note)) {
                    actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_ABORTED
                }

                // For SLNP non-returnables we need to call AcceptItem and also set the Qualifier depending on the active client
                // BVB -> SLNP_REQ_DOCUMENT_SUPPLIED, BSZ -> SLNP_REQ_DOCUMENT_AVAILABLE
                if (request.stateModel.shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER)) {
                    performCommonAction(request, parameters, actionResultDetails, auditMessage)

                    String slnpNonRetActiveClientSettingValue = settingsService.getSettingValue(SettingsData.SETTING_SLNP_NON_RETURNABLE_ACTIVE_CLIENT)

                    if (slnpNonRetActiveClientSettingValue != null) {
                        if (slnpNonRetActiveClientSettingValue.equalsIgnoreCase(SettingsData.SETTING_VALUE_SLNP_NON_RETURNABLE_CLIENT_BVB)) {
                            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_DOCUMENT_SUPPLIED
                        } else if (slnpNonRetActiveClientSettingValue.equalsIgnoreCase(SettingsData.SETTING_VALUE_SLNP_NON_RETURNABLE_CLIENT_BSZ)) {
                            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_DOCUMENT_AVAILABLE
                        }
                    }
                }
            }
        }

        // Now return the results to the caller
        return(actionResultDetails);
    }
}
