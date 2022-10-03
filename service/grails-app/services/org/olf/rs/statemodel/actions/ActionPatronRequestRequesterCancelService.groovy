package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

import com.k_int.web.toolkit.refdata.RefdataCategory;
import com.k_int.web.toolkit.refdata.RefdataValue;

/**
 * Executes the actions action for the requester cancelling a request
 * @author Chas
 *
 */
public class ActionPatronRequestRequesterCancelService extends ActionPatronRequestCancelService {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_REQUESTER_CANCEL);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Do we have a reason
        if (parameters.reason) {
            RefdataCategory cat = RefdataCategory.findByDesc('cancellationReasons');
            RefdataValue val = RefdataValue.findByOwnerAndValue(cat, parameters.reason);
            if (val) {
                request.cancellationReason = val;
            }
        }

        // If we do not already have a resolved supplier in hand we cannot send ISO18626 messages
        if (request.resolvedSupplier?.id) {
            sendCancel(request, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, parameters, actionResultDetails);
        } else {
            // In this case, set the qualifier to no supplier
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO_SUPPLIER;
        }

        return(actionResultDetails);
    }
}
