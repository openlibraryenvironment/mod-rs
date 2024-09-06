package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest
import org.olf.rs.RerequestService
import org.olf.rs.SettingsService
import org.olf.rs.iso18626.ReasonForMessage
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.ActionResult
import org.olf.rs.statemodel.ActionResultDetails
import com.k_int.web.toolkit.settings.AppSetting
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.StateModel

/**
 * Action that deals with the ISO18626 StatusChange message
 * @author Chas
 *
 */
public class ActionPatronRequestISO18626StatusChangeService extends ActionISO18626RequesterService {

    public static final String SETTING_YES = "yes";

    SettingsService settingsService;
    RerequestService rerequestService;

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
                // Add an audit entry

                actionResultDetails.auditMessage = auditMessage
                if (actionResultDetails.qualifier.equalsIgnoreCase(ActionEventResultQualifier.QUALIFIER_CANCELLED) &&
                        'ABORT'.equalsIgnoreCase(parameters.messageInfo?.note)) {
                    actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_ABORTED
                }

                if (request.stateModel.shortcode.equalsIgnoreCase(StateModel.MODEL_REQUESTER)) {
                    if (actionResultDetails.qualifier == "Unfilled") {
                        log.debug("Handling Unfilled result");
                        if (parameters.messageInfo.reasonUnfilled == "transfer") {
                            String pattern = /transferToCluster:(.+?)(#seq:.+#)?/
                            String note = parameters.messageInfo.note;
                            if (note) {
                                def matcher = note =~ pattern;
                                if (matcher.matches()) {
                                    String newCluster = matcher.group(1);
                                    if (settingsService.hasSettingValue(SettingsData.SETTING_AUTO_REREQUEST, SETTING_YES)) {
                                        //Trigger Re-Request here
                                        actionResultDetails.qualifier = "UnfilledTransfer"; //To transition to Rerequested state
                                        PatronRequest newRequest = rerequestService.createNewRequestFromExisting(request, RerequestService.preserveFields, ["systemInstanceIdentifier":newCluster]);
                                    }
                                } else {
                                    log.debug("reasonUnfilled was 'transfer', but a valid cluster id was not found in note: ${note}");
                                }
                            }

                        }
                    }
                }

                // For SLNP non-returnables we need to call AcceptItem and also set the Qualifier depending on the active client
                // BVB -> SLNP_REQ_DOCUMENT_SUPPLIED, BSZ -> SLNP_REQ_DOCUMENT_AVAILABLE
                if (request.stateModel.shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER)) {
                    log.debug('Auto Supply....')
                    performCommonAction(request, parameters, actionResultDetails, auditMessage)

                    String autoSupplySetting = AppSetting.findByKey(SettingsData.SETTING_AUTO_RESPONDER_REQUESTER_NON_RETURNABLE)?.value
                    if (autoSupplySetting) {
                        autoSupplySetting = autoSupplySetting.toLowerCase()
                        if (autoSupplySetting == "on:_available") {
                            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_DOCUMENT_AVAILABLE
                        } else if (autoSupplySetting == "on:_supplied") {
                            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_DOCUMENT_SUPPLIED
                        } else {
                            log.debug('Auto supply is turned off!')
                        }
                    }
                }

            }
        }

        // Now return the results to the caller
        return(actionResultDetails);
    }
}
