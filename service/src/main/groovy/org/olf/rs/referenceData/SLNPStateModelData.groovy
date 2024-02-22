package org.olf.rs.referenceData

import groovy.util.logging.Slf4j
import org.olf.rs.statemodel.ActionEvent
import org.olf.rs.statemodel.ActionEventResultList
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.AvailableAction
import org.olf.rs.statemodel.Events
import org.olf.rs.statemodel.StateModel
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StatusStage
import org.olf.rs.statemodel.UndoStatus;

/**
 * Loads the SLNP State model data required for the system to process requests
 */
@Slf4j
public class SLNPStateModelData {

    public static final Map<String, String> tags = [
            ACTIVE_BORROW: 'ACTIVE_BORROW',
            ACTIVE_LOAN: 'ACTIVE_LOAN',
            ACTIVE_PATRON: 'ACTIVE_PATRON'
    ];

    static private final List slnpRequesterStates = [
            [status: Status.SLNP_REQUESTER_IDLE],
            [status: Status.SLNP_REQUESTER_CANCELLED, isTerminal: true],
            [status: Status.SLNP_REQUESTER_ABORTED],
            [status: Status.SLNP_REQUESTER_SHIPPED],
            [status: Status.SLNP_REQUESTER_CHECKED_IN],
            [status: Status.SLNP_REQUESTER_AWAITING_RETURN_SHIPPING],
            [status: Status.SLNP_REQUESTER_COMPLETE, isTerminal: true]

    ];

    static private final List slnpResponderStates = [
            [status: Status.SLNP_RESPONDER_IDLE],
            [status: Status.SLNP_RESPONDER_UNFILLED, isTerminal: true],
            [status: Status.SLNP_RESPONDER_ABORTED, isTerminal: true],
            [status: Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP],
            [status: Status.SLNP_RESPONDER_AWAIT_PICKING],
            [status: Status.SLNP_RESPONDER_AWAIT_SHIP],
            [status: Status.SLNP_RESPONDER_ITEM_SHIPPED],
            [status: Status.SLNP_RESPONDER_COMPLETE, isTerminal: true]
    ];

