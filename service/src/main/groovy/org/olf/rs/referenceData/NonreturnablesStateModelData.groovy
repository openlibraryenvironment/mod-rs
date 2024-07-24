package org.olf.rs.referenceData

import groovy.util.logging.Slf4j
import org.olf.rs.statemodel.ActionEvent
import org.olf.rs.statemodel.ActionEventResultList;
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.AvailableAction
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.StatusStage;

@Slf4j
public class NonreturnablesStateModelData {

    // STATEMODEL STATE LISTING

    static private final List nrRequesterStates = [
        [ status : Status.PATRON_REQUEST_IDLE ],
        [ status : Status.PATRON_REQUEST_VALIDATED ],
        [ status : Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED ],
        [ status : Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY ],
        [ status : Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER ],
        [ status : Status.PATRON_REQUEST_DOCUMENT_DELIVERED ],
        [ status : Status.PATRON_REQUEST_REQUEST_COMPLETE, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_CANCELLED, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_OVER_LIMIT ],
        [ status : Status.PATRON_REQUEST_INVALID_PATRON ],
        [ status : Status.PATRON_REQUEST_BLANK_FORM_REVIEW ],
        [ status : Status.PATRON_REQUEST_END_OF_ROTA, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_END_OF_ROTA_REVIEWED ]
    ];

    static private final List nrResponderStates = [
        [ status : Status.RESPONDER_IDLE ],
        [ status : Status.RESPONDER_NEW_AWAIT_PULL_SLIP ],
        [ status : Status.RESPONDER_UNFILLED ],
        [ status : Status.RESPONDER_COPY_AWAIT_PICKING ],
        [ status : Status.RESPONDER_DOCUMENT_DELIVERED, isTerminal: true ]
    ];

    //NR REQUEST ACTIONEVENT RESULTS

