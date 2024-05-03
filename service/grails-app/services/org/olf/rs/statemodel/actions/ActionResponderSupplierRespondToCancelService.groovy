package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Responder is replying to a cancel request from the requester
 * @author Chas
 *
 */
public class ActionResponderSupplierRespondToCancelService extends ActionResponderService {

    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip";

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Send the response to the requester
        reshareActionService.sendSupplierCancelResponse(request, parameters, actionResultDetails);


            // If the cancellation is denied, switch the cancel flag back to false, otherwise send request to complete
        if (parameters?.cancelResponse == 'no') {
            // Set the audit message and qualifier
            actionResultDetails.auditMessage = 'Cancellation denied';
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO;
        } else {
            actionResultDetails.auditMessage = 'Cancellation accepted';

        }

        return(actionResultDetails);
    }
}
