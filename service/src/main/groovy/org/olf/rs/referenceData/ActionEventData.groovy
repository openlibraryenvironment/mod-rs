package org.olf.rs.referenceData

import org.olf.rs.statemodel.ActionEvent;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.Events;

import groovy.util.logging.Slf4j

/**
 * Loads the ActionEvent data required for the system to process requests
 */
@Slf4j
public class ActionEventData {

	public void load() {
		log.info("Adding action and events to the database");

        // All the various actions
        ActionEvent.ensure(Actions.ACTION_RESPONDER_ITEM_RETURNED, 'The responder has received the returned item(s)', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_RESPOND_YES, 'The responder has said they will supply the item', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, 'The responder has added a loan condition', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, 'The responder is saying they cannot supply the item(s)', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, 'The item(s) has been checked out of the responders LMS to Reshare', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, 'The item(s) has been checked backed into the responders LMS from Reshare', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, 'The responder is specifying conditions before they supply the item(s)', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_MANUAL_CHECKOUT, 'Fill in the description for this action, where is it checked out from ?', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED, 'The requester has informed the responder that they will agree to the conditions outside of any protocol in use', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED, 'The responder has shipped the item(s) to the requester', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, 'The responder has printed the pull slip', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL, 'The responder is responding to a request to cancel the request from the requester', true, null);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE_AND_MARK_SHIPPED, 'The item(s) have been checked into reshare from the responders LMS and shipped to the requester', true, null);

        // Requester Actions
        ActionEvent.ensure(Actions.ACTION_REQUESTER_BORROWER_CHECK, 'Check that the borrower id is valid and they are able to request an item', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE, 'Check that the borrower id is valid and they are able to request an item, if they are not valid or cannot request an item the librarian can override this to say they are', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_CANCEL_LOCAL, 'Cancel a request that has been made locally', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_FILL_LOCALLY, 'The item can be filled locally', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY, 'The local library cannot supply', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, 'The patron has returned the item(s) to the requesting libarary', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS, 'The requester has agreed to the conditions imposed by the responder', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'The requester is asking the responder to cancel the request', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN, 'Fill in the description for this action, where is it checked in from ?', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, 'The requester has received the item(s) from the responder', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS, 'The requester has rejected the conditions imposed by the responder', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_SHIPPED_RETURN, 'The requester has returned the item(s) to the responder', true, null);
        ActionEvent.ensure(Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED, 'The patron has returned the item(s) and they have been shipped back to the responder', true, null);

        // Both Requester and Responder actions
        ActionEvent.ensure(Actions.ACTION_INCOMING_ISO18626, 'An incoming ISO 18626 message', true, null);
        ActionEvent.ensure(Actions.ACTION_MANUAL_CLOSE, 'Close the request', true, null);
        ActionEvent.ensure(Actions.ACTION_MESSAGE, 'A message is to be sent to the other side of the conversation', true, null);
        ActionEvent.ensure(Actions.ACTION_MESSAGES_ALL_SEEN, 'Mark all message as being seen', true, null);
        ActionEvent.ensure(Actions.ACTION_MESSAGE_SEEN, 'Makr a specific message as being seen', true, null);

        // All the various events
        ActionEvent.ensure(Events.EVENT_MESSAGE_REQUEST_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_NEW_PATRON_REQUEST_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_NO_IMPLEMENTATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_REQUESTING_AGENCY_MESSAGE_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_AWAITING_RETURN_SHIPPING_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_BORROWER_RETURNED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_BORROWING_LIBRARY_RECEIVED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_CANCEL_PENDING_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_CANCELLED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_CANCELLED_WITH_SUPPLIER_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_END_OF_ROTA_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_REQUEST_SENT_TO_SUPPLIER_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_SHIPPED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_SOURCING_ITEM_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_SUPPLIER_IDENTIFIED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_UNFILLED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_REQ_VALIDATED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_RESPONDER_ERROR_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_RESPONDER_NOT_SUPPLIED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_CANCEL_REQUEST_RECEIVED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_CANCELLED_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_CHECKED_IN_TO_RESHARE_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_IDLE_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_STATUS_RES_OVERDUE_INDICATION, 'Fill in description for this event', false, null);
        ActionEvent.ensure(Events.EVENT_SUPPLYING_AGENCY_MESSAGE_INDICATION, 'Fill in description for this event', false, null);
	}

	public static void loadAll() {
		(new ActionEventData()).load();
	}
}