    private static Map nrRequesterNewPatronRequestOK = [
        code: 'nrRequesterNewPatronRequestOK',
        description: 'New non-returnables patron request has passed initial checks',
        result: true,
        status: Status.PATRON_REQUEST_VALIDATED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map nrRequesterOverLimit = [
            code: 'nrRequesterOverLimit',
            description: "New non-returnables request exceeds patron's request limit",
            result: true,
            status: Status.PATRON_REQUEST_OVER_LIMIT,
            qualifier: ActionEventResultQualifier.QUALIFIER_OVER_LIMIT,
            saveRestoreState: null,
            nextActionEvent: null

    ];

    private static Map nrRequesterInvalidPatron = [
            code: 'nrRequesterInvalidPatron',
            description: "Patron for new non-returnables request is invalid",
            result: true,
            status: Status.PATRON_REQUEST_INVALID_PATRON,
            qualifier: ActionEventResultQualifier.QUALIFIER_INVALID_PATRON,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterLMSCallFailed = [
            code: 'nrRequesterLMSCallFailed',
            description: 'Failed to talk to the host LMS to validate the patron',
            status: Status.PATRON_REQUEST_INVALID_PATRON,
            qualifier: ActionEventResultQualifier.QUALIFIER_HOST_LMS_CALL_FAILED,
            result: true,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterBlankForm = [
            code: 'nrRequesterBlankForm',
            description: "New non-returnables request flagged as not having cluster ID, needs review",
            result: true,
            status: Status.PATRON_REQUEST_BLANK_FORM_REVIEW,
            qualifier: ActionEventResultQualifier.QUALIFIER_BLANK_FORM_REVIEW,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterValidatePatronRequestOK = [
            code: 'nrRequesterValidatePatronRequestOK',
            description: 'Event triggered by status change to Validate',
            result: true,
            status: Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterValidatePatronRequestEndOfRota = [
            code: 'nrRequesterValidatePatronRequestEndOfRota',
            description: 'Status change toValidate, but no suppliers found',
            result: true,
            status: Status.PATRON_REQUEST_END_OF_ROTA,
            qualifier: ActionEventResultQualifier.QUALIFIER_END_OF_ROTA,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterSendToNextLocationOK = [
            code: 'nrRequesterSendToNextLocationOK',
            description: 'Event triggered when status indicates that the request is sent to the next location',
            result: true,
            status: Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
            qualifier: null,
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    private static Map nrRequesterSendToNextLocationEndOfRota = [
            code: 'nrRequesterSendToNextLocationEndOfRota',
            description: 'Event triggered by status change, but we have reached the end of the rota',
            result: true,
            status: Status.PATRON_REQUEST_END_OF_ROTA,
            qualifier: ActionEventResultQualifier.QUALIFIER_END_OF_ROTA,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterISO18626ExpectToSupply = [
            code: 'nrRequesterISO18626ExpectToSupply',
            description: 'Incoming ISO18686 message from the responder has said that the status is ExpectToSupply',
            result: true,
            status: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
            qualifier: ActionEventResultQualifier.QUALIFIER_EXPECT_TO_SUPPLY,
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null

    ];

    private static Map nrRequesterISO18626Unfilled = [
            code: 'nrRequesterISO18626Unfilled',
            description: 'Incoming ISO18626 message from the responder has said the status is Unfilled',
            result: true,
            status: Status.PATRON_REQUEST_UNFILLED,
            //qualifier: ActionEventResultQualifier.QUALIFIER_UNFILLED,
            qualifier: 'Unfilled', //it wants to be uppercase
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    private static Map nrDefaultNoStatusChangeOK = [
            code: 'nrDefaultNoStatusChangeOK',
            description: "Default scenario, don't change",
            result: true,
            status: null,
            qualifier: null,
            nextActionEvent: null

    ];

    private static Map nrRequesterISO18626Delivered = [
            code: 'requesterISO18626Delivered',
            description: 'Incoming ISO18626 message from the responder has said the status is Loaned',
            result: true,
            status: Status.PATRON_REQUEST_DOCUMENT_DELIVERED,
            qualifier: ActionEventResultQualifier.QUALIFIER_LOANED,
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    private static Map nrRequesterDeliveredOK = [
            code: 'nrRequesterDeliveredOK',
            description: 'The request is successfully delivered',
            result: true,
            status: null, //Don't change status here
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterCompleteOK = [
            code: 'requesterCompleteOK',
            description: 'The request is completed',
            result: true,
            status: Status.PATRON_REQUEST_REQUEST_COMPLETE,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterCancelOK = [
            code: 'requesteCancelOK',
            description: 'request is cancelled',
            result: true,
            status: Status.PATRON_REQUEST_CANCELLED,
            qualifier: null,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
            nextActionEvent: null
    ];

    private static Map nrRequesterCancelOKNoSupplier = [
            code: 'requesterCancelOKNoSupplier',
            description: 'Requester has said they want to cancel the request, but there is no supplier',
            result: true,
            status: Status.PATRON_REQUEST_CANCELLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_NO_SUPPLIER,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterNewPatronRequestRetry = [
            code: 'requesterNewPatronRequestRetry',
            description: 'Retry a patron request as if new',
            result: true,
            status: Status.PATRON_REQUEST_IDLE,
            qualifer: null,
            saveRestoreState: null,
            nextActionEvent: null
    ]

    private static Map nrRequesterMarkEndOfRotaReviewed = [
            code: 'requesterMarkEndOfRotaReviewed',
            description: 'EOR request marked as reviewed',
            result: true,
            status: Status.PATRON_REQUEST_END_OF_ROTA_REVIEWED,
            qualifer: null,
            saveRestoreState: null,
            nextActionEvent: null
    ]


    //NR REQUESTER ACTIONEVENT RESULT LISTS
    private static Map nrRequesterNewPatronRequestList = [
            code: ActionEventResultList.NR_REQUESTER_EVENT_NEW_PATRON_REQUEST,
            description: 'Event for a new Non-Returnable Request entering the system',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterNewPatronRequestOK,
                    nrRequesterOverLimit,
                    nrRequesterInvalidPatron,
                    nrRequesterLMSCallFailed,
                    nrRequesterBlankForm
            ]

    ];

    private static Map nrRequesterPatronRequestValidatedList = [
            code: ActionEventResultList.NR_REQUESTER_EVENT_PATRON_REQUEST_VALIDATED,
            description: 'Event when a Non-Returnable Request is validated',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterValidatePatronRequestOK,
                    nrRequesterValidatePatronRequestEndOfRota
            ]
    ];

    private static Map nrRequesterSendToNextLocationList = [
            code: ActionEventResultList.NR_REQUESTER_EVENT_SUPPLIER_IDENTIFIED,
            description: 'Event when a Non-Returnable Request has identified a supplier',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterSendToNextLocationOK,
                    nrRequesterSendToNextLocationEndOfRota
            ]
    ];

    private static Map nrRequesterSentToSupplierISO18626List = [
            code: ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626,
            description: 'Sets our status based on an incoming ISO 18626 status when we are in state ' + Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterISO18626ExpectToSupply,
                    nrRequesterISO18626Unfilled,
                    nrDefaultNoStatusChangeOK
            ]
    ];


    private static Map nrRequesterExpectToSupplyISO18626List = [
            code: ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626,
            description: 'Sets our status based on an incoming ISO 18626 status when we are in state ' + Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterISO18626Delivered,
                    nrRequesterISO18626Unfilled,
                    nrDefaultNoStatusChangeOK
            ]
    ];

    private static Map nrRequesterDeliveredList = [
            code: ActionEventResultList.NR_REQUESTER_DOCUMENT_DELIVERED,
            description: 'Requester has had the document delivered',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterDeliveredOK
            ]

    ];

    private static Map nrRequesterCompletedList = [
            code: ActionEventResultList.NR_REQUESTER_COMPLETED,
            description: 'Requester has completed the request',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterCompleteOK
            ]
    ];

    private static Map nrRequesterCancelList = [
            code: ActionEventResultList.NR_REQUESTER_CANCEL,
            description: 'Requester has cancelled',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterCancelOK,
                    nrRequesterCancelOKNoSupplier
            ]

    ];

    private static Map nrRequesterBypassedValidationList = [
            code: ActionEventResultList.NR_REQUESTER_BYPASSED_VALIDATION,
            description: 'Requester has bypassed the validation step',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterNewPatronRequestOK
            ]
    ];

    private static Map nrRequesterRetriedValidationList = [
            code: ActionEventResultList.NR_REQUESTER_RETRIED_VALIDATION,
            description: 'Requester has requested to re-try the validation step',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterNewPatronRequestRetry
            ]
    ];

    private static Map nrRequesterMarkEndOfRotaReviewedList = [
            code: ActionEventResultList.NR_REQUESTER_MARK_END_OF_ROTA_REVIEWED,
            description: 'Requester has reviewed end of rota',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterMarkEndOfRotaReviewed
            ]
    ]

    //NR RESPONSE ACTIONEVENT RESULTS

    private static Map nrResponderNewPatronRequestOK = [
            code: 'nrResponderNewPatronRequestOK',
            description: 'New non-returnable request has come in from requester and triggered event',
            result: true,
            status: Status.RESPONDER_IDLE,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderAnswerYesOK = [
            code: 'nrResponderAnswerYesOK',
            description: 'Responder has agreed to supply non-returnable',
            result: true,
            status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null

    ];

    private static Map nrResponderCannotSupplyOK = [
            code:'nrResponderCannotSupplyOK',
            description: 'Responder cannot supply the non-returnable',
            result: true,
            status: Status.RESPONDER_UNFILLED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderPrintPullslipOK = [
            code: 'nrResponderPrintPullslipOK',
            description: 'Responder has printed the pullslip for the nonreturnable',
            result: true,
            status: Status.RESPONDER_COPY_AWAIT_PICKING,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderAddURLOK = [
            code: 'nrResponderAddURLOK',
            description: 'URL has been added to document',
            result: true,
            status: Status.RESPONDER_DOCUMENT_DELIVERED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderISO18626Cancel = [
            code: 'nrResponderISO18626Cancel',
            description: 'Requester has sent cancel message',
            result: true,
            status: Status.RESPONDER_CANCELLED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    //NR RESPONSE ACTIONEVENT RESULT LISTS

    private static Map nrResponderNewPatronRequestList = [
            code: ActionEventResultList.NR_RESPONDER_EVENT_NEW_PATRON_REQUEST,
            description: 'Event for a new incoming request on the responder side',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderNewPatronRequestOK
            ]
    ];

    private static Map nrResponderAnswerYesList = [
            code: ActionEventResultList.NR_RESPONDER_ANSWER_YES,
            description: 'Responder agrees to supply non-returnable item(s)',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderAnswerYesOK
            ]
    ];

    private static Map nrResponderCannotSupplyList = [
            code: ActionEventResultList.NR_RESPONDER_CANNOT_SUPPLY,
            description: 'The responder cannot supply the item(s)',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderCannotSupplyOK
            ]
    ];

    private static Map nrResponderPrintPullslipList = [
            code: ActionEventResultList.NR_RESPONDER_PRINT_PULL_SLIP,
            description: 'The responder has printed the pull slip',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderPrintPullslipOK
            ]
    ];

    private static Map nrResponderAddURLToDocumentList = [
            code: ActionEventResultList.NR_RESPONDER_ADD_URL_TO_DOCUMENT,
            description: 'The URL has been sent to fill the request',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderAddURLOK
            ]
    ];

    private static Map nrResponderCancelRecievedISO18626 = [
            code: ActionEventResultList.NR_RESPONDER_CANCEL_RECEIVED_ISO18626,
            description: 'An incoming ISO18626 Cancel has been received by the supplier',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderISO18626Cancel
            ]
    ];

    private static Map[] resultLists = [
            nrRequesterNewPatronRequestList,
            nrRequesterPatronRequestValidatedList,
            nrRequesterSendToNextLocationList,
            nrRequesterSentToSupplierISO18626List,
            nrRequesterExpectToSupplyISO18626List,
            nrRequesterDeliveredList,
            nrRequesterCompletedList,
            nrRequesterCancelList,
            nrRequesterBypassedValidationList,
            nrRequesterRetriedValidationList,
            nrResponderNewPatronRequestList,
            nrResponderAnswerYesList,
            nrResponderCannotSupplyList,
            nrResponderPrintPullslipList,
            nrResponderAddURLToDocumentList,
            nrResponderCancelRecievedISO18626,
            nrRequesterMarkEndOfRotaReviewedList
    ];

    // Initialization methods
    public static void loadStatusData() {
        Status.ensure(Status.PATRON_REQUEST_DOCUMENT_DELIVERED, StatusStage.COMPLETED, '10001', true, false, false, null);
        Status.ensure(Status.RESPONDER_DOCUMENT_DELIVERED, StatusStage.COMPLETED, '10002', true, false, true, null);
        Status.ensure(Status.RESPONDER_COPY_AWAIT_PICKING, StatusStage.ACTIVE, '10003', true, false, false, null);
    }

    public static void loadActionEventResultData() {
        ActionEventResultData.load(resultLists);
    }

    public static void loadActionEventData() {
        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_ADD_URL_TO_DOCUMENT,
                'Attach a URL to fulfill a nonreturnable request', true,
                StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_ADD_URL_TO_DOCUMENT.capitalize(),
                ActionEventResultList.NR_RESPONDER_ADD_URL_TO_DOCUMENT);

        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, 'Print the pull slip', true,
                StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP.capitalize(),
                ActionEventResultList.NR_RESPONDER_PRINT_PULL_SLIP);

