package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * Document has been successfully supplied from supplied side
 *
 */
public class ActionSLNPNonReturnableResponderSlnpSupplierSuppliesDocumentService extends AbstractAction {
    @Override
    String name() {
        return(Actions.ACTION_SLNP_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT)
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        actionResultDetails.auditMessage = 'Document supplied from supplied successfully'

        return (actionResultDetails)
    }
}
