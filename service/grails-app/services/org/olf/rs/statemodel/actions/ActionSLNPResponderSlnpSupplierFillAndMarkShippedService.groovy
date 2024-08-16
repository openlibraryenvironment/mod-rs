package org.olf.rs.statemodel.actions

import com.k_int.web.toolkit.settings.AppSetting
import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestAudit
import org.olf.rs.statemodel.AbstractResponderSupplierCheckInToReshare
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * Action that occurs when the responder checks in to reshare and also marks shipped
 * @author Editim
 *
 */
public class ActionSLNPResponderSlnpSupplierFillAndMarkShippedService extends AbstractResponderSupplierCheckInToReshare {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED)
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Perform action Supplier check in to reshare - ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE
        performCommonAction(request, parameters, actionResultDetails)

        // Perform action Supplier mark shipped - ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED
        String autoLoanSetting = AppSetting.findByKey('auto_responder_status')?.value
        if (autoLoanSetting != null && autoLoanSetting.equalsIgnoreCase("on:_loaned_and_cannot_supply")) {
            reshareActionService.sendResponse(request, ActionEventResultQualifier.QUALIFIER_LOANED, parameters, actionResultDetails)
        }
        actionResultDetails.auditMessage = 'Shipped'
        return actionResultDetails
    }

    @Override
    ActionResultDetails undo(PatronRequest request, PatronRequestAudit audit, ActionResultDetails actionResultDetails) {
        return actionResultDetails
    }
}
