package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * This action is performed when the requester rejects the conditions
 * @author Chas
 *
 */
public class ActionPatronRequestRequesterRejectConditionsService extends ActionPatronRequestCancelService {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        parameters.usePRSenderSymbol = true; //This tells us to use the stored symbol in the PR, rather than the default symbol, for broker mode
        sendCancel(request, Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS, parameters, actionResultDetails);
        actionResultDetails.auditMessage = 'Rejected loan conditions';

        return(actionResultDetails);
    }
}
