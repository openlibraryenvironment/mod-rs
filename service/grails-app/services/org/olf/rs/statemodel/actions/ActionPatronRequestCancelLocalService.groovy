package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.refdata.RefdataCategory;
import com.k_int.web.toolkit.refdata.RefdataValue;

/**
 * Action class that deals with the patron requesting a local cancel
 * @author Chas
 *
 */
public class ActionPatronRequestCancelLocalService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_CANCEL_LOCAL);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED);
        actionResultDetails.auditMessage = 'Local request cancelled';
        if (parameters.reason) {
            RefdataCategory cat = RefdataCategory.findByDesc('cancellationReasons');
            RefdataValue reason = RefdataValue.findByOwnerAndValue(cat, parameters.reason);
            if (reason) {
                request.cancellationReason = reason;
                actionResultDetails.auditMessage += ": ${reason}";
            }
        }

        return(actionResultDetails);
    }
}
