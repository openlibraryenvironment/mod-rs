package org.olf.rs.statemodel.actions

import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Responder is replying to a cancel request from the requester
 * @author Chas
 *
 */
public class ActionResponderSupplierRespondToCancelService extends ActionResponderService {

    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip";

    HostLMSService hostLMSService;
    SettingsService settingsService;

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Send the response to the requester
        reshareActionService.sendSupplierCancelResponse(request, parameters, actionResultDetails);


            // If the cancellation is denied, switch the cancel flag back to false, otherwise send request to complete
        if (parameters?.cancelResponse == 'no') {
            // Set the audit message and qualifier
            actionResultDetails.auditMessage = 'Cancellation denied';
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO;
        } else {
            actionResultDetails.auditMessage = 'Cancellation accepted';

            //Are we using request item? If so, we need to instruct the host lms to send a cancel request item if necessary
            if (settingsService.hasSettingValue(SettingsData.SETTING_USE_REQUEST_ITEM, SETTING_REQUEST_ITEM_NCIP)) {
                if (hostLMSService.isManualCancelRequestItem()) {
                    log.debug("Sending CancelRequestItem");
                    Map cancelRequestItemResult = hostLMSService.cancelRequestItem(request, request.hrid);
                    log.debug("Result of CancelRequestItem is ${cancelRequestItemResult}");
                }
            }
        }

        return(actionResultDetails);
    }
}
