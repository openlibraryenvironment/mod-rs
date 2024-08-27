package org.olf.rs.statemodel.actions


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractSupplierCheckOutOfReshare
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
/**
 * Requester has returned the item so we therefore need to check it out of reshare
 * @author Chas
 *
 */
public class ActionSLNPResponderSlnpSupplierCheckOutOfReshareService extends AbstractSupplierCheckOutOfReshare {

    @Override
    String name() {
        return(Actions.ACTION_SLNP_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        performCommonAction(request, parameters, actionResultDetails)

        return(actionResultDetails)
    }
}