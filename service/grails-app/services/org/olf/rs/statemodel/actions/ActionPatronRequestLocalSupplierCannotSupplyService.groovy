package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Action that deals with a cannot supply locally
 * @author Chas
 *
 */
public class ActionPatronRequestLocalSupplierCannotSupplyService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        PatronRequestRota prr = request.rota.find({ rota -> rota.rotaPosition == request.rotaPosition });
        if (prr) {
            Status rotaState = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_NOT_SUPPLIED);
            prr.state = rotaState;
            prr.save(flush:true, failOnError: true);
        }

        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNFILLED);
        actionResultDetails.auditMessage = 'Request locally flagged as unable to supply';

        return(actionResultDetails);
    }
}
