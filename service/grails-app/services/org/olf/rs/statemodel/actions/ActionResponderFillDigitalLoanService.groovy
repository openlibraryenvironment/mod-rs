package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.iso18626.ReasonForMessage;
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

/**
 * Fill a loan digitally
 *
 */
public class ActionResponderFillDigitalLoanService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_FILL_DIGITAL_LOAN);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        reshareActionService.sendSupplyingAgencyMessage(request, ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE, 'Loaned', [deliveredFormat: 'URL', *:parameters], actionResultDetails);
        actionResultDetails.auditMessage = 'Loaned digitally';

        return(actionResultDetails);
    }
}
