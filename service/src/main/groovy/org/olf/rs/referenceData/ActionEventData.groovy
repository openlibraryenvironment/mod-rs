package org.olf.rs.referenceData

import org.olf.rs.statemodel.ActionEvent;
import org.olf.rs.statemodel.ActionEventResultList;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.UndoStatus;

import groovy.util.logging.Slf4j

/**
 * Loads the ActionEvent data required for the system to process requests
 */
@Slf4j
public class ActionEventData {

	public void load() {
		log.info("Adding action and events to the database");

        // Note: The calls to capitalize and prefixing the with the model are for backward compatibility,
        // in future an actions service class should not be related to the model unless they are very specific to the model
        // All the various actions
        ActionEvent.ensure(Actions.ACTION_RESPONDER_ISO18626_CANCEL, 'An ISO-18626 Cancel Request has been received', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_ISO18626_CANCEL.capitalize(), ActionEventResultList.RESPONDER_CANCEL_RECEIVED_ISO18626);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_ISO18626_RECEIVED, 'An ISO-18626 received has arrived at the responder', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_ISO18626_RECEIVED.capitalize(), null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_ISO18626_SHIPPED_RETURN, 'An ISO-18626 shipped return has arrived at the responder', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_ISO18626_SHIPPED_RETURN.capitalize(), ActionEventResultList.RESPONDER_SHIPPED_RETURN_ISO18626);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_ISO18626_STATUS_REQUEST, 'An ISO-18626 status request has arrived at the responder', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_ISO18626_STATUS_REQUEST.capitalize(), ActionEventResultList.RESPONDER_STATUS_REQUEST_ISO18626);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_ITEM_RETURNED, 'The responder has received the returned item(s)', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_ITEM_RETURNED.capitalize(), null, true);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_LOCAL_NOTE, 'Local note updated', true, 'LocalNote', null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_RESPOND_YES, 'The responder has said they will supply the item', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_RESPOND_YES.capitalize(), ActionEventResultList.RESPONDER_ANWSER_YES);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, 'The responder has added a loan condition', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION.capitalize(), ActionEventResultList.RESPONDER_ADD_CONDITIONAL);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, 'The responder is saying they cannot supply the item(s)', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY.capitalize(), ActionEventResultList.RESPONDER_CANNOT_SUPPLY, true);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, 'The item(s) has been checked out of the responders LMS to Reshare', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE.capitalize(), ActionEventResultList.RESPONDER_CHECK_INTO_RESHARE, false, true, UndoStatus.YES);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE_AND_MARK_SHIPPED, 'The item(s) have been checked into reshare from the responders LMS and shipped to the requester', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE_AND_MARK_SHIPPED.capitalize(), ActionEventResultList.RESPONDER_CHECK_IN_AND_SHIP);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, 'The item(s) has been checked backed into the responders LMS from Reshare', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE.capitalize(), ActionEventResultList.RESPONDER_ITEM_RETURNED, true);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, 'The responder is specifying conditions before they supply the item(s)', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY.capitalize(), ActionEventResultList.RESPONDER_ANWSER_CONDITIONAL);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_MANUAL_CHECKOUT, 'Responder hs checked the item(s) out of the local LMS', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_MANUAL_CHECKOUT.capitalize(), ActionEventResultList.RESPONDER_MANUAL_CHECK_OUT);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED, 'The requester has informed the responder that they will agree to the conditions outside of any protocol in use', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED.capitalize(), ActionEventResultList.RESPONDER_MARK_CONDITIONS_AGREED, true);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED, 'The responder has shipped the item(s) to the requester', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED.capitalize(), ActionEventResultList.RESPONDER_MARK_SHIPPED, true);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, 'The responder has printed the pull slip', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP.capitalize(), ActionEventResultList.RESPONDER_PRINT_PULL_SLIP);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL, 'The responder is responding to a request to cancel the request from the requester', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL.capitalize(), ActionEventResultList.RESPONDER_CANCEL);
        // CDL specific
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_FILL_DIGITAL_LOAN, 'The responder has filled the loan digitally', true, 'ResponderFillDigitalLoan', ActionEventResultList.CDL_RESPONDER_FILL_DIGITAL_LOAN);


        // Requester Actions
        ActionEvent.ensure(Actions.ACTION_REQUESTER_BYPASS_VALIDATION, 'Completely bypass the validation step',
                true,  StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_BYPASS_VALIDATION.capitalize(),
                ActionEventResultList.REQUESTER_BYPASSED_VALIDATION);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_BORROWER_CHECK, 'Check that the borrower id is valid and they are able to request an item', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_BORROWER_CHECK.capitalize(), ActionEventResultList.REQUESTER_BORROWER_CHECK, true);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE, 'Check that the borrower id is valid and they are able to request an item, if they are not valid or cannot request an item the librarian can override this to say they are', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE.capitalize(), ActionEventResultList.REQUESTER_BORROWER_CHECK, true);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_CANCEL_DUPLICATE, 'The requester has cancelled a duplicate request',
                true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_CANCEL_DUPLICATE.capitalize(),
                ActionEventResultList.REQUESTER_CANCEL_DUPLICATE);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_RETRY_VALIDATION, 'Retry validation on a request',
                true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_RETRY_VALIDATION.capitalize(), ActionEventResultList.REQUESTER_RETRIED_VALIDATION);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_CANCEL_LOCAL, 'Cancel a request that has been made locally', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_CANCEL_LOCAL.capitalize(), ActionEventResultList.REQUESTER_CANCEL_LOCAL);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_EDIT, 'Edits a request', true, 'PatronRequestEdit', ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_FILL_LOCALLY, 'The item can be filled locally', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_FILL_LOCALLY.capitalize(), ActionEventResultList.REQUESTER_FILLED_LOCALLY);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_ISO18626_CANCEL_RESPONSE, 'An ISO-18626 Cancel Response has been received', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_ISO18626_CANCEL_RESPONSE.capitalize(), ActionEventResultList.REQUESTER_CANCEL_PENDING_ISO18626);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_ISO18626_RENEW_RESPONSE, 'An ISO-18626 Renew Response has been received', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_ISO18626_RENEW_RESPONSE.capitalize(), null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, 'An ISO-18626 Request Response has been received', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE.capitalize(), null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, 'An ISO-18626 Status Change has been received', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE.capitalize(), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, 'An ISO-18626 Status Request Response has been received', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE.capitalize(), null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY, 'The local library cannot supply', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY.capitalize(), ActionEventResultList.REQUESTER_LOCAL_CANNOT_SUPPLY);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_MARK_END_OF_ROTA_REVIEWED, 'Requested marked end of rota request as reviewed', true, 'GenericDoNothing', ActionEventResultList.REQUESTER_MARK_END_OF_ROTA_REVIEWED);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, 'The patron has returned the item(s) to the requesting libarary', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM.capitalize(), ActionEventResultList.REQUESTER_PATRON_RETURNED, true);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED, 'The patron has returned the item(s) and they have been shipped back to the responder', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED.capitalize(), ActionEventResultList.REQUESTER_PATRON_RETURNED_SHIPPED);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS, 'The requester has agreed to the conditions imposed by the responder', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS.capitalize(), ActionEventResultList.REQUESTER_AGREE_CONDITIONS, true);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'The requester is asking the responder to cancel the request', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_REQUESTER_CANCEL.capitalize(), ActionEventResultList.REQUESTER_CANCEL);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN, 'Requester has received the item(s) from the responder and checked them in manually', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN.capitalize(), ActionEventResultList.REQUESTER_MANUAL_CHECK_IN);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, 'The requester has received the item(s) from the responder', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_REQUESTER_RECEIVED.capitalize(), ActionEventResultList.REQUESTER_RECEIVED, true);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS, 'The requester has rejected the conditions imposed by the responder', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS.capitalize(), ActionEventResultList.REQUESTER_REJECT_CONDITIONS, true);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REREQUEST, 'Re-requested with changes', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_REREQUEST.capitalize(), null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_SHIPPED_RETURN, 'The requester has returned the item(s) to the responder', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_SHIPPED_RETURN.capitalize(), ActionEventResultList.REQUESTER_SHIPPED_RETURN, true);

        // Both Requester and Responder actions
        ActionEvent.ensure(Actions.ACTION_INCOMING_ISO18626, 'An incoming ISO 18626 message', true, Actions.ACTION_INCOMING_ISO18626.capitalize(), null);
        ActionEvent.ensure(Actions.ACTION_ISO18626_NOTIFICATION, 'An ISO-18626 Notification has been received', true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_ISO18626_NOTIFICATION.capitalize(), null, false, true, UndoStatus.DECIDE, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_ISO18626_NOTIFICATION.capitalize());
        ActionEvent.ensure(Actions.ACTION_MANUAL_CLOSE, 'Close the request', true, 'ManualClose', null);
        ActionEvent.ensure(Actions.ACTION_MESSAGE, 'A message is to be sent to the other side of the conversation', true, 'Message', null, false, true, UndoStatus.SKIP);
        ActionEvent.ensure(Actions.ACTION_MESSAGES_ALL_SEEN, 'Mark all message as being seen', true, 'MessagesAllSeen', null, false, false, UndoStatus.SKIP);
        ActionEvent.ensure(Actions.ACTION_MESSAGE_SEEN, 'Mark a specific message as being seen', true, 'MessageSeen', null, false, true, UndoStatus.SKIP);
        ActionEvent.ensure(Actions.ACTION_UNDO, 'Attempts to undo the last action performed', true, Actions.ACTION_UNDO.capitalize(), null, false, true, UndoStatus.SKIP);

        // All the various events
        ActionEvent.ensure(Events.EVENT_MESSAGE_REQUEST_INDICATION, 'Fill in description for this event', false, eventServiceName(Events.EVENT_MESSAGE_REQUEST_INDICATION), null);
        ActionEvent.ensure(Events.EVENT_NO_IMPLEMENTATION, 'Dummy event for all those status that do not have an indication event', false, eventServiceName("No Implementation"), null);
        ActionEvent.ensure(Events.EVENT_REQUESTER_NEW_PATRON_REQUEST_INDICATION, 'A new patron request for the requester has been created', false, eventServiceName(Events.EVENT_REQUESTER_NEW_PATRON_REQUEST_INDICATION), ActionEventResultList.REQUESTER_EVENT_NEW_PATRON_REQUEST);
        ActionEvent.ensure(Events.EVENT_REQUESTING_AGENCY_MESSAGE_INDICATION, 'Fill in description for this event', false, eventServiceName(Events.EVENT_REQUESTING_AGENCY_MESSAGE_INDICATION), null);
        ActionEvent.ensure(Events.EVENT_RESPONDER_NEW_PATRON_REQUEST_INDICATION, 'A new patron requester for the responder has been created', false, eventServiceName(Events.EVENT_RESPONDER_NEW_PATRON_REQUEST_INDICATION), ActionEventResultList.RESPONDER_EVENT_NEW_PATRON_REQUEST);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_AWAITING_RETURN_SHIPPING_INDICATION, 'Status has changed to Return Shipping', false, eventServiceName(Events.EVENT_STATUS_REQ_AWAITING_RETURN_SHIPPING_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_BORROWER_RETURNED_INDICATION, 'Status has changed to Borrower Returned', false, eventServiceName(Events.EVENT_STATUS_REQ_BORROWER_RETURNED_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_BORROWING_LIBRARY_RECEIVED_INDICATION, 'Status has changed to Corrowing Library Received', false, eventServiceName(Events.EVENT_STATUS_REQ_BORROWING_LIBRARY_RECEIVED_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_CANCEL_PENDING_INDICATION, 'Status has changed to Cancel Pending', false, eventServiceName(Events.EVENT_STATUS_REQ_CANCEL_PENDING_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_CANCELLED_INDICATION, 'Status has changed to Cancelled', false, eventServiceName(Events.EVENT_STATUS_REQ_CANCELLED_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_CANCELLED_WITH_SUPPLIER_INDICATION, 'Status has changed to cancelled with supplier', false, eventServiceName(Events.EVENT_STATUS_REQ_CANCELLED_WITH_SUPPLIER_INDICATION), ActionEventResultList.REQUESTER_CANCEL_WITH_SUPPLER_INDICATION);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_END_OF_ROTA_INDICATION, 'Status has changed to End of Rota', false, eventServiceName(Events.EVENT_STATUS_REQ_END_OF_ROTA_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_LOANED_DIGITALLY_INDICATION, 'Status has changed to Loaned Digitally', false, eventServiceName(Events.EVENT_STATUS_REQ_LOANED_DIGITALLY_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_REQUEST_SENT_TO_SUPPLIER_INDICATION, 'Status has changed to Sent to Supplier', false, eventServiceName(Events.EVENT_STATUS_REQ_REQUEST_SENT_TO_SUPPLIER_INDICATION), ActionEventResultList.REQUESTER_REQUEST_SENT_TO_SUPPLIER_INDICATION);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_SHIPPED_INDICATION, 'Status has changed to Shipped', false, eventServiceName(Events.EVENT_STATUS_REQ_SHIPPED_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_SOURCING_ITEM_INDICATION, 'Status has changed to Sourcing', false, eventServiceName(Events.EVENT_STATUS_REQ_SOURCING_ITEM_INDICATION), ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_SUPPLIER_IDENTIFIED_INDICATION, 'A supplier has been identified for the request', false, eventServiceName(Events.EVENT_STATUS_REQ_SUPPLIER_IDENTIFIED_INDICATION), ActionEventResultList.REQUESTER_SEND_TO_NEXT_LOCATION);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_UNFILLED_INDICATION, 'Requester status has changed to unfilled', false, eventServiceName(Events.EVENT_STATUS_REQ_UNFILLED_INDICATION), ActionEventResultList.REQUESTER_SEND_TO_NEXT_LOCATION);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_VALIDATED_INDICATION, 'Requester status has changed to validate', false, eventServiceName(Events.EVENT_STATUS_REQ_VALIDATED_INDICATION), ActionEventResultList.REQUESTER_VALIDATE_INDICATION);
        ActionEvent.ensure(Events.EVENT_STATUS_RESPONDER_ERROR_INDICATION, 'Status has changed to Error', false, eventServiceName(Events.EVENT_STATUS_RESPONDER_ERROR_INDICATION), ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_RESPONDER_NOT_SUPPLIED_INDICATION, 'Status has changed to Not Supplied', false, eventServiceName(Events.EVENT_STATUS_RESPONDER_NOT_SUPPLIED_INDICATION), ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_CANCEL_REQUEST_RECEIVED_INDICATION, 'Responder status has changed to Cancel Request Received', false, eventServiceName(Events.EVENT_STATUS_RES_CANCEL_REQUEST_RECEIVED_INDICATION), ActionEventResultList.RESPONDER_CANCEL_RECEIVED_INDICATION);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_CANCELLED_INDICATION, 'Status has changed to Cancelled', false, eventServiceName(Events.EVENT_STATUS_RES_CANCELLED_INDICATION), ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_CHECKED_IN_TO_RESHARE_INDICATION, 'Status has changed to Checked into Reshare', false, eventServiceName(Events.EVENT_STATUS_RES_CHECKED_IN_TO_RESHARE_INDICATION), ActionEventResultList.RESPONDER_CHECKED_INTO_RESHARE_IND);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_IDLE_INDICATION, 'Status has changed to Idle', false, eventServiceName(Events.EVENT_STATUS_RES_IDLE_INDICATION), ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_OVERDUE_INDICATION, 'Status has changed to Overdue', false, eventServiceName(Events.EVENT_STATUS_RES_OVERDUE_INDICATION), ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_AWAIT_DESEQUESTRATION_INDICATION, 'Status has changed to Awaiting Desequestration', false, eventServiceName(Events.EVENT_STATUS_RES_AWAIT_DESEQUESTRATION_INDICATION), ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        ActionEvent.ensure(Events.EVENT_SUPPLYING_AGENCY_MESSAGE_INDICATION, 'Fill in description for this event', false, eventServiceName(Events.EVENT_SUPPLYING_AGENCY_MESSAGE_INDICATION), null);
}

    public static String eventServiceName(String eventName) {
        // We only do this for backward compatibility, no need to call this in the future
        // We split the event name on the underscores then capitalize each word and then join it back together
        String[] eventNameWords = eventName.replace(' ', '_').toLowerCase().split("_");
        String eventNameNormalised = "";
        eventNameWords.each{ word ->
            eventNameNormalised += word.capitalize();
        }
        return(eventNameNormalised);
    }

	public static void loadAll() {
		(new ActionEventData()).load();
	}
}