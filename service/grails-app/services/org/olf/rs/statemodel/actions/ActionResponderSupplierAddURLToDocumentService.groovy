package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.iso18626.ReasonForMessage
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

class ActionResponderSupplierAddURLToDocumentService extends AbstractAction{
    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        reshareActionService.sendSupplyingAgencyMessage(request, ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE, ActionEventResultQualifier.QUALIFIER_COPY_COMPLETED, [deliveredFormat: 'URL', *:parameters], actionResultDetails);
        actionResultDetails.auditMessage = 'URL Added';

        return actionResultDetails;
    }

    @Override
    String name() {
        return Actions.ACTION_RESPONDER_SUPPLIER_ADD_URL_TO_DOCUMENT;
    }
}