    private static Map slnpRequesterCancelRequest = [
            code: 'slnpRequesterCancelRequest',
            description: 'The request has been cancelled by the requester staff and is completed. No further actions can be taken including the action Create revised request.',
            result: true,
            status: Status.SLNP_REQUESTER_CANCELLED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpRequesterReceived = [
            code: 'slnpRequesterReceived',
            description: 'Mark received',
            result: true,
            status: Status.SLNP_REQUESTER_CHECKED_IN,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpRequesterCheckedIn = [
            code: 'slnpRequesterCheckedIn',
            description: 'Mark returned by patron',
            result: true,
            status: Status.SLNP_REQUESTER_AWAITING_RETURN_SHIPPING,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpRequesterShippedReturn = [
            code: 'slnpRequesterShippedReturn',
            description: 'Mark return shipped',
            result: true,
            status: Status.SLNP_REQUESTER_COMPLETE,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpRequesterISO18626Aborted = [
            code: 'slnpRequesterISO18626Aborted',
            description: 'Request has been aborted by the supplier and it\'s been added to the abort queue in ZFL. Requester staff must handle the request in ZFL, either close it or restart with updated metadata as a new request.',
            result: true,
            status: Status.SLNP_REQUESTER_ABORTED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpRequesterISO18626Shipped = [
            code: 'slnpRequesterISO18626Shipped',
            description: 'Request has been updated by the supplier with the shipped status.',
            result: true,
            status: Status.SLNP_REQUESTER_SHIPPED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderRespondYes = [
            code: 'slnpResponderRespondYes',
            description: 'Item has been located and we expect to supply, staff is awaiting printing pull slip',
            result: true,
            status: Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderCannotSupply = [
            code: 'slnpResponderCannotSupply',
            description: 'Request cannot be filled and is complete',
            result: true,
            status: Status.SLNP_RESPONDER_UNFILLED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderAbortSupply = [
            code: 'slnpResponderAbortSupply',
            description: 'Request has been aborted due to missing metadata and is complete',
            result: true,
            status: Status.SLNP_RESPONDER_ABORTED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderConditionalSupply = [
            code: 'slnpResponderConditionalSupply',
            description: 'Item has been located and we expect to supply, staff is awaiting printing pull slip',
            result: true,
            status: Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderSupplierPrintPullSlip = [
            code: 'slnpResponderSupplierPrintPullSlip',
            description: 'Pull slip has been printed and item is being pulled from the shelves',
            result: true,
            status: Status.SLNP_RESPONDER_AWAIT_PICKING,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderSupplierCheckInReshare = [
            code: 'slnpResponderSupplierCheckInReshare',
            description: 'Item has been checked out from the host ILS and into ReShare',
            result: true,
            status: Status.SLNP_RESPONDER_AWAIT_SHIP,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderSupplierMarkShipped = [
            code : 'slnpResponderSupplierMarkShipped',
            description: 'Request has been shipped to the requesting library',
            result: true,
            status: Status.SLNP_RESPONDER_ITEM_SHIPPED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderItemReturned = [
            code: 'slnpResponderItemReturned',
            description: 'Request is complete',
            result: true,
            status: Status.SLNP_RESPONDER_COMPLETE,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpDefaultNoStatusChangeOK = [
            code: 'slnpDefaultNoStatusChangeOK',
            description: 'Default scenario, status is not changing',
            result: true,
            status: null,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    // SLNP Requester lists

    private static Map slnpRequesterCancelList = [
            code : ActionEventResultList.SLNP_REQUESTER_CANCEL,
            description: 'Status has changed to canceled',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterCancelRequest
            ]
    ];

    private static Map slnpRequesterReceivedList = [
            code: ActionEventResultList.SLNP_REQUESTER_RECEIVED,
            description: 'Request has been updated by the supplier with the shipped status.',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterReceived
            ]
    ];

    private static Map slnpRequesterAbortedList = [
            code: ActionEventResultList.SLNP_REQUESTER_ABORTED,
            description: 'Status has changed to canceled',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterCancelRequest
            ]
    ];

    private static Map slnpRequesterISO18626AbortedList = [
            code: ActionEventResultList.SLNP_REQUESTER_ISO_18626_ABORTED,
            description: 'Abort patron request (special to SLNP, sent as Notification)',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterISO18626Aborted
            ]
    ];

    private static Map slnpRequesterISO18626ShippedList = [
            code: ActionEventResultList.SLNP_REQUESTER_ISO_18626_SHIPPED,
            description: 'Patron request is shipped',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterISO18626Shipped
            ]
    ];

    private static Map slnpRequesterPrintPullSlipList = [
            code: ActionEventResultList.SLNP_REQUESTER_PRINT_PULL_SLIP,
            description: 'Requester has triggered print pull slip action',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpDefaultNoStatusChangeOK
            ]
    ];

    private static Map slnpRequesterCheckedInList = [
            code: ActionEventResultList.SLNP_REQUESTER_CHECKED_IN,
            description: 'Request has been checked-in to the local ILS',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterCheckedIn
            ]
    ];

    private static Map slnpRequesterShippedReturnList = [
            code: ActionEventResultList.SLNP_REQUESTER_SHIPPED_RETURN,
            description: 'Request has been returned by patron and awaits return shipping to the lender.',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterShippedReturn
            ]
    ];

    // SLNP responder lists

    private static Map slnpResponderRespondYesList = [
            code: ActionEventResultList.SLNP_RESPONDER_RESPOND_YES,
            description: 'The responder has said that they will supply the item(s)',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderRespondYes
            ]
    ];

    private static Map slnpResponderRespondCannotSupplyList = [
            code: ActionEventResultList.SLNP_RESPONDER_CANNOT_SUPPLY,
            description: 'The responder has said that they cannot supply the item(s)',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderCannotSupply
            ]
    ];

    private static Map slnpResponderAbortSupplyList = [
            code: ActionEventResultList.SLNP_RESPONDER_ABORT_SUPPLY,
            description: 'The responder has said that they will abort the supply of the item(s)',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderAbortSupply
            ]
    ];

    private static Map slnpResponderConditionalSupplyList = [
            code: ActionEventResultList.SLNP_RESPONDER_CONDITIONAL_SUPPLY,
            description: 'The responder has said that they will supply the item(s) conditionally',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderConditionalSupply
            ]
    ];

    private static Map slnpResponderConditionalSupplyNoTransitionList = [
            code: ActionEventResultList.SLNP_RESPONDER_CONDITIONAL_SUPPLY_NO_TRANSITION,
            description: 'Add loan condition',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpDefaultNoStatusChangeOK
            ]
    ];

    private static Map slnpResponderPrintPullSlipNoTransitionList = [
            code: ActionEventResultList.SLNP_RESPONDER_SUPPLIER_PRINT_PULL_SLIP_NO_TRANSITION,
            description: 'Print pull slip',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpDefaultNoStatusChangeOK
            ]
    ];

    private static Map slnpResponderSupplierPrintPullSlipList = [
            code: ActionEventResultList.SLNP_RESPONDER_SUPPLIER_PRINT_PULL_SLIP,
            description: 'Print pull slip',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderSupplierPrintPullSlip
            ]
    ];

