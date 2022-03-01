package org.olf.rs.referenceData

import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.AvailableAction;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import groovy.util.logging.Slf4j

@Slf4j
public class AvailableActionData {

	public void load() {
		log.info("Adding available actions to the database");

		// To delete an unwanted action add State Model, State, Action to this array
		[
			[ StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_REQUESTER_CANCEL ],
			[ StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY ],
			[ StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PROXY_BORROWER, Actions.ACTION_MESSAGE ],
			[ StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PROXY_BORROWER, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION ],
			[ StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PROXY_BORROWER, Actions.ACTION_MANUAL_CLOSE ],
			[ StateModel.MODEL_RESPONDER, Status.RESPONDER_CHECKED_IN_TO_RESHARE, Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED ],
			[ StateModel.MODEL_RESPONDER, Status.RESPONDER_CHECKED_IN_TO_RESHARE, Actions.ACTION_MANUAL_CLOSE ],
			[ StateModel.MODEL_RESPONDER, Status.RESPONDER_CHECKED_IN_TO_RESHARE, Actions.ACTION_MESSAGE ],
			[ StateModel.MODEL_RESPONDER, Status.RESPONDER_CHECKED_IN_TO_RESHARE, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION ]
		].each { action_to_remove ->
			log.info("Remove available action ${action_to_remove}");
			try {
				AvailableAction.executeUpdate("""delete from AvailableAction
													 where id in ( select aa.id from AvailableAction as aa where aa.actionCode=:code and aa.fromState.code=:fs and aa.model.shortcode=:sm)""",
											  [code:action_to_remove[2],fs:action_to_remove[1],sm:action_to_remove[0]]);
			} catch ( Exception e ) {
				log.error("Unable to delete action ${action_to_remove} - ${e.message}", e);
			}
		}
		
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, 'M')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_RESPOND_YES, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, 'M')
        //AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, 'dummyAction', 'S')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_MESSAGE, 'M')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_MESSAGE, 'M')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, 'M')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_SHIPPED, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_SHIPPED, Actions.ACTION_MESSAGE, 'M')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED, Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED, Actions.ACTION_MESSAGE, 'M')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_COMPLETE, Actions.ACTION_MESSAGE, 'M')

        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, Actions.ACTION_MESSAGE, 'M')


        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_REQUESTER_BORROWER_CHECK, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_BORROWER_CHECK, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SOURCING_ITEM, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SOURCING_ITEM, Actions.ACTION_MANUAL_CLOSE, 'M')
		
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_MESSAGE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_MANUAL_CLOSE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_MESSAGE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_REQUESTER_SHIPPED_RETURN, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Actions.ACTION_MANUAL_CLOSE, 'M')

        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_COMPLETE, Actions.ACTION_MESSAGE, 'M')
        
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_SHIPPED_RETURN, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_MANUAL_CLOSE, 'M')
        
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_FILL_LOCALLY, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_CANCEL_LOCAL, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_MANUAL_CLOSE, 'M')
        
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_FILLED_LOCALLY, Actions.ACTION_MESSAGE, 'M')
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_FILLED_LOCALLY, Actions.ACTION_MANUAL_CLOSE, 'M')
	}

	public static void loadAll() {
		(new AvailableActionData()).load();
	}
}
