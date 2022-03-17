package org.olf.rs.referenceData

import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.AvailableAction;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import groovy.util.logging.Slf4j

/**
 * Creates the available actions that should be available at each state
 * @author Chas
 *
 */
@Slf4j
public class AvailableActionData {

    public static void loadAll() {
        (new AvailableActionData()).load();
    }

    public void load() {
        log.info('Adding available actions to the database');

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
        ].each { actionToRemove ->
            log.info("Remove available action ${actionToRemove}");
            try {
                AvailableAction.executeUpdate('''delete from AvailableAction
                                                     where id in ( select aa.id from AvailableAction as aa where aa.actionCode=:code and aa.fromState.code=:fs and aa.model.shortcode=:sm)''',
                                              [code:actionToRemove[2], fs:actionToRemove[1], sm:actionToRemove[0]]);
            } catch (Exception e) {
                log.error("Unable to delete action ${actionToRemove} - ${e.message}", e);
            }
        }
        
        // The comments below are to make it easier to parse this file of statics
        // for a given state from the model OR frontend translation (May drift out of date)

        // RES_AWAIT_SHIP OR "Awaiting shipping"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_RESPOND_YES, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        //AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, 'dummyAction', 'S')

        // RES_PENDING_CONDITIONAL_ANSWER OR "Loan conditions sent"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        
        // RES_CANCEL_REQUEST_RECEIVED OR "Cancel request received"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_NEW_AWAIT_PULL_SLIP OR "Awaiting pull slip printing"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_AWAIT_PICKING OR "Searching"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, AvailableAction.TRIGGER_TYPE_MANUAL)
        // Combined FILL and SHIP option. Will skip RES_AWAIT_SHIP, and so items will not be able to be added after this
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE_AND_MARK_SHIPPED, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_ITEM_SHIPPED OR "Shipped"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_SHIPPED, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_SHIPPED, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_ITEM_RETURNED OR "Return shipped"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED, Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_COMPLETE OR "Complete"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_COMPLETE, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_OVERDUE OR "Overdue"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_REQUEST_SENT_TO_SUPPLIER OR "Request sent"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_CONDITIONAL_ANSWER_RECEIVED OR "Loan conditions received"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_REQUESTER_BORROWER_CHECK, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        
        // REQ_INVALID_PATRON OR "Invalid patron"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_BORROWER_CHECK, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_CANCEL_PENDING OR "Cancel pending"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_VALIDATED OR "Validated"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_SOURCING_ITEM OR "Sourcing"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SOURCING_ITEM, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SOURCING_ITEM, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_SUPPLIER_IDENTIFIED OR "Supplier identified"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_EXPECTS_TO_SUPPLY OR "Expects to supply"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_SHIPPED OR "Shipped"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_BORROWING_LIBRARY_RECEIVED OR "Awaiting local item creation" 
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        
        // REQ_CHECKED_IN OR "In local circulation process"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_AWAITING_RETURN_SHIPPING OR "Awaiting return shipping"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_REQUESTER_SHIPPED_RETURN, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_SHIPPED_TO_SUPPLIER OR "Return shipped"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_REQUEST_COMPLETE OR "Complete"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_COMPLETE, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_OVERDUE OR "Overdue"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_SHIPPED_RETURN, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_LOCAL_REVIEW OR "Requires review - locally available"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_FILL_LOCALLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_CANCEL_LOCAL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_FILLED_LOCALLY OR "Filled locally"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_FILLED_LOCALLY, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_FILLED_LOCALLY, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL)
    }
}
