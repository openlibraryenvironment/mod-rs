package org.olf.rs.statemodel.actions

import com.k_int.web.toolkit.custprops.CustomProperty
import com.k_int.web.toolkit.settings.AppSetting
import org.olf.rs.DirectoryEntryService
import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.SettingsService
import org.olf.rs.constants.Directory;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.StateModel;

/**
 * Performed when the responder has said he cannot supply
 * @author Chas
 *
 */
public class ActionResponderSupplierCannotSupplyService extends AbstractAction {
    private static final String SETTING_INSTITUTIONAL_ID = 'default_institutional_patron_id';
    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip";

    SettingsService settingsService;
    HostLMSService hostLMSService;
    DirectoryEntryService directoryEntryService;

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just send the message of unfilled
        if (!StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER.equalsIgnoreCase(request.stateModel.shortcode)) {
            reshareActionService.sendResponse(request, 'Unfilled', parameters, actionResultDetails);

            log.debug("Checking to see if we need to send a CancelRequestItem");
            if (settingsService.hasSettingValue(SettingsData.SETTING_USE_REQUEST_ITEM, SETTING_REQUEST_ITEM_NCIP)) {
                if (hostLMSService.isManualCancelRequestItem()) {

                    CustomProperty institutionalPatronId = directoryEntryService.extractCustomPropertyFromDirectoryEntry(
                            request.resolvedRequester?.owner, Directory.KEY_LOCAL_INSTITUTION_PATRON_ID);
                    String institutionalPatronIdValue = institutionalPatronId?.value;
                    if (!institutionalPatronIdValue) {
                        // If nothing on the Directory Entry then fallback to the default in settings
                        AppSetting defaultInstitutionalPatronId = AppSetting.findByKey(SETTING_INSTITUTIONAL_ID);
                        institutionalPatronIdValue = defaultInstitutionalPatronId?.value;
                    }
                    log.debug("Sending CancelRequestItem");
                    Map cancelRequestItemResult = hostLMSService.cancelRequestItem(request, request.externalHoldRequestId, institutionalPatronIdValue);
                    log.debug("Result of CancelRequestItem is ${cancelRequestItemResult}");
                }
            }
        }


        // Now set the  audit message
        actionResultDetails.auditMessage = 'Request manually flagged unable to supply';

        return(actionResultDetails);
    }
}
