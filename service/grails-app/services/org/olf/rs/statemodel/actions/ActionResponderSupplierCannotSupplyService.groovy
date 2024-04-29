package org.olf.rs.statemodel.actions;

import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.SettingsService;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Performed when the responder has said he cannot supply
 * @author Chas
 *
 */
public class ActionResponderSupplierCannotSupplyService extends AbstractAction {

    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip";

    SettingsService settingsService;
    HostLMSService hostLMSService;

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just send the message of unfilled
        reshareActionService.sendResponse(request, 'Unfilled', parameters, actionResultDetails);

        log.debug("Checking to see if we need to send a CancelRequestItem");
        if (settingsService.hasSettingValue(SettingsData.SETTING_USE_REQUEST_ITEM, SETTING_REQUEST_ITEM_NCIP)) {
            if (hostLMSService.isManualCancelRequestItem()) {
                log.debug("Sending CancelRequestItem");
                Map cancelRequestItemResult = hostLMSService.cancelRequestItem(request, request.hrid);
                log.debug("Result of CancelRequestItem is ${cancelRequestItemResult}");
            }
        }

        // Now set the  audit message
        actionResultDetails.auditMessage = 'Request manually flagged unable to supply';

        return(actionResultDetails);
    }
}
