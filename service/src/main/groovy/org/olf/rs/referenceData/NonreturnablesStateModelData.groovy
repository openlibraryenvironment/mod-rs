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
        [ status : Status.RESPONDER_DOCUMENT_DELIVERED ],
    ];

    //NR REQUEST ACTIONEVENT RESULTS

    private static Map nrRequesterNewPatronRequestOK = [
        code: 'nrRequesterNewPatronRequestOK',

    ];

    private static Map nrRequesterOverLimit = [

    ];

    private static Map nrRequesterInvalidPatron = [

    ];

    private static Map nrRequesterBlankForm = [

    ];

    private static Map nrRequesterValidatePatronRequestOK = [

    ];

    private static Map nrRequesterValidatePatronRequestEndOfRota = [

    ];

    private static Map nrRequesterSendToNextLocationOK = [

    ];

    private static Map nrRequesterSendToNextLocationEndOfRota = [

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

    private static Map nrRequesterExpect


}