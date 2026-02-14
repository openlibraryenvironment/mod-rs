package org.olf.rs.referenceData

import groovy.util.logging.Slf4j
import org.olf.rs.statemodel.*

/**
 * Loads the SLNP Non-returnables State model data required for the system to process requests
 */
@Slf4j
public class SLNPNonReturnablesStateModelData {

    static private final List slnpNonReturnablesRequesterStates = [
            [status: Status.SLNP_REQUESTER_IDLE],
            [status: Status.SLNP_REQUESTER_ABORTED],
            [status: Status.SLNP_REQUESTER_DOCUMENT_AVAILABLE],
            [status: Status.SLNP_REQUESTER_PATRON_INVALID, isTerminal: true],
            [status: Status.SLNP_REQUESTER_CANCELLED, isTerminal: true],
            [status: Status.SLNP_REQUESTER_DOCUMENT_SUPPLIED, isTerminal: true]
    ];

    static private final List slnpNonReturnablesResponderStates = [
            [status: Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP],
            [status: Status.SLNP_RESPONDER_AWAIT_PICKING],
            [status: Status.SLNP_RESPONDER_UNFILLED, isTerminal: true],
            [status: Status.SLNP_RESPONDER_DOCUMENT_SUPPLIED, isTerminal: true]
    ];

