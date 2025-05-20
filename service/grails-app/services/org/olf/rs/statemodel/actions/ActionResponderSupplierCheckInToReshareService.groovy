package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestAudit
import org.olf.rs.statemodel.AbstractResponderSupplierCheckInToReshare
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * Action that occurs when the responder checjs the item into reshare from the LMS
 * @author Chas
 *
 */
public class ActionResponderSupplierCheckInToReshareService extends AbstractResponderSupplierCheckInToReshare {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        return performCommonAction(request, parameters, actionResultDetails)
    }

    @Override
    ActionResultDetails undo(PatronRequest request, PatronRequestAudit audit, ActionResultDetails actionResultDetails) {
        return performCommonUndoAction(request, actionResultDetails)
    }
}