    private static Map slnpResponderSupplierCheckInReshareList = [
            code: ActionEventResultList.SLNP_RESPONDER_SUPPLIER_CHECK_IN_RESHARE,
            description: 'Scan item barcode to fill this request',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderSupplierCheckInReshare
            ]
    ];

    private static Map slnpResponderSupplierMarkShippedList = [
            code: ActionEventResultList.SLNP_RESPONDER_SUPPLIER_MARK_SHIPPED,
            description: 'Mark request shipped',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderSupplierMarkShipped
            ]
    ];

    private static Map slnpResponderSupplierUndoLastActionList = [
            code: ActionEventResultList.SLNP_RESPONDER_UNDO_LAST_ACTION,
            description: 'Mark request shipped',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpDefaultNoStatusChangeOK
            ]
    ];

    private static Map slnpResponderItemReturnedList = [
            code: ActionEventResultList.SLNP_RESPONDER_ITEM_RETURNED,
            description: 'Scan request ID to mark request returned',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderItemReturned
            ]
    ];

    private static Map[] resultLists = [
            slnpRequesterCancelList,
            slnpRequesterReceivedList,
            slnpRequesterAbortedList,
            slnpRequesterPrintPullSlipList,
            slnpRequesterCheckedInList,
            slnpRequesterShippedReturnList,
            slnpRequesterISO18626AbortedList,
            slnpRequesterISO18626ShippedList,
            slnpResponderRespondYesList,
            slnpResponderRespondCannotSupplyList,
            slnpResponderAbortSupplyList,
            slnpResponderConditionalSupplyList,
            slnpResponderSupplierPrintPullSlipList,
            slnpResponderConditionalSupplyNoTransitionList,
            slnpResponderSupplierCheckInReshareList,
            slnpResponderSupplierUndoLastActionList,
            slnpResponderSupplierMarkShippedList,
            slnpResponderItemReturnedList,
            slnpResponderPrintPullSlipNoTransitionList
    ];

    public static void loadStatusData() {
        Status.ensure(Status.SLNP_REQUESTER_IDLE, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_CANCELLED, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_ABORTED, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_SHIPPED, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_CHECKED_IN, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_AWAITING_RETURN_SHIPPING, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_COMPLETE, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_ABORTED, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);

        Status.ensure(Status.SLNP_RESPONDER_IDLE, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_UNFILLED, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_ABORTED, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_AWAIT_PICKING, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_AWAIT_SHIP, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_ITEM_SHIPPED, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_COMPLETE, StatusStage.PREPARING, '9996', true, true, false, null, [ tags.ACTIVE_PATRON ]);
    }

    public static void loadAvailableActionData() {
        // SLNP_RES_AWAIT_SHIP OR "Awaiting shipping"
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_SUPPLIER_MARK_SHIPPED);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_SHIP, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CONDITIONAL_SUPPLY_NO_TRANSITION);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_SHIP, Actions.ACTION_UNDO, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_UNDO_LAST_ACTION);

        // SLNP_RES_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_IDLE, Actions.ACTION_RESPONDER_RESPOND_YES, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_RESPOND_YES);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CANNOT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_IDLE, Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_ABORT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CONDITIONAL_SUPPLY);

        // SLNP_RES_NEW_AWAIT_PULL_SLIP OR "Awaiting pull slip printing"
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_SUPPLIER_PRINT_PULL_SLIP);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CANNOT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_ABORT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CONDITIONAL_SUPPLY_NO_TRANSITION);

        // SLNP_RES_AWAIT_PICKING OR "Searching"
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_SUPPLIER_CHECK_IN_RESHARE);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CONDITIONAL_SUPPLY_NO_TRANSITION);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CANNOT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_SUPPLIER_PRINT_PULL_SLIP_NO_TRANSITION);

        // SLNP_RES_ITEM_SHIPPED OR "Shipped"
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_ITEM_SHIPPED, Actions.ACTION_RESPONDER_ITEM_RETURNED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_ITEM_RETURNED);

        // SLNP_REQ_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_REQUESTER_CANCEL_LOCAL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_CANCEL)
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_SLNP_REQUESTER_ISO18626_ABORTED, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.SLNP_REQUESTER_ISO_18626_ABORTED)
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_SLNP_REQUESTER_ISO18626_LOANED, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.SLNP_REQUESTER_ISO_18626_SHIPPED)

        // SLNP_REQ_SHIPPED OR "Shipped"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_SHIPPED, Actions.ACTION_REQUESTER_REQUESTER_RECEIVED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_RECEIVED);
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_SHIPPED, Actions.ACTION_SLNP_REQUESTER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_PRINT_PULL_SLIP);

        // SLNP_REQ_ABORTED OR "Aborted"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_ABORTED, Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_ABORTED);

        // SLNP_REQ_CHECKED_IN OR "In local circulation process"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_CHECKED_IN, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_CHECKED_IN);
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_CHECKED_IN, Actions.ACTION_SLNP_REQUESTER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_PRINT_PULL_SLIP);

        // SLNP_REQ_AWAITING_RETURN_SHIPPING OR "Awaiting return shipping"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_AWAITING_RETURN_SHIPPING, Actions.ACTION_REQUESTER_SHIPPED_RETURN, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_SHIPPED_RETURN);
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_AWAITING_RETURN_SHIPPING, Actions.ACTION_SLNP_REQUESTER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_PRINT_PULL_SLIP);
    }

    public static void loadActionEventData() {
        ActionEvent.ensure(Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT, 'The requester has canceled the request', true, StateModel.MODEL_SLNP_REQUESTER.capitalize() + Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT.capitalize(), ActionEventResultList.SLNP_REQUESTER_ABORTED, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_REQUESTER_PRINT_PULL_SLIP, 'The requester has initiated print pull slip', true, StateModel.MODEL_SLNP_REQUESTER.capitalize() + Actions.ACTION_SLNP_REQUESTER_PRINT_PULL_SLIP.capitalize(), ActionEventResultList.SLNP_REQUESTER_PRINT_PULL_SLIP, true);

        ActionEvent.ensure(Actions.ACTION_SLNP_REQUESTER_ISO18626_ABORTED, 'Abort patron request (special to SLNP, sent as Notification)', true, StateModel.MODEL_SLNP_REQUESTER.capitalize() + Actions.ACTION_SLNP_REQUESTER_ISO18626_ABORTED.capitalize(), ActionEventResultList.SLNP_REQUESTER_ISO_18626_ABORTED, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_REQUESTER_ISO18626_LOANED, 'Patron request is shipped', true, StateModel.MODEL_SLNP_REQUESTER.capitalize() + Actions.ACTION_SLNP_REQUESTER_ISO18626_LOANED.capitalize(), ActionEventResultList.SLNP_REQUESTER_ISO_18626_SHIPPED, true);

        ActionEvent.ensure(Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY, 'Respond "Abort Supply"', true, StateModel.MODEL_SLNP_RESPONDER.capitalize() + Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY.capitalize(), ActionEventResultList.SLNP_RESPONDER_ABORT_SUPPLY, true);
        ActionEvent.ensure(Actions.ACTION_RESPONDER_ITEM_RETURNED, 'The responder has received the returned item(s)', true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_ITEM_RETURNED.capitalize(), ActionEventResultList.SLNP_RESPONDER_ITEM_RETURNED, true);
        ActionEvent.ensure(Actions.ACTION_UNDO, 'Attempts to undo the last action performed', true, Actions.ACTION_UNDO.capitalize(), ActionEventResultList.SLNP_RESPONDER_UNDO_LAST_ACTION, false, true, UndoStatus.SKIP);

        ActionEvent.ensure(Events.EVENT_REQUESTER_NEW_SLNP_PATRON_REQUEST_INDICATION, 'A new SLNP patron request for the requester has been created', false, eventServiceName(Events.EVENT_REQUESTER_NEW_SLNP_PATRON_REQUEST_INDICATION), null);
        ActionEvent.ensure(Events.EVENT_RESPONDER_NEW_SLNP_PATRON_REQUEST_INDICATION, 'A new SLNP patron request for the responder has been created', false, eventServiceName(Events.EVENT_RESPONDER_NEW_SLNP_PATRON_REQUEST_INDICATION), null);
    }

	public static void loadStateModelData() {
		log.info("Adding SLNP state model records to the database");
        StateModel.ensure(StateModel.MODEL_SLNP_REQUESTER, null, Status.SLNP_REQUESTER_IDLE, null, null, null,
                slnpRequesterStates, [[stateModel: StateModel.MODEL_REQUESTER, priority: 6]]);
        StateModel.ensure(
                StateModel.MODEL_SLNP_RESPONDER, null, Status.SLNP_RESPONDER_IDLE,
                Actions.ACTION_RESPONDER_RESPOND_YES,
                Status.SLNP_RESPONDER_IDLE,
                null,
                slnpResponderStates, [[stateModel: StateModel.MODEL_RESPONDER, priority: 6]]);
    }

    public static void loadActionEventResultData() {
        ActionEventResultData.load(resultLists);
    }

    private static String eventServiceName(String eventName) {
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
        loadStateModelData();
        loadStatusData();
        loadAvailableActionData();
        loadActionEventData();
        loadActionEventResultData();
	}
}