        ActionEvent.ensure(Events.EVENT_NONRETURNABLE_REQUESTER_NEW_PATRON_REQUEST_INDICATION,
                "A new Non-Returnable patron request for the requester has been created", false,
                ActionEventData.eventServiceName(Events.EVENT_NONRETURNABLE_REQUESTER_NEW_PATRON_REQUEST_INDICATION),
                ActionEventResultList.NR_REQUESTER_EVENT_NEW_PATRON_REQUEST);

        ActionEvent.ensure(Events.EVENT_NONRETURNABLE_RESPONDER_NEW_PATRON_REQUEST_INDICATION,
                "A new Non-Returnable patron request for the responder has been created", false,
                ActionEventData.eventServiceName(Events.EVENT_NONRETURNABLE_RESPONDER_NEW_PATRON_REQUEST_INDICATION),
                ActionEventResultList.NR_RESPONDER_EVENT_NEW_PATRON_REQUEST);

        ActionEvent.ensure(Events.EVENT_STATUS_REQ_DOCUMENT_DELIVERED_INDICATION,
                "A non-returnable request has had the document delivered", false,
                ActionEventData.eventServiceName(Events.EVENT_STATUS_REQ_DOCUMENT_DELIVERED_INDICATION),
                ActionEventResultList.NR_REQUESTER_DOCUMENT_DELIVERED);

        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_REQUESTER_BYPASS_VALIDATION, 'Completely bypass the validation step',
                true,  StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_BYPASS_VALIDATION.capitalize(),
                ActionEventResultList.NR_REQUESTER_BYPASSED_VALIDATION);

        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_REQUESTER_RETRY_VALIDATION, 'Retry validation on a request',
                true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_RETRY_VALIDATION.capitalize(),
                ActionEventResultList.NR_REQUESTER_RETRIED_VALIDATION);

        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, 'The requester is asking the responder to cancel the request',
                true, StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_REQUESTER_CANCEL.capitalize(),
                ActionEventResultList.NR_REQUESTER_CANCEL);

        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_REQUESTER_COMPLETE_REQUEST, "Complete the request", true,
                StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_COMPLETE_REQUEST.capitalize(),
                ActionEventResultList.NR_REQUESTER_COMPLETED);

        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_RESPONDER_RESPOND_YES, "Respond yes to supply", true,
                StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_RESPOND_YES.capitalize(),
                ActionEventResultList.NR_RESPONDER_ANSWER_YES);

        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_CANNOT_SUPPLY, "Respond can't supply", true,
                StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY.capitalize(),
                ActionEventResultList.NR_RESPONDER_CANNOT_SUPPLY);
        
        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_REQUESTER_MARK_END_OF_ROTA_REVIEWED, "Review EOR", true,
                StateModel.MODEL_REQUESTER.capitalize() + Actions.ACTION_REQUESTER_MARK_END_OF_ROTA_REVIEWED.capitalize(),
                ActionEventResultList.NR_REQUESTER_MARK_END_OF_ROTA_REVIEWED);
    }

    public static void loadStateModelData() {
        StateModel.ensure(StateModel.MODEL_NR_REQUESTER, null, Status.PATRON_REQUEST_IDLE, null, null, null, nrRequesterStates);
        StateModel.ensure(StateModel.MODEL_NR_RESPONDER, null, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY,
                null, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, nrResponderStates);
    }

    public static void loadAvailableActionData() {
        //REQ_IDLE
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL);

        //REQ_VALIDATED
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_VALIDATED, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL);

        //REQ_BLANK_FORM_REVIEW
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_RETRY_VALIDATION, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_BYPASS_VALIDATION, AvailableAction.TRIGGER_TYPE_MANUAL);

        //REQ_INVALID_PATRON
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_NONRETURNABLE_REQUESTER_RETRY_VALIDATION, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_NONRETURNABLE_REQUESTER_BYPASS_VALIDATION, AvailableAction.TRIGGER_TYPE_MANUAL)

        //REQ_SENT_TO_SUPPLIER
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626);

        //REQ_EXPECTS_TO_SUPPLY
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626);

        //REQ_DOCUMENT_DELIVERED
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_DOCUMENT_DELIVERED, Actions.ACTION_NONRETURNABLE_REQUESTER_COMPLETE_REQUEST, AvailableAction.TRIGGER_TYPE_MANUAL);

        //REQ_END_OF_ROTA
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA, Actions.ACTION_NONRETURNABLE_REQUESTER_MARK_END_OF_ROTA_REVIEWED, AvailableAction.TRIGGER_TYPE_MANUAL);

        //RES_IDLE
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_NONRETURNABLE_RESPONDER_RESPOND_YES, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_RESPONDER_CANCEL_RECEIVED_ISO18626);

        //RES_NEW_AWAIT_PULL_SLIP
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL);

        //RES_COPY_AWAIT_PICKING
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_COPY_AWAIT_PICKING, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_ADD_URL_TO_DOCUMENT, AvailableAction.TRIGGER_TYPE_MANUAL);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_COPY_AWAIT_PICKING, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_RESPONDER_CANCEL_RECEIVED_ISO18626);
    }

    public static void loadAll() {
        loadStatusData();
        loadActionEventResultData();
        loadActionEventData();
        loadStateModelData();
        loadAvailableActionData();
    }
}