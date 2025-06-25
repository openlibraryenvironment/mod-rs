package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService
import org.olf.rs.iso18626.ReasonForMessage
import org.olf.rs.iso18626.TypeStatus
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Action that deals with a cannot supply locally
 * @author Chas
 *
 */
public class ActionPatronRequestLocalSupplierCannotSupplyService extends AbstractAction {
    SettingsService settingsService

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {

        actionResultDetails.auditMessage = 'Request locally flagged as unable to supply';
        String requestRouterSetting = settingsService.getSettingValue('routing_adapter')
        if (requestRouterSetting == 'disabled') {
            reshareActionService.sendSupplyingAgencyMessage(request, ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE, TypeStatus.UNFILLED.value(), [*: parameters], actionResultDetails)
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_CONTINUE;
            // Set supplying symbol back to the default
            String defaultPeerSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_PEER_SYMBOL);
            if (defaultPeerSymbolString) {
                log.debug("Setting supplying institution symbol to ${defaultPeerSymbolString}");
                request.supplyingInstitutionSymbol = defaultPeerSymbolString;
            }
        }
        return(actionResultDetails);
    }
}