    private static Map slnpNonReturnablesResponderUnfilled = [
            code: 'slnpNonReturnablesResponderUnfilled',
            description: 'Request cannot be filled and is complete',
            result: true,
            status: Status.SLNP_REQUESTER_CANCELLED,
            qualifier: 'Unfilled',
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map slnpNonReturnableRequesterISO18626DocumentAvailable = [
            code: 'slnpNonReturnableRequesterISO18626DocumentAvailable',
            description: 'Request is manually marked as available.',
            result: true,
            status: Status.SLNP_REQUESTER_DOCUMENT_AVAILABLE,
            qualifier: ActionEventResultQualifier.QUALIFIER_DOCUMENT_AVAILABLE,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableRequesterISO18626DocumentSupplied = [
            code: 'slnpNonReturnableRequesterISO18626DocumentSupplied',
            description: 'Request is manually marked as supplied.',
            result: true,
            status: Status.SLNP_REQUESTER_DOCUMENT_SUPPLIED,
            qualifier: ActionEventResultQualifier.QUALIFIER_DOCUMENT_SUPPLIED,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableRequesterReceived = [
            code: 'slnpNonReturnableRequesterReceived',
            description: 'Document has been successfully supplied',
            result: true,
            status: Status.SLNP_REQUESTER_DOCUMENT_SUPPLIED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableRequesterMarkedAsAvailable = [
            code: 'slnpNonReturnableRequesterMarkedAsAvailable',
            description: 'Request is manually marked as available. This action is needed when no data-change event ("Loaned") from ZFL is available. An automatic fee is created in the ILS for the request via AcceptItem.',
            result: true,
            status: Status.SLNP_REQUESTER_DOCUMENT_AVAILABLE,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableRequesterManualCloseCancelled = [
            code: 'slnpNonReturnableRequesterManualCloseCancelled',
            description: 'Manually closing because the request is cancelled',
            result: true,
            status: Status.SLNP_REQUESTER_CANCELLED,
            qualifier: Status.SLNP_REQUESTER_CANCELLED,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableRequesterManualCloseDocumentSupplied = [
            code: 'slnpNonReturnableRequesterManualCloseDocumentSupplied',
            description: 'Manually closing because the document was supplied',
            result: true,
            status: Status.SLNP_REQUESTER_DOCUMENT_SUPPLIED,
            qualifier: Status.SLNP_REQUESTER_DOCUMENT_SUPPLIED,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableRequesterManualClosePatronInvalid = [
            code: 'slnpNonReturnableRequesterManualClosePatronInvalid',
            description: 'Manually closing because the patron is invalid',
            result: true,
            status: Status.SLNP_REQUESTER_PATRON_INVALID,
            qualifier: Status.SLNP_REQUESTER_PATRON_INVALID,
            saveRestoreState: null,
            nextActionEvent : null
    ];


    private static Map slnpNonReturnableResponderSupplierSuppliesDocument = [
            code: 'slnpNonReturnableResponderSupplierSuppliesDocument',
            description: 'The document has been uploaded to a server in ZFL to fill the request. No message is sent.',
            result: true,
            status: Status.SLNP_RESPONDER_DOCUMENT_SUPPLIED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableResponderManualCloseUnfilled = [
            code: 'slnpNonReturnableResponderManualCloseUnfilled',
            description: 'Manually closing because the request was unfilled',
            result: true,
            status: Status.SLNP_RESPONDER_UNFILLED,
            qualifier: Status.SLNP_RESPONDER_UNFILLED,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableResponderManualCloseAborted = [
            code: 'slnpNonReturnableResponderManualCloseAborted',
            description: 'Manually closing because the request was aborted',
            result: true,
            status: Status.SLNP_RESPONDER_ABORTED,
            qualifier: Status.SLNP_RESPONDER_ABORTED,
            saveRestoreState: null,
            nextActionEvent : null
    ];

    private static Map slnpNonReturnableResponderManualCloseComplete = [
            code: 'slnpNonReturnableResponderManualCloseComplete',
            description: 'Manually closing because the request was completed',
            result: true,
            status: Status.SLNP_RESPONDER_COMPLETE,
            qualifier: Status.SLNP_RESPONDER_COMPLETE,
            saveRestoreState: null,
            nextActionEvent : null
    ];


    //Define Lists

    private static Map slnpNonReturnableResponderCloseManualList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_CLOSE_MANUAL,
            description: 'The responder is terminating this request',
            model: StateModel.MODEL_SLNP_RESPONDER,
            results: [
                    slnpNonReturnableResponderManualCloseUnfilled,
                    slnpNonReturnableResponderManualCloseAborted,
                    slnpNonReturnableResponderManualCloseComplete
            ]
    ];

    // Requester Event lists

    private static Map slnpNonReturnableRequesterCancelList = [
            code : ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_CANCEL,
            description: 'Status has changed to canceled',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER,
            results: [
                    SLNPStateModelData.slnpRequesterCancelRequest
            ]
    ];

    private static Map slnpNonReturnableRequesterISO18626StatusChangeList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_ISO_18626_STATUS_CHANGE,
            description: 'Perform SLNP non returnable state model status change',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER,
            results: [
                    SLNPStateModelData.slnpRequesterISO18626Aborted,
                    SLNPStateModelData.slnpRequesterISO18626CancelRequest,
                    slnpNonReturnablesResponderUnfilled,
                    slnpNonReturnableRequesterISO18626DocumentAvailable,
                    slnpNonReturnableRequesterISO18626DocumentSupplied
            ]
    ];

    private static Map slnpNonReturnableRequesterAbortedList = [
            code: ActionEventResultList.SLNP_REQUESTER_ABORTED,
            description: 'Status has changed to canceled',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER,
            results: [
                    SLNPStateModelData.slnpRequesterCancelRequest
            ]
    ];

    private static Map slnpNonReturnableRequesterReceivedList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_RECEIVED,
            description: 'Request has been updated by the supplier with a document supplied status.',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER,
            results: [
                    slnpNonReturnableRequesterReceived
            ]
    ];

    private static Map slnpNonReturnableRequesterManuallyMarkSuppliedList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED,
            description: 'Request is manually marked as supplied.',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER,
            results: [
                    slnpNonReturnableRequesterReceived
            ]
    ];

    private static Map slnpNonReturnableRequesterManuallyMarkAvailableList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE,
            description: 'Request is manually marked as available.',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER,
            results: [
                    slnpNonReturnableRequesterMarkedAsAvailable
            ]
    ];

    private static Map slnpNonReturnableRequesterCloseManualList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_CLOSE_MANUAL,
            description: 'The requester is terminating this request',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER,
            results: [
                        slnpNonReturnableRequesterManualCloseCancelled,
                        slnpNonReturnableRequesterManualCloseDocumentSupplied,
                        slnpNonReturnableRequesterManualClosePatronInvalid,
            ]
    ];

    // Responder Event lists

    private static Map slnpNonReturnableResponderRespondCannotSupplyList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_CANNOT_SUPPLY,
            description: 'The responder has said that they cannot supply the item(s)',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER,
            results: [
                    SLNPStateModelData.slnpResponderCannotSupply
            ]
    ];

    private static Map slnpNonReturnableResponderSupplierPrintPullSlipList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_SUPPLIER_PRINT_PULL_SLIP,
            description: 'Print pull slip',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER,
            results: [
                    SLNPStateModelData.slnpResponderSupplierPrintPullSlip
            ]
    ];

    private static Map slnpNonReturnableResponderSupplierSuppliesDocumentList = [
            code: ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT,
            description: 'Pull slip has been printed and document is being prepared to upload',
            model: StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER,
            results: [
                    slnpNonReturnableResponderSupplierSuppliesDocument
            ]
    ];

    private static Map[] resultLists = [
            slnpNonReturnableRequesterCancelList,
            slnpNonReturnableRequesterISO18626StatusChangeList,
            slnpNonReturnableRequesterAbortedList,
            slnpNonReturnableRequesterReceivedList,
            slnpNonReturnableRequesterManuallyMarkSuppliedList,
            slnpNonReturnableRequesterManuallyMarkAvailableList,
            slnpNonReturnableRequesterCloseManualList,

            slnpNonReturnableResponderRespondCannotSupplyList,
            slnpNonReturnableResponderSupplierPrintPullSlipList,
            slnpNonReturnableResponderSupplierSuppliesDocumentList,
            slnpNonReturnableResponderCloseManualList
    ];

    public static void loadStatusData() {
        // To delete an unwanted status add Status code and Stage to this array
        [].each { statusToRemove ->
            log.info("Remove status ${statusToRemove}");
            try {
                AvailableAction.executeUpdate('''delete from Status
                                                     where id in ( select s.id from Status as s where s.code=:code and s.stage=:stage )''',
                        [code:statusToRemove[0], stage:statusToRemove[1]]);
            } catch (Exception e) {
                log.error("Unable to delete status ${statusToRemove} - ${e.message}", e);
            }
        }
        Status.ensure(Status.SLNP_REQUESTER_DOCUMENT_AVAILABLE, StatusStage.PREPARING, '9997', true, false, false, null, [ SLNPStateModelData.tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_REQUESTER_DOCUMENT_SUPPLIED, StatusStage.COMPLETED, '9997', true, false, true, null, [ SLNPStateModelData.tags.ACTIVE_PATRON ]);
        Status.ensure(Status.SLNP_RESPONDER_DOCUMENT_SUPPLIED, StatusStage.COMPLETED, '9997', true, false, true, null, [ SLNPStateModelData.tags.ACTIVE_PATRON ]);
    }

    public static void loadAvailableActionData() {
        // To delete an unwanted available action add Model id and action code to this array
        [[StateModel.lookup(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER).id, Actions.ACTION_SLNP_REQUESTER_REQUESTER_RECEIVED],
         [StateModel.lookup(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER).id, Actions.ACTION_REQUESTER_CANCEL_LOCAL]]
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

        /// SLNP_REQ_IDLE OR "New"
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_CANCEL)
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_ISO_18626_STATUS_CHANGE)
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED)
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER, Status.SLNP_REQUESTER_IDLE, Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE)

        // SLNP_REQ_ABORTED OR "Aborted"
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER, Status.SLNP_REQUESTER_ABORTED, Actions.ACTION_SLNP_REQUESTER_HANDLE_ABORT, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_REQUESTER_ABORTED);

        // SLNP_REQ_DOCUMENT_AVAILABLE OR "Document available"
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER, Status.SLNP_REQUESTER_DOCUMENT_AVAILABLE, Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_REQUESTER_RECEIVED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_RECEIVED);

        // SLNP_RES_NEW_AWAIT_PULL_SLIP OR "Awaiting pull slip printing"
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER, Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_SUPPLIER_PRINT_PULL_SLIP);
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER, Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_CANNOT_SUPPLY);

        // SLNP_RES_AWAIT_PICKING OR "Searching"
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_SLNP_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT);
        AvailableAction.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER, Status.SLNP_RESPONDER_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_CANNOT_SUPPLY);

        // Manual Close
        AvailableActionData.assignToNonTerminalStates(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_CLOSE_MANUAL)
        AvailableActionData.assignToNonTerminalStates(StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_CLOSE_MANUAL)
    }



    public static void loadActionEventData() {
        ActionEvent.ensure(Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_REQUESTER_RECEIVED, 'Mark received and complete the request. This action is for BVB.', true, StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER.capitalize() + Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_REQUESTER_RECEIVED.capitalize(), ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_RECEIVED, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT, 'The document has been uploaded to a server in ZFL to fill the request. No message is sent.', true, StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER.capitalize() + Actions.ACTION_SLNP_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT.capitalize(), ActionEventResultList.SLNP_NON_RETURNABLE_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED, 'Request is manually marked as supplied. This action is needed when no data-change event ("Loaned") from ZFL is available.', true, StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER.capitalize() + Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED.capitalize(), ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED, true);
        ActionEvent.ensure(Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE, 'Request is manually marked as available. This action is needed when no data-change event ("Loaned") from ZFL is available.', true, StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER.capitalize() + Actions.ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE.capitalize(), ActionEventResultList.SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE, true);
    }

	public static void loadStateModelData() {
		log.info("Adding SLNP non returnable state model records to the database");
        StateModel.ensure(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER, null, Status.SLNP_REQUESTER_IDLE, null, null, null,
                slnpNonReturnablesRequesterStates, [[stateModel: StateModel.MODEL_REQUESTER, priority: 6]]);
        StateModel.ensure(
                StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER, null, Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP,
                Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP,
                Status.SLNP_RESPONDER_NEW_AWAIT_PULL_SLIP,
                null,
                slnpNonReturnablesResponderStates, [[stateModel: StateModel.MODEL_RESPONDER, priority: 6]]);
    }

    /**
     * Loads all SLNP Non-returnable StateModel related data, order is important.
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
