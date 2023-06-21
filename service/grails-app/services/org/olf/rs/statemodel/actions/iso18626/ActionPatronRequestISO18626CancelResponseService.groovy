package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.iso18626.ReasonForMessage;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 Notification message
 * @author Chas
 *
 */
public class ActionPatronRequestISO18626CancelResponseService extends ActionISO18626RequesterService {

    @Override
    String name() {
        return(ReasonForMessage.MESSAGE_REASON_CANCEL_RESPONSE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Must have an wnswerYesNo field
        if (parameters?.messageInfo?.answerYesNo == null) {
            actionResultDetails.result == ActionResult.ERROR;
            actionResultDetails.responseResult.errorType = EventISO18626IncomingAbstractService.ERROR_TYPE_NO_CANCEL_VALUE;
        } else {
            // Call the base class first
            actionResultDetails = super.performAction(request, parameters, actionResultDetails);

            // Only continue if we were successful
            if (actionResultDetails.result == ActionResult.SUCCESS) {
                // Ensure we are dealing with a string and that it is a case we are expecting
                switch (parameters.messageInfo.answerYesNo.toString().toUpperCase()) {
                    case 'Y':
                        // The cancel response ISO18626 message should contain a status of "Cancelled", and so this case will be handled by handleStatusChange
                        actionResultDetails.auditMessage = 'Cancelled allowed by supplier.';
                        break;

                    case 'N':
                        // Is this always the correct way of doing it ?
                        actionResultDetails.auditMessage = 'Supplier denied cancellation.';
                        actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO;
                        break;

                    default:
                        actionResultDetails.result == ActionResult.ERROR;
                        actionResultDetails.responseResult.errorType = EventISO18626IncomingAbstractService.ERROR_TYPE_INVALID_CANCEL_VALUE;
                        actionResultDetails.responseResult.errorValue = parameters.messageInfo.answerYesNo.toString();
                        break;
                }
            }
        }

        // Now just call the base class
        return(actionResultDetails);
    }
}
