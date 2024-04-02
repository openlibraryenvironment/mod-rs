package org.olf.rs.referenceData

import org.olf.rs.statemodel.Status;

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
        [ status : Status.RESPONDER_AWAIT_PICKING ],
        [ status : Status.RESPONDER_DOCUMENT_DELIVERED, isTerminal: true ],
    ];

    //NR REQUEST ACTIONEVENT RESULTS

    private static Map nrRequesterNewPatronRequestOK = [
        code: 'nrRequesterNewPatronRequestOK',
        description: 'New non-returnables patron request has passed initial checks',
        result: true,
        status: Status.PATRON_REQUEST_VALIDATED,
        qualifier, null,
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
            qualifier: ActionEventResultQualifier.QUALIFIER_EXPECTS_TO_SUPPLY,
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null

    ];

    private static Map nrRequesterISO18626Unfilled = [
            code: 'nrRequesterISO18626Unfilled',
            description: 'Incoming ISO18626 message from the responder has said the status is Unfilled',
            result: true,
            status: Status.PATRON_REQUEST_UNFILLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_UNFILLED,
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
            description: 'Incoming ISO18626 message fromt he responder has said the status is Delivered',
            result: true,
            status: Status.PATRON_REQUEST_DELIVERED,
            qualifier: ActionEventResultQualifier.QUALIFIER_DELIVERED,
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    private static Map nrRequesterCompleteOK = [
            code: 'requesterCompleteOK',
            description: 'The request is completed',
            result: true,
            status: Status.PATRON_REQUEST_COMPLETE,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    //NR REQUEST ACTIONEVENT RESULT LISTS
    private static Map nrRequesterNewPatronRequestList = [
            code: ActionEventResultList.NR_REQUESTER_EVENT_NEW_PATRON_REQUEST,
            description: 'Event for a new Non-Returnable Request entering the system',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterNewPatronRequestOK,
                    nrRequesterOverLimit,
                    nrRequesterInvalidPatron,
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
            description: 'Sets our status based on an incoming ISO 18626 status when we are in state ' + Status.PATRON_REQUEST_REQUEST_EXPECTS_TO_SUPPLY,
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterISO18626Delivered,
                    nrRequesterISO18626Unfilled,
                    nrDefaultNoStatusChangeOK
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
            status: Status.RESPONDER_AWAIT_PICKING,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderAddURLOK = [
            code: 'nrResponderAddURLOK',
            description: 'URL has been added to document',
            result: true,
            status: Status.RES_DOCUMENT_DELIVERED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    //NR RESPONSE ACTIONEVENT RESULT LISTS

    private static Map nrResponderNewPatronRequestList = [
            code: ActionEventResultList.NR_RESPONDER_NEW_PATRON_REQUEST,
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
    ]

    private static Map[] resultLists = [
            nrRequesterNewPatronRequestList,
            nrRequesterPatronRequestValidatedList,
            nrRequesterSendToNextLocationList,
            nrRequesterSentToSupplierISO18626List,
            nrRequesterExpectToSupplyISO18626List,
            nrRequesterCompletedList,
            nrResponderNewPatronRequestList,
            nrResponderAnswerYesList,
            nrResponderCannotSupplyList,
            nrResponderPrintPullslipList,
            nrResponderAddURLToDocumentList
    ];


    // Initialization methods

    public static void loadStatusData() {
        Status.ensure(Status.PATRON_REQUEST_DOCUMENT_DELIVERED, StatusStage.COMPLETED, '10001', true, false, true, null);
        Status.ensure(Status.RESPONDER_DOCUMENT_DELIVERED, StatusStage.COMPLETED, '10002', true, false, true, null);
    }

    public static void loadActionEventResultData() {
        ActionEventResultData.load(resultLists);
    }

    public static void loadActionEventData() {
        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_REQUESTER)
    }

    public static void loadAll() {
        loadStatusData();
        loadActionEventResultData();
        loadActionEventData();
        loadStateModelData();
        loadAvailableActionData();

    }






}