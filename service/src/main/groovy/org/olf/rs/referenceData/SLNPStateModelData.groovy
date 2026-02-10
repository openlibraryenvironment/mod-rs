package org.olf.rs.referenceData

import groovy.util.logging.Slf4j
import org.olf.rs.statemodel.ActionEvent
import org.olf.rs.statemodel.ActionEventResultList
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.AvailableAction
import org.olf.rs.statemodel.Events
import org.olf.rs.statemodel.StateModel
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StatusStage

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
            [status: Status.SLNP_REQUESTER_COMPLETE, isTerminal: true],
            [status: Status.SLNP_REQUESTER_ITEM_LOST, isTerminal: true],
            [status: Status.SLNP_REQUESTER_PATRON_INVALID, isTerminal: true]
    ];

    static private final List slnpResponderStates = [
            [status: Status.SLNP_RESPONDER_IDLE],
            [status: Status.SLNP_RESPONDER_UNFILLED, isTerminal: true],
            [status: Status.SLNP_RESPONDER_ABORTED, isTerminal: true],
            [status: Status.SLNP_RESPONDER_AWAIT_PICKING],
            [status: Status.SLNP_RESPONDER_ITEM_SHIPPED],
            [status: Status.SLNP_RESPONDER_COMPLETE, isTerminal: true]
    ];

    public static Map slnpRequesterCancelRequest = [
            code: 'slnpRequesterCancelRequest',
            description: 'The request has been cancelled by the requester staff and is completed. No further actions can be taken including the action Create revised request.',
            result: true,
            status: Status.SLNP_REQUESTER_CANCELLED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    public static Map slnpRequesterISO18626CancelRequest = [
            code: 'slnpRequesterISO18626CancelRequest',
            description: 'The request has been cancelled by the requester staff and is completed. No further actions can be taken including the action Create revised request.',
            result: true,
            status: Status.SLNP_REQUESTER_CANCELLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_CANCELLED,
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

    private static Map slnpRequesterMarkItemLost = [
            code: 'slnpRequesterMarkItemLost',
            description: 'Item has been lost and the request is complete',
            result: true,
            status: Status.SLNP_REQUESTER_ITEM_LOST,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    public static Map slnpRequesterISO18626Aborted = [
            code: 'slnpRequesterISO18626Aborted',
            description: 'Request has been aborted by the supplier and it\'s been added to the abort queue in ZFL. Requester staff must handle the request in ZFL, either close it or restart with updated metadata as a new request.',
            result: true,
            status: Status.SLNP_REQUESTER_ABORTED,
            qualifier: ActionEventResultQualifier.QUALIFIER_ABORTED,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpRequesterISO18626Shipped = [
            code: 'slnpRequesterISO18626Shipped',
            description: 'Request has been updated by the supplier with the shipped status.',
            result: true,
            status: Status.SLNP_REQUESTER_SHIPPED,
            qualifier: ActionEventResultQualifier.QUALIFIER_LOANED,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpRequesterManualCloseCancelled = [
            code: 'slnpRequesterManualCloseCancelled',
            description: 'Requester is closing this request as cancelled',
            result: true,
            status: Status.SLNP_REQUESTER_CANCELLED,
            qualifier: Status.SLNP_REQUESTER_CANCELLED,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map slnpRequesterManualCloseComplete = [
            code: 'slnpRequesterManualCloseComplete',
            description: 'Requester is closing this request as completed',
            result: true,
            status: Status.SLNP_REQUESTER_COMPLETE,
            qualifier: Status.SLNP_REQUESTER_COMPLETE,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map slnpRequesterManualCloseItemLost = [
            code: 'slnpRequesterManualCloseItemLost',
            description: 'Requester is closing this request as lost',
            result: true,
            status: Status.SLNP_REQUESTER_ITEM_LOST,
            qualifier: Status.SLNP_REQUESTER_ITEM_LOST,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map slnpRequesterManualClosePatronInvalid = [
            code: 'slnpRequesterManualClosePatronInvalid',
            description: 'Requester is closing this request due to invalid patron',
            result: true,
            status: Status.SLNP_REQUESTER_PATRON_INVALID,
            qualifier: Status.SLNP_REQUESTER_PATRON_INVALID,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    public static Map slnpResponderRespondYes = [
            code: 'slnpResponderRespondYes',
            description: 'Item has been located and we expect to supply, staff is awaiting printing pull slip',
            result: true,
            status: Status.SLNP_RESPONDER_AWAIT_PICKING,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    public static Map slnpResponderRespondYesUnfilled = [
            code: 'slnpResponderRespondYesUnfilled',
            description: 'Cannot Supply',
            result: true,
            status: Status.SLNP_RESPONDER_UNFILLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_UNFILLED,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    public static Map slnpResponderCannotSupply = [
            code: 'slnpResponderCannotSupply',
            description: 'Request cannot be filled and is complete',
            result: true,
            status: Status.SLNP_RESPONDER_UNFILLED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    public static Map slnpResponderAbortSupply = [
            code: 'slnpResponderAbortSupply',
            description: 'Request has been aborted due to missing metadata and is complete',
            result: true,
            status: Status.SLNP_RESPONDER_ABORTED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    public static Map slnpResponderSupplierPrintPullSlip = [
            code: 'slnpResponderSupplierPrintPullSlip',
            description: 'Pull slip has been printed and item is being pulled from the shelves',
            result: true,
            status: Status.SLNP_RESPONDER_AWAIT_PICKING,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderSupplierCheckInReshareFailure = [
            code: 'slnpResponderSupplierCheckInReshare',
            description: 'SLNP Responder has failed to check out 1 or more items out of the LMS into reshare',
            result: false,
            status: Status.SLNP_RESPONDER_AWAIT_PICKING,
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

    private static Map slnpResponderCheckoutOfReshareOK = [
            code: 'slnpResponderCheckoutOfReshareOK',
            description: 'Request is complete',
            result: true,
            status: Status.SLNP_RESPONDER_COMPLETE,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderCheckoutOfReshareFailure = [
            code: 'slnpResponderCheckoutOfReshareFailure',
            description: 'Failure in accept item call',
            result: false,
            status: Status.SLNP_RESPONDER_ITEM_SHIPPED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    public static Map slnpDefaultNoStatusChangeOK = [
            code: 'slnpDefaultNoStatusChangeOK',
            description: 'Default scenario, status is not changing',
            result: true,
            status: null,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpResponderNewPatronRequestIndRequestItemLocated = [
            code: 'slnpResponderNewPatronRequestIndRequestItemLocated',
            description: 'Event triggered by a new incoming request, the item has been located and we are configured to use request item',
            result: true,
            status: Status.SLNP_RESPONDER_AWAIT_PICKING,
            qualifier: ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map slnpResponderNewPatronRequestIndRequestItemLoaned = [
            code: 'slnpResponderNewPatronRequestIndRequestItemLoaned',
            description: 'Event triggered by incoming request, respond with message Loaned',
            result: true,
            status: Status.SLNP_RESPONDER_ITEM_SHIPPED,
            qualifier: ActionEventResultQualifier.QUALIFIER_LOANED,
            saveRestoreState: null,
            nextActionEvent: null
    ];


    private static Map slnpResponderNewPatronRequestIndRequestItemUnfilled = [
            code: 'slnpResponderNewPatronRequestIndRequestItemUnfilled',
            description: 'Event triggered by incoming request, respond with message Unfilled',
            result: true,
            status: Status.SLNP_RESPONDER_UNFILLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_UNFILLED,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map slnpResponderManualCloseUnfilled = [
            code: 'slnpResponderManualCloseUnfilled',
            description: 'Responder is closing this request as unfilled',
            result: true,
            status: Status.SLNP_RESPONDER_UNFILLED,
            qualifier: Status.SLNP_RESPONDER_UNFILLED, //Qualifier matches state name?
            saveRestoreState: null,
            nextActionEvent: null
    ]

    private static Map slnpResponderManualCloseAborted = [
            code: 'slnpResponderManualCloseAborted',
            description: 'Responder is closing this request as aborted',
            result: true,
            status: Status.SLNP_RESPONDER_ABORTED,
            qualifier: Status.SLNP_RESPONDER_ABORTED, //Qualifier matches state name?
            saveRestoreState: null,
            nextActionEvent: null
    ]

    private static Map slnpResponderManualCloseComplete = [
            code: 'slnpResponderManualCloseComplete',
            description: 'Responder is closing this request as completed',
            result: true,
            status: Status.SLNP_RESPONDER_COMPLETE,
            qualifier: Status.SLNP_RESPONDER_COMPLETE, //Qualifier matches state name?
            saveRestoreState: null,
            nextActionEvent: null
    ]

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

    private static Map slnpRequesterISO18626StatusChangeList = [
            code: ActionEventResultList.SLNP_REQUESTER_ISO_18626_STATUS_CHANGE,
            description: 'Perform SLNP state model status change',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterISO18626Aborted,
                    slnpRequesterISO18626Shipped,
                    slnpRequesterISO18626CancelRequest
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

    private static Map slnpRequesterMarkItemLostList = [
            code: ActionEventResultList.SLNP_REQUESTER_MARK_ITEM_LOST,
            description: 'Mark item lost',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterMarkItemLost
            ]
    ];

    private static Map slnpRequesterCloseManualList = [
            code: ActionEventResultList.SLNP_REQUESTER_CLOSE_MANUAL,
            description: 'The requester is terminating this request',
            model: StateModel.MODEL_SLNP_REQUESTER,
            results: [
                    slnpRequesterManualCloseCancelled,
                    slnpRequesterManualCloseComplete,
                    slnpRequesterManualCloseItemLost,
                    slnpRequesterManualClosePatronInvalid
            ]
    ]

    // SLNP responder lists
    private static Map slnpResponderRespondYesList = [
            code: ActionEventResultList.SLNP_RESPONDER_RESPOND_YES,
            description: 'The responder has said that they will supply the item(s)',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderRespondYes,
                    slnpResponderRespondYesUnfilled
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

    private static Map slnpResponderSupplierPrintPullSlipList = [
            code: ActionEventResultList.SLNP_RESPONDER_SUPPLIER_PRINT_PULL_SLIP,
            description: 'Print pull slip',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderSupplierPrintPullSlip
            ]
    ];

    private static Map slnpResponderSupplierFillAndMarkShippedList = [
            code: ActionEventResultList.SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED,
            description: 'Fill and mark request shipped',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderSupplierMarkShipped,
                    slnpResponderSupplierCheckInReshareFailure
            ]
    ];

    private static Map slnpResponderCheckoutOfReshareList = [
            code: ActionEventResultList.SLNP_RESPONDER_CHECK_OUT_OF_RESHARE,
            description: 'Scan request ID to mark request returned',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderCheckoutOfReshareOK,
                    slnpResponderCheckoutOfReshareFailure
            ]
    ];

    private static Map slnpResponderNewPatronRequestIndList = [
            code: ActionEventResultList.SLNP_RESPONDER_EVENT_NEW_PATRON_REQUEST,
            description: 'Event for new incoming request indication',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderNewPatronRequestIndRequestItemLocated,
                    slnpResponderNewPatronRequestIndRequestItemLoaned,
                    slnpResponderNewPatronRequestIndRequestItemUnfilled
            ]
    ];

    private static Map slnpResponderCloseManualList = [
            code: ActionEventResultList.SLNP_RESPONDER_CLOSE_MANUAL,
            description: 'The responder is terminating this request',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpResponderManualCloseUnfilled,
                    slnpResponderManualCloseAborted,
                    slnpResponderManualCloseComplete
            ]
    ]

    private static Map[] resultLists = [
            slnpRequesterCancelList,
            slnpRequesterReceivedList,
            slnpRequesterAbortedList,
            slnpRequesterCheckedInList,
            slnpRequesterShippedReturnList,
            slnpRequesterISO18626StatusChangeList,
            slnpRequesterCloseManualList,
            slnpResponderRespondYesList,
            slnpResponderRespondCannotSupplyList,
            slnpResponderAbortSupplyList,
            slnpResponderSupplierPrintPullSlipList,
            slnpResponderSupplierFillAndMarkShippedList,
            slnpResponderCheckoutOfReshareList,
            slnpResponderNewPatronRequestIndList,
            slnpRequesterMarkItemLostList,
            slnpResponderCloseManualList
    ];

    public static void loadStatusData() {
        // To delete an unwanted status add Status code and Stage to this array
        [
                [ Status.SLNP_REQUESTER_CANCELLED, StatusStage.PREPARING ],
                [ Status.SLNP_REQUESTER_COMPLETE, StatusStage.PREPARING ],
                [ Status.SLNP_RESPONDER_UNFILLED, StatusStage.PREPARING ],
                [ Status.SLNP_RESPONDER_ABORTED, StatusStage.PREPARING ],
                [ Status.SLNP_RESPONDER_COMPLETE, StatusStage.PREPARING ],
                [ Status.SLNP_RESPONDER_AWAIT_SHIP, StatusStage.PREPARING ],
        ].each { statusToRemove ->
            log.info("Remove status ${statusToRemove}");
            try {
                AvailableAction.executeUpdate('''delete from Status
                                                     where id in ( select s.id from Status as s where s.code=:code and s.stage=:stage )''',
                        [code:statusToRemove[0], stage:statusToRemove[1]]);
            } catch (Exception e) {
                log.error("Unable to delete status ${statusToRemove} - ${e.message}", e);
            }
        }

        Status.ensure(Status.SLNP_REQUESTER_IDLE, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_CANCELLED, StatusStage.COMPLETED, '9996', true, false, true, null);
        Status.ensure(Status.SLNP_REQUESTER_ABORTED, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_SHIPPED, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_CHECKED_IN, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_AWAITING_RETURN_SHIPPING, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_COMPLETE, StatusStage.COMPLETED, '9996', true, false, true, null);
        Status.ensure(Status.SLNP_REQUESTER_ABORTED, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_ITEM_LOST, StatusStage.COMPLETED, '9996', true, false, true, null);
        Status.ensure(Status.SLNP_REQUESTER_PATRON_INVALID, StatusStage.COMPLETED, '9996', true, true, true, null);

        Status.ensure(Status.SLNP_RESPONDER_IDLE, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_UNFILLED, StatusStage.COMPLETED, '9996', true, false, true, null);
        Status.ensure(Status.SLNP_RESPONDER_ABORTED, StatusStage.COMPLETED, '9996', true, false, true, null);
        Status.ensure(Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_AWAIT_PICKING, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_ITEM_SHIPPED, StatusStage.PREPARING, '9996', true, false, false, null, [ tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_COMPLETE, StatusStage.COMPLETED, '9996', true, false, true, null, [ tags.ACTIVE_PATRON ]);
    }

    public static void loadAvailableActionData() {
        // To delete an unwanted available action add Model id and action code to this array
        [
                [StateModel.lookup(StateModel.MODEL_SLNP_RESPONDER).id, Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE],
                [StateModel.lookup(StateModel.MODEL_SLNP_RESPONDER).id, Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED],
                [StateModel.lookup(StateModel.MODEL_SLNP_RESPONDER).id, Actions.ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE],
                [StateModel.lookup(StateModel.MODEL_SLNP_RESPONDER).id, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY]
        ]
                .each { availableActionToRemove ->
                    log.info("Remove available action ${availableActionToRemove}");
                    try {
                        AvailableAction.executeUpdate(
                                '''
                                                        delete from AvailableAction
                                                        where aa_model = :model and aa_action_code = :code
                                                     ''',
                                [model:availableActionToRemove[0], code:availableActionToRemove[1]]);
                    } catch (Exception e) {
                        log.error("Unable to delete available action ${availableActionToRemove} - ${e.message}", e);
                    }
                }

        // SLNP_RES_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_IDLE, Actions.ACTION_SLNP_RESPONDER_RESPOND_YES, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_RESPOND_YES);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CANNOT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_IDLE, Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_ABORT_SUPPLY);

        // SLNP_RES_AWAIT_PICKING OR "Searching"
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED, null, Boolean.TRUE, Boolean.TRUE);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CANNOT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_ABORT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_SUPPLIER_PRINT_PULL_SLIP);

        // SLNP_RES_ITEM_SHIPPED OR "Shipped"
        AvailableAction.ensure(StateModel.MODEL_SLNP_RESPONDER, Status.SLNP_RESPONDER_ITEM_SHIPPED, Actions.ACTION_SLNP_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CHECK_OUT_OF_RESHARE, null, Boolean.TRUE, Boolean.TRUE);

        // SLNP_REQ_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_CANCEL)
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.SLNP_REQUESTER_ISO_18626_STATUS_CHANGE)

        // SLNP_REQ_SHIPPED OR "Shipped"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_SHIPPED, Actions.ACTION_SLNP_REQUESTER_REQUESTER_RECEIVED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_RECEIVED, null, Boolean.TRUE, Boolean.TRUE);

        // SLNP_REQ_ABORTED OR "Aborted"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_ABORTED, Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_ABORTED);

        // SLNP_REQ_CHECKED_IN OR "In local circulation process"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_CHECKED_IN, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_CHECKED_IN, null, Boolean.TRUE, Boolean.TRUE);
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_CHECKED_IN, Actions.ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_SHIPPED_RETURN, null, Boolean.TRUE, Boolean.TRUE)
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_CHECKED_IN, Actions.ACTION_SLNP_REQUESTER_MARK_ITEM_LOST, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_MARK_ITEM_LOST);

        // SLNP_REQ_AWAITING_RETURN_SHIPPING OR "Awaiting return shipping"
        AvailableAction.ensure(StateModel.MODEL_SLNP_REQUESTER, Status.SLNP_REQUESTER_AWAITING_RETURN_SHIPPING, Actions.ACTION_REQUESTER_SHIPPED_RETURN, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_SHIPPED_RETURN, null, Boolean.TRUE, Boolean.FALSE);

        // Manual close
        AvailableActionData.assignToNonTerminalStates(StateModel.MODEL_SLNP_REQUESTER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_CLOSE_MANUAL)
        AvailableActionData.assignToNonTerminalStates(StateModel.MODEL_SLNP_RESPONDER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_RESPONDER_CLOSE_MANUAL)
    }

    public static void loadActionEventData() {
        ActionEvent.ensure(Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT, 'The requester has canceled the request', true, StateModel.MODEL_SLNP_REQUESTER.capitalize() + Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT.capitalize(), ActionEventResultList.SLNP_REQUESTER_ABORTED, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_REQUESTER_REQUESTER_RECEIVED, 'The requester has received the item(s) from the responder', true, StateModel.MODEL_SLNP_REQUESTER.capitalize() + Actions.ACTION_SLNP_REQUESTER_REQUESTER_RECEIVED.capitalize(), ActionEventResultList.SLNP_REQUESTER_RECEIVED, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_REQUESTER_MARK_ITEM_LOST, 'The requester has marked item lost', true, StateModel.MODEL_SLNP_REQUESTER.capitalize() + Actions.ACTION_SLNP_REQUESTER_MARK_ITEM_LOST.capitalize(), ActionEventResultList.SLNP_REQUESTER_MARK_ITEM_LOST, true);

        ActionEvent.ensure(Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY, 'Respond "Abort Supply"', true, StateModel.MODEL_SLNP_RESPONDER.capitalize() + Actions.ACTION_SLNP_RESPONDER_ABORT_SUPPLY.capitalize(), ActionEventResultList.SLNP_RESPONDER_ABORT_SUPPLY, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_RESPONDER_RESPOND_YES, 'The responder has said they will supply the item', true, StateModel.MODEL_SLNP_RESPONDER.capitalize() + Actions.ACTION_SLNP_RESPONDER_RESPOND_YES.capitalize(), ActionEventResultList.SLNP_RESPONDER_RESPOND_YES, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED, 'The item(s) has been checked out of the responders LMS to Reshare and the responder has shipped the item(s) to the requester', true, StateModel.MODEL_SLNP_RESPONDER.capitalize() + Actions.ACTION_SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED.capitalize(), ActionEventResultList.SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE, 'The item(s) has been checked backed into the responders LMS from Reshare', true, StateModel.MODEL_SLNP_RESPONDER.capitalize() + Actions.ACTION_SLNP_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE.capitalize(), ActionEventResultList.SLNP_RESPONDER_CHECK_OUT_OF_RESHARE, true);

        ActionEvent.ensure(Events.EVENT_REQUESTER_NEW_SLNP_PATRON_REQUEST_INDICATION, 'A new SLNP patron request for the requester has been created', false, eventServiceName(Events.EVENT_REQUESTER_NEW_SLNP_PATRON_REQUEST_INDICATION), null);
        ActionEvent.ensure(Events.EVENT_RESPONDER_NEW_SLNP_PATRON_REQUEST_INDICATION, 'A new SLNP patron request for the responder has been created', false, eventServiceName(Events.EVENT_RESPONDER_NEW_SLNP_PATRON_REQUEST_INDICATION), ActionEventResultList.SLNP_RESPONDER_EVENT_NEW_PATRON_REQUEST);
    }

	public static void loadStateModelData() {
		log.info("Adding SLNP state model records to the database");
        StateModel.ensure(StateModel.MODEL_SLNP_REQUESTER, null, Status.SLNP_REQUESTER_IDLE, null, null, null,
                slnpRequesterStates, [[stateModel: StateModel.MODEL_REQUESTER, priority: 6]]);
        StateModel.ensure(
                StateModel.MODEL_SLNP_RESPONDER, null, Status.SLNP_RESPONDER_IDLE,
                Actions.ACTION_SLNP_RESPONDER_RESPOND_YES,
                Status.SLNP_RESPONDER_IDLE,
                null,
                slnpResponderStates, [[stateModel: StateModel.MODEL_RESPONDER, priority: 6]]);
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

    /**
     * Loads all SLNP StateModel related data, order is important.
     */
	public static void loadAll() {
        // 1. StatusData
        loadStatusData();
        // 2. ActionEventResultData
        ActionEventResultData.load(resultLists);
        // 3. ActionEventData
        loadActionEventData();
        // 4. StateModelData
        loadStateModelData();
        // 5. AvailableActionData
        loadAvailableActionData();
	}
}
