package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Supplier has printed the pull slip
 * @author Chas
 *
 */
public class ActionResponderSupplierPrintPullSlipService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just set the audit message
        actionResultDetails.auditMessage = 'Pull slip printed';
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
