package org.olf.rs.referenceData

import org.olf.rs.statemodel.ActionEventResultList;
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
            [ StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_FILLED_LOCALLY, Actions.ACTION_MESSAGE ],
            [ StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_REQUESTER_CANCEL ],
            [ StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY ],
            [ StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PROXY_BORROWER, Actions.ACTION_MESSAGE ],
            [ StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PROXY_BORROWER, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION ],
            [ StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PROXY_BORROWER, Actions.ACTION_MANUAL_CLOSE ],
            [ StateModel.MODEL_RESPONDER, Status.RESPONDER_CHECKED_IN_TO_RESHARE, Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED ],
            [ StateModel.MODEL_RESPONDER, Status.RESPONDER_CHECKED_IN_TO_RESHARE, Actions.ACTION_MANUAL_CLOSE ],
            [ StateModel.MODEL_RESPONDER, Status.RESPONDER_CHECKED_IN_TO_RESHARE, Actions.ACTION_MESSAGE ],
            [ StateModel.MODEL_RESPONDER, Status.RESPONDER_CHECKED_IN_TO_RESHARE, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION ],
            [ StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_DUPLICATE_REVIEW, Actions.ACTION_REQUESTER_APPROVE_DUPLICATE ]
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
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_CANCEL_RECEIVED_ISO18626);

        // RES_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_RESPOND_YES, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_CANCEL_RECEIVED_ISO18626);

        // RES_PENDING_CONDITIONAL_ANSWER OR "Loan conditions sent"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_CANCEL_RECEIVED_ISO18626);

        // RES_CANCEL_REQUEST_RECEIVED OR "Cancel request received"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_NEW_AWAIT_PULL_SLIP OR "Awaiting pull slip printing"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_CANCEL_RECEIVED_ISO18626);

        // RES_AWAIT_PICKING OR "Searching"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_CDL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.CDL_RESPONDER_CHECK_INTO_RESHARE)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE_AND_MARK_SHIPPED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_CANCEL_RECEIVED_ISO18626);

        // RES_ITEM_SHIPPED OR "Shipped"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_SHIPPED, Actions.ACTION_RESPONDER_ISO18626_RECEIVED, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_SHIPPED, Actions.ACTION_RESPONDER_ISO18626_SHIPPED_RETURN, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_SHIPPED_RETURN_ISO18626);

        // RES_ITEM_RETURNED OR "Return shipped"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED, Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_COMPLETE OR "Complete"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_COMPLETE, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_COMPLETE, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE)

        // RES_OVERDUE OR "Overdue"
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, Actions.ACTION_RESPONDER_ISO18626_SHIPPED_RETURN, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_SHIPPED_RETURN_ISO18626);

        // RES_SEQUESTERED
        AvailableAction.ensure(StateModel.MODEL_CDL_RESPONDER, Status.RESPONDER_SEQUESTERED, Actions.ACTION_RESPONDER_SUPPLIER_FILL_DIGITAL_LOAN, AvailableAction.TRIGGER_TYPE_MANUAL)

        // RES_LOANED_DIGITALLY
        AvailableAction.ensure(StateModel.MODEL_CDL_RESPONDER, Status.RESPONDER_AWAIT_DESEQUESTRATION, Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_REQUEST_SENT_TO_SUPPLIER OR "Request sent"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SENT_TO_SUPPLIER_ISO18626);

        // REQ_CONDITIONAL_ANSWER_RECEIVED OR "Loan conditions received"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626);

        // REQ_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_REQUESTER_BORROWER_CHECK, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_INVALID_PATRON OR "Invalid patron"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_BORROWER_CHECK, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_CANCEL_PENDING OR "Cancel pending"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING, Actions.ACTION_REQUESTER_ISO18626_CANCEL_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CANCEL_PENDING_ISO18626);

        // REQ_CANCELLED OR "Cancelled"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED, Actions.ACTION_REQUESTER_REREQUEST, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.REQUESTER_REREQUEST_CANCELLED);

        // REQ_END_OF_ROTA OR "End of Rota"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA, Actions.ACTION_REQUESTER_MARK_END_OF_ROTA_REVIEWED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.REQUESTER_MARK_END_OF_ROTA_REVIEWED);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA, Actions.ACTION_REQUESTER_REREQUEST, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.REQUESTER_REREQUEST_END_OF_ROTA);

        // REQ_VALIDATED OR "Validated"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_SOURCING_ITEM OR "Sourcing"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SOURCING_ITEM, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_SUPPLIER_IDENTIFIED OR "Supplier identified"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_EXPECTS_TO_SUPPLY OR "Expects to supply"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_EXPECTS_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_EXPECTS_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_EXPECTS_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_EXPECTS_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_EXPECTS_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_EXPECTS_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_EXPECTS_TO_SUPPLY_ISO18626);

        // REQ_SHIPPED OR "Shipped"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SHIPPED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SHIPPED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SHIPPED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SHIPPED_ISO18626);

        // REQ_BORROWING_LIBRARY_RECEIVED OR "Awaiting local item creation"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_BORROWING_LIBRARY_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_BORROWING_LIBRARY_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_BORROWING_LIBRARY_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_BORROWING_LIBRARY_RECEIVED_ISO18626);

        // REQ_CHECKED_IN OR "In local circulation process"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CHECKED_IN_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CHECKED_IN_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CHECKED_IN_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_CHECKED_IN_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_AWAITING_RETURN_SHIPPING OR "Awaiting return shipping"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_REQUESTER_SHIPPED_RETURN, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_AWAITING_RETURN_SHIPPING_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_AWAITING_RETURN_SHIPPING_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_AWAITING_RETURN_SHIPPING_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_AWAITING_RETURN_SHIPPING_ISO18626);

        // REQ_SHIPPED_TO_SUPPLIER OR "Return shipped"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SHIPPED_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SHIPPED_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SHIPPED_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_SHIPPED_TO_SUPPLIER_ISO18626);

        // REQ_REQUEST_COMPLETE OR "Complete"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_COMPLETE, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.REQUESTER_NO_STATUS_CHANGE)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_COMPLETE, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_NO_STATUS_CHANGE)

        // REQ_OVERDUE OR "Overdue"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_SHIPPED_RETURN, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_OVERDUE_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_OVERDUE_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_OVERDUE_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_OVERDUE_ISO18626);

        // REQ_LOCAL_REVIEW OR "Requires review - locally available"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_FILL_LOCALLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_CANCEL_LOCAL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)

        // REQ_DUPLICATE_REVIEW Or "New request flagged as possible duplicate, needs review"
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_DUPLICATE_REVIEW,
            Actions.ACTION_REQUESTER_FORCE_VALIDATE, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_DUPLICATE_REVIEW,
            Actions.ACTION_REQUESTER_CANCEL_DUPLICATE, AvailableAction.TRIGGER_TYPE_MANUAL);

        // REQ_BLANK_FORM_REVIEW
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW,
            Actions.ACTION_REQUESTER_RETRY_REQUEST, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW,
            Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW,
            Actions.ACTION_REQUESTER_FORCE_VALIDATE, AvailableAction.TRIGGER_TYPE_MANUAL);

        // REQ_FILLED_LOCALLY OR "Filled locally"

        // REQ_BORROWER_RETURNED
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWER_RETURNED, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_BORROWER_RETURNED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWER_RETURNED, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_BORROWER_RETURNED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWER_RETURNED, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_BORROWER_RETURNED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWER_RETURNED, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_BORROWER_RETURNED_ISO18626);

        // REQ_RECALLED
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_RECALLED, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_RECALLED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_RECALLED, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_RECALLED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_RECALLED, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_RECALLED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_RECALLED, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_RECALLED_ISO18626);

        // REQ_LOANED_DIGITALLY
        AvailableAction.ensure(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Status.REQUESTER_LOANED_DIGITALLY, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_LOANED_DIGITALLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Status.REQUESTER_LOANED_DIGITALLY, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_LOANED_DIGITALLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Status.REQUESTER_LOANED_DIGITALLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_LOANED_DIGITALLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Status.REQUESTER_LOANED_DIGITALLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_LOANED_DIGITALLY_ISO18626);

        // The messageAllSeen action can be applied to all states
        assignToAllStates(StateModel.MODEL_REQUESTER, Actions.ACTION_MESSAGES_ALL_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        assignToAllStates(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Actions.ACTION_MESSAGES_ALL_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        assignToAllStates(StateModel.MODEL_RESPONDER, Actions.ACTION_MESSAGES_ALL_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        assignToAllStates(StateModel.MODEL_CDL_RESPONDER, Actions.ACTION_MESSAGES_ALL_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);

        // The messageSeen action can be applied to all states
        assignToAllStates(StateModel.MODEL_REQUESTER, Actions.ACTION_MESSAGE_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        assignToAllStates(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Actions.ACTION_MESSAGE_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        assignToAllStates(StateModel.MODEL_RESPONDER, Actions.ACTION_MESSAGE_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        assignToAllStates(StateModel.MODEL_CDL_RESPONDER, Actions.ACTION_MESSAGE_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);

        // The message action can be applied to all active states
        assignToActiveStates(StateModel.MODEL_REQUESTER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        assignToActiveStates(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.REQUESTER_NO_STATUS_CHANGE);
        assignToActiveStates(StateModel.MODEL_RESPONDER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        assignToActiveStates(StateModel.MODEL_CDL_RESPONDER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);

        // The manualClose action can be applied to all non terminal states
        assignToActiveStates(StateModel.MODEL_REQUESTER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.REQUESTER_CLOSE_MANUAL);
        assignToActiveStates(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.REQUESTER_CLOSE_MANUAL);
        assignToActiveStates(StateModel.MODEL_RESPONDER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.RESPONDER_CLOSE_MANUAL);
        assignToActiveStates(StateModel.MODEL_CDL_RESPONDER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.RESPONDER_CLOSE_MANUAL);

        // The ISO18626Notification action can be applied to all active actions
        assignToActiveStates(StateModel.MODEL_REQUESTER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_NOTIFICATION_RECEIVED_ISO18626);
        assignToActiveStates(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.REQUESTER_NOTIFICATION_RECEIVED_ISO18626);
        assignToActiveStates(StateModel.MODEL_RESPONDER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_NOTIFICATION_RECEIVED_ISO18626);
        assignToActiveStates(StateModel.MODEL_CDL_RESPONDER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_NOTIFICATION_RECEIVED_ISO18626);

        // The ISO18626StatusRequest action can be applied to all active responder actions
        assignToActiveStates(StateModel.MODEL_RESPONDER, Actions.ACTION_RESPONDER_ISO18626_STATUS_REQUEST, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);
        assignToActiveStates(StateModel.MODEL_CDL_RESPONDER, Actions.ACTION_RESPONDER_ISO18626_STATUS_REQUEST, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);


        // These ones are for when the state is specified in the message from the responder for ISO-18626, hence trigger is system
        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_AWAITING_RETURN_SHIPPING_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_BORROWER_RETURNED,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_BORROWER_RETURNED_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_BORROWING_LIBRARY_RECEIVED_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_CANCEL_PENDING,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_CANCEL_PENDING_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_CHECKED_IN,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_CHECKED_IN_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_EXPECTS_TO_SUPPLY_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_OVERDUE,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_OVERDUE_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_RECALLED,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_RECALLED_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_SENT_TO_SUPPLIER_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_SHIPPED,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_SHIPPED_ISO18626
        );

        AvailableAction.ensure(
            StateModel.MODEL_REQUESTER,
            Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER,
            Actions.ACTION_INCOMING_ISO18626,
            AvailableAction.TRIGGER_TYPE_SYSTEM,
            ActionEventResultList.REQUESTER_SHIPPED_TO_SUPPLIER_ISO18626
        );
    }

    private void assignToAllStates(String model, String action, String triggerType, String resultList) {
        // The supplied action can be applied to all states
        List<Status> allStates = StateModel.getAllStates(model);
        assignToStates(allStates, model, action, triggerType, resultList);
    }

    private void assignToActiveStates(String model, String action, String triggerType, String resultList) {
        // The supplied action can be applied to all active states
        List<Status> activeStates = StateModel.getActiveStates(model);
        assignToStates(activeStates, model, action, triggerType, resultList);
    }

    private void assignToNonTerminalStates(String model, String action, String triggerType, String resultList) {
        // The supplied action can be applied to all non terminal states
        List<Status> nonTerminalStates = StateModel.getNonTerminalStates(model);
        assignToStates(nonTerminalStates, model, action, triggerType, resultList);
    }

    private void assignToStates(List<Status> states, String model, String action, String triggerType, String resultList) {
        // Make the action to all the states in the list
        states.each { status ->
            AvailableAction.ensure(model, status.code, action, triggerType, resultList);
        }
    }
}
