package org.olf.rs.referenceData

import groovy.util.logging.Slf4j
import org.olf.rs.statemodel.ActionEvent
import org.olf.rs.statemodel.ActionEventResult
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
        [ status : Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED ],
        [ status : Status.PATRON_REQUEST_DOCUMENT_DELIVERED, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_CANCEL_PENDING ],
        [ status : Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER ],
        [ status : Status.PATRON_REQUEST_CANCELLED, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_OVER_LIMIT ],
        [ status : Status.PATRON_REQUEST_INVALID_PATRON ],
        [ status : Status.PATRON_REQUEST_BLANK_FORM_REVIEW ],
        [ status : Status.PATRON_REQUEST_END_OF_ROTA, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_END_OF_ROTA_REVIEWED ],
        [ status : Status.PATRON_REQUEST_UNFILLED],
        [ status : Status.PATRON_REQUEST_REREQUESTED]
    ];

    static private final List nrResponderStates = [
        [ status : Status.RESPONDER_IDLE, canTriggerStaleRequest: true ],
        [ status : Status.RESPONDER_CANCEL_REQUEST_RECEIVED ],
        [ status : Status.RESPONDER_CANCELLED, isTerminal : true ],
        [ status : Status.RESPONDER_NEW_AWAIT_PULL_SLIP, canTriggerStaleRequest : true, triggerPullSlipEmail : true ],
        [ status : Status.RESPONDER_UNFILLED ],
        [ status : Status.RESPONDER_COPY_AWAIT_PICKING ],
        [ status : Status.RESPONDER_PENDING_CONDITIONAL_ANSWER ],
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

    private static Map nrRequesterISO18626Cancelled = [
            code: 'requesterISO18626Cancelled',
            description: 'An incoming ISO-18626 message for the requester has said that the status is Cancelled',
            result: true,
            status: Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER,
            qualifier: 'Cancelled',
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    private static Map nrRequesterISO18626CancelNo = [
            code: 'requesterISO18626CancelNo',
            description: 'An incoming ISO-18626 message for the requester has said that we are not cancelling the request',
            result: true,
            status: Status.PATRON_REQUEST_CANCEL_PENDING,
            qualifier: ActionEventResultQualifier.QUALIFIER_NO,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    private static Map nrRequesterISO18626Conditional = [
            code: 'nrRequesterISO18626Conditional',
            description: 'An incoming ISO-18626 message for the requester has said that the status is Conditional',
            result: true,
            status: Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
            qualifier: ActionEventResultQualifier.QUALIFIER_CONDITIONAL,
            saveRestoreState: null,
            fromStatus: null,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    private static Map nrRequesterISO18626NotificationConditionalExpectToSupply = [
            code: 'nrRequesterISO18626NotificationConditionalExpectToSupply',
            description: 'An incoming ISO-18626 message for the requester has said that the status is Conditional',
            result: true,
            status: Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
            qualifier: ActionEventResultQualifier.QUALIFIER_CONDITIONAL,
            saveRestoreState: null,
            fromStatus: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    /*
        Mirroring our odd behavior from ActionEventResultData.groovy
     */

    private static Map nrRequesterISO18626ExpectToSupply = [
            code: 'nrRequesterISO18626ExpectToSupply',
            description: 'Incoming ISO18686 message from the responder has said that the status is ExpectToSupply',
            result: true,
            status: null,
            qualifier: ActionEventResultQualifier.QUALIFIER_EXPECT_TO_SUPPLY,
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null

    ];

    private static Map nrRequesterISO18626WillSupply = [
            code: 'nrRequesterISO18626WillSupply',
            description: 'An incoming ISO-18626 message for the requester has said that the status is WillSupply',
            result: true,
            status: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, //Yes, WillSupply yields Expect_To_Supply status
            qualifier: ActionEventResultQualifier.QUALIFIER_WILL_SUPPLY,
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

    private static Map nrRequesterAgreeConditionsOK = [
            code: 'nrRequesterAgreeConditionsOK',
            description: 'Requester has agreed to the conditions',
            result: true,
            status: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
            qualifier: null,
            saveRestoreState: null,
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
            code: 'nrRequesterISO18626Delivered',
            description: 'Incoming ISO18626 message from the responder has said the status is Delivered',
            result: true,
            status: Status.PATRON_REQUEST_DOCUMENT_DELIVERED,
            qualifier: ActionEventResultQualifier.QUALIFIER_COPY_COMPLETED,
            saveRestoreState: null,
            updateRotaLocation: true,
            nextActionEvent: null
    ];


    private static Map nrRequesterISO18626UnfilledTransfer = [
            code: 'nrRequesterISO18626UnfilledTransfer',
            description: 'An incoming ISO-18626 message for the requester has said that the status is Unfilled, but the reason is "transfer"',
            result: true,
            status: Status.PATRON_REQUEST_REREQUESTED,
            qualifier: 'UnfilledTransfer',
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
            status: Status.PATRON_REQUEST_CANCEL_PENDING,
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
    ];

    private static Map nrRequesterRejectConditionsOK = [
            code: 'nrRequesterRejectConditionsOK',
            description: 'Requester has rejected the conditions from the responder',
            result: true,
            status: Status.PATRON_REQUEST_CANCEL_PENDING,
            qualifier: null,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
            nextActionEvent: null
    ];

    private static Map nrRequesterRerequested = [
            code: 'nrRequesterRerequested',
            description: 'Request re-requested',
            result: true,
            status: Status.PATRON_REQUEST_END_OF_ROTA, //Should this go to re-requested status?
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ]

    private static Map defaultNRNoStatusChangeOK = [
            code: 'defaultNoStatusChangeOK',
            description: 'Default scenario, status is not changing',
            result: true,
            status: null,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterManualCloseCancelledOK = [
            code: 'nonreturnableRequesterManualCloseCancelledOK',
            description: 'Requester is closing this request as cancelled',
            result: true,
            status: Status.PATRON_REQUEST_CANCELLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_CANCELLED,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterManualCloseCompleteOK = [
            code: 'nonreturnableRequesterManualCloseCompleteOK',
            description: 'Requester is closing this request as completed',
            result: true,
            status: Status.PATRON_REQUEST_REQUEST_COMPLETE,
            qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_COMPLETE,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterManualCloseEndOfRotaOK = [
            code: 'nonreturnableRequesterManualCloseEndOfRotaK',
            description: 'Requester is closing this request as end of rota',
            result: true,
            status: Status.PATRON_REQUEST_END_OF_ROTA,
            qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_END_OF_ROTA,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterManualCloseLocallyFilledOK = [
            code: 'nonreturnableRequesterManualCloseLocallyFilledOK',
            description: 'Requester is closing this request as it has been filled locally',
            result: true,
            status: Status.PATRON_REQUEST_FILLED_LOCALLY,
            qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_FILLED_LOCALLY,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrRequesterManualCloseFailure = [
            code: 'nonreturnableRequesterManualCloseFailure',
            description: 'Incorrect parameters were passed in to close the request',
            result: false,
            status: null,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];



    //NR REQUESTER ACTIONEVENT RESULT LISTS
    private static Map nrRequesterNoStatusChangeList = [
            code: ActionEventResultList.NR_REQUESTER_NO_STATUS_CHANGE,
            description: 'Default for when we do not have a status change for the non-returnable requester',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    defaultNRNoStatusChangeOK
            ]
    ];

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
                    nrRequesterISO18626Conditional,
                    nrRequesterISO18626ExpectToSupply,
                    nrRequesterISO18626WillSupply,
                    nrRequesterISO18626Unfilled,
                    nrRequesterISO18626UnfilledTransfer,
                    nrDefaultNoStatusChangeOK
            ]
    ];


    private static Map nrRequesterExpectToSupplyISO18626List = [
            code: ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626,
            description: 'Sets our status based on an incoming ISO 18626 status when we are in state ' + Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterISO18626Conditional,
                    nrRequesterISO18626Delivered,
                    nrRequesterISO18626Unfilled,
                    nrRequesterISO18626UnfilledTransfer,
                    nrDefaultNoStatusChangeOK
            ]
    ];

    private static Map nrRequesterConditionAnswerReceivedISO18626List = [
            code: ActionEventResultList.NR_REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626,
            description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterISO18626Delivered,
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

    /*
    private static Map nrRequesterCompletedList = [
            code: ActionEventResultList.NR_REQUESTER_COMPLETED,
            description: 'Requester has completed the request',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterCompleteOK
            ]
    ];
    */


    private static Map nrRequesterAgreeConditionsList = [
            code: ActionEventResultList.NR_REQUESTER_AGREE_CONDITIONS,
            description: 'Requester has agreed to the conditions',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterAgreeConditionsOK
            ]
    ];

    private static Map nrRequesterRejectConditionsList = [
            code: ActionEventResultList.NR_REQUESTER_REJECT_CONDITIONS,
            description: 'Requester has rejected the conditions',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterRejectConditionsOK
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

    private static Map nrRequesterRerequestList = [
            code: ActionEventResultList.NR_REQUESTER_REREQUEST,
            description: 'Re-request request',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterRerequested
            ]
    ]

    private static Map nrRequesterCloseManualList = [
            code: ActionEventResultList.NR_REQUESTER_CLOSE_MANUAL,
            description: 'Requester is manually closing down the request',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterManualCloseCancelledOK,
                    nrRequesterManualCloseCompleteOK,
                    nrRequesterManualCloseEndOfRotaOK,
                    nrRequesterManualCloseLocallyFilledOK,
                    nrRequesterManualCloseFailure
            ]
    ]

    private static Map nrRequesterNotificationReceivedISO18626List = [
            code: ActionEventResultList.NR_REQUESTER_NOTIFICATION_RECEIVED_ISO18626,
            description: 'An incoming ISO18626 notifications has been received by the requester',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterISO18626NotificationConditionalExpectToSupply,
                    nrDefaultNoStatusChangeOK
            ]
    ];

    private static Map nrRequesterCancelPendingISO18626List = [
            code: ActionEventResultList.NR_REQUESTER_CANCEL_PENDING_ISO18626,
            description: 'In incoming ISO18626 message has been received by a requester pending cancel',
            model: StateModel.MODEL_NR_REQUESTER,
            results: [
                    nrRequesterISO18626Cancelled,
                    nrRequesterISO18626CancelNo
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

    private static Map nrResponderNewPatronRequestOKLocated = [
            code: 'nrResponderNewPatronRequestOKLocated',
            description: 'Event triggered by a new incoming request and the item has been located',
            result: true,
            status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
            qualifier: ActionEventResultQualifier.QUALIFIER_LOCATED,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderNewPatronRequestOKUnfilled = [
            code: 'nrResponderNewPatronRequestOKUnfilled',
            description: 'Event triggered by a new incoming request and the item was not found',
            result: true,
            status: Status.RESPONDER_UNFILLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_UNFILLED,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderNewPatronRequestOKRequestItem = [
            code: 'nrResponderNewPatronRequestOKRequestItem',
            description: 'Event triggered by a new incoming request, the item has been located and we are configured to use request item',
            result: true,
            status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
            qualifier: ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderAddConditionalHoldingOK = [
            code: 'nrResponderAddConditionalHoldingOK',
            description: 'Responder has added a conditional to the request placing the request on hold',
            result: true,
            status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
            qualifier: ActionEventResultQualifier.QUALIFIER_HOLDING,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
            overrideSaveStatus: null,
            nextActionEvent: null
    ];

    private static Map nrResponderAddConditionalOK = [
            code: 'nrResponderAddConditionalOK',
            description: 'Responder has added a conditional without placing the request on hold',
            result: true,
            status: null,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderAnswerConditionalHoldingOK = [
            code: 'brResponderAnswerConditionalHoldingOK',
            description: 'Responder has responded with a conditional and placed the request on hold',
            result: true,
            status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
            qualifier: ActionEventResultQualifier.QUALIFIER_HOLDING,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
            overrideSaveStatus: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
            nextActionEvent: null
    ];

    private static Map nrResponderAnswerConditionalOK = [
            code: 'nrResponderAnswerConditionalOK',
            description: 'Responder has responded with a conditional without placing the request on hold',
            result: true,
            status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
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

    private static Map nrResponderMarkConditionsAgreedOK = [
            code: 'nrResponderMarkConditionsAgreedOK',
            description: 'Responder is saying that the requester has agreed to the conditions placed on the request',
            result: true,
            status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
            qualifier: null,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
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
            status: Status.RESPONDER_CANCEL_REQUEST_RECEIVED,
            qualifier: null,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
            nextActionEvent: null
    ];

    private static Map nrResponderManualCloseCancelledOK = [
            code: 'nonreturnableResponderManualCloseCancelledOK',
            description: 'Responder is closing this request as being cancelled',
            result: true,
            status: Status.RESPONDER_CANCELLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_RESP_CANCELLED,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderManualCloseCompleteOK = [
            code: 'nonreturnableResponderManualCloseCompleteOK',
            description: 'Responder is closing this request as completed',
            result: true,
            status: Status.RESPONDER_COMPLETE,
            qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_RESP_COMPLETE,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderManualCloseNotSuppliedOK = [
            code: 'nonreturnableResponderManualCloseNotSuppliedOK',
            description: 'Responder is closing this request as not supplied',
            result: true,
            status: Status.RESPONDER_NOT_SUPPLIED,
            qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_NOT_SUPPLIED,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderManualCloseUnfilledOK = [
            code: 'nonreturnableResponderManualCloseUnfilledOK',
            description: 'Responder is closing this request as unfilled',
            result: true,
            status: Status.RESPONDER_UNFILLED,
            qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_UNFILLED,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderCancelOK = [
            code: 'nonreturnableResponderCancelOK',
            description: 'Responder has replied yes to cancel',
            result: true,
            status: Status.RESPONDER_CANCELLED,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    private static Map nrResponderCancelNoOK = [
            code: 'nonreturnableResponderCancelNoOK',
            description: 'Responder replied no to cancel',
            result: true,
            status: Status.RESPONDER_CANCEL_REQUEST_RECEIVED,
            qualifier: ActionEventResultQualifier.QUALIFIER_NO,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
            nextActionEvent: null
    ];

    private static Map nrResponderManualCloseFailure = [
            code: 'nonreturnableResponderManualCloseFailure',
            description: 'Incorrect parameters passed to manual close',
            result: false,
            status: null,
            qualifier: null,
            saveRestoreState: null,
            nextActionEvent: null
    ];

    //NR RESPONSE ACTIONEVENT RESULT LISTS

    private static Map nrResponderISO18626AgreeConditions = [
            code: 'responderISO18626AgreeConditions',
            description: 'Requester has said they want to agree to the conditions',
            result: true,
            status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
            qualifier: ActionEventResultQualifier.QUALIFIER_CONDITIONS_AGREED,
            saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
            fromStatus: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
            updateRotaLocation: true,
            nextActionEvent: null
    ];

    private static Map nrResponderNewPatronRequestList = [
            code: ActionEventResultList.NR_RESPONDER_EVENT_NEW_PATRON_REQUEST,
            description: 'Event for a new incoming request on the responder side',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderNewPatronRequestOK,
                    nrResponderNewPatronRequestOKLocated,
                    nrResponderNewPatronRequestOKRequestItem,
                    nrResponderNewPatronRequestOKUnfilled
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

    private static Map nrResponderNoStatusChangeList = [
            code: ActionEventResultList.RESPONDER_NO_STATUS_CHANGE,
            description: 'Default for when we do not have a status change for the responder',
            model: StateModel.MODEL_RESPONDER,
            results: [
                    defaultNRNoStatusChangeOK
            ]
    ];

    private static Map nrResponderCloseManualList = [
            code: ActionEventResultList.NR_RESPONDER_CLOSE_MANUAL,
            description: 'The responder is terminating this request',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderManualCloseCancelledOK,
                    nrResponderManualCloseCompleteOK,
                    nrResponderManualCloseNotSuppliedOK,
                    nrResponderManualCloseUnfilledOK,
                    nrResponderManualCloseFailure
            ]
    ];

    private static Map nrResponderCancelList = [
            code: ActionEventResultList.NR_RESPONDER_CANCEL,
            description: 'Responder responds "yes" to cancel request',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderCancelOK,
                    nrResponderCancelNoOK
            ]
    ];

    private static Map nrResponderNotificationReceivedISO18626List = [
            code: ActionEventResultList.NR_RESPONDER_NOTIFICATION_RECEIVED_ISO18626,
            description: 'An incoming ISO18626 notifications has been received by the supplier',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderISO18626AgreeConditions,
                    nrDefaultNoStatusChangeOK
            ]
    ];

    private static Map nrResponderAddConditionalList = [
            code: ActionEventResultList.NR_RESPONDER_ADD_CONDITIONAL,
            description: 'The responder is adding a conditional',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderAddConditionalHoldingOK,
                    nrResponderAddConditionalOK
            ]
    ];

    private static Map nrResponderAnswerConditionalList = [
            code: ActionEventResultList.NR_RESPONDER_ANSWER_CONDITIONAL,
            description: 'The responder has said that they will supply the item(s) ',
            model: StateModel.MODEL_RESPONDER,
            results: [
                    nrResponderAnswerConditionalOK,
                    nrResponderAnswerConditionalHoldingOK
            ]
    ];

    private static Map nrResponderMarkConditionsAgreedList = [
            code: ActionEventResultList.NR_RESPONDER_MARK_CONDITIONS_AGREED,
            description: 'The responder has reported that the reqquester has agreed to the conditions',
            model: StateModel.MODEL_NR_RESPONDER,
            results: [
                    nrResponderMarkConditionsAgreedOK
            ]
    ];




    private static Map[] resultLists = [
            nrRequesterNewPatronRequestList,
            nrRequesterPatronRequestValidatedList,
            nrRequesterSendToNextLocationList,
            nrRequesterSentToSupplierISO18626List,
            nrRequesterExpectToSupplyISO18626List,
            nrRequesterConditionAnswerReceivedISO18626List,
            nrRequesterDeliveredList,
            //nrRequesterCompletedList,
            nrRequesterCancelList,
            nrRequesterBypassedValidationList,
            nrRequesterMarkEndOfRotaReviewedList,
            nrRequesterAgreeConditionsList,
            nrRequesterRejectConditionsList,
            nrRequesterNoStatusChangeList,
            nrRequesterRetriedValidationList,
            nrRequesterCloseManualList,
            nrRequesterNotificationReceivedISO18626List,
            nrRequesterCancelPendingISO18626List,
            nrResponderNewPatronRequestList,
            nrResponderAddConditionalList,
            nrResponderAnswerConditionalList,
            nrResponderAnswerYesList,
            nrResponderMarkConditionsAgreedList,
            nrResponderCannotSupplyList,
            nrResponderPrintPullslipList,
            nrResponderAddURLToDocumentList,
            nrResponderCancelRecievedISO18626,
            nrResponderNoStatusChangeList,
            nrResponderCloseManualList,
            nrResponderCancelList,
            nrResponderNotificationReceivedISO18626List
    ];

    // Initialization methods
    public static void loadStatusData() {
        Status.ensure(Status.PATRON_REQUEST_DOCUMENT_DELIVERED, StatusStage.COMPLETED, '10001', true, false, true, null);
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

        ActionEvent.ensure(Actions.ACTION_NONRETURNABLE_RESPONDER_ADD_CONDITION, 'The responder has added a loan condition',
                true, StateModel.MODEL_RESPONDER.capitalize() + Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION.capitalize(),
                ActionEventResultList.NR_RESPONDER_ADD_CONDITIONAL);




    }

    public static void loadStateModelData() {
        StateModel.ensure(StateModel.MODEL_NR_REQUESTER, null, Status.PATRON_REQUEST_IDLE, null, null, null, nrRequesterStates);
        StateModel.ensure(StateModel.MODEL_NR_RESPONDER, null, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY,
                null, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, nrResponderStates);
    }

    public static void loadAvailableActionData() {
        // To delete an unwanted action add State Model, State, Action to this array
        [
          [ StateModel.lookup(StateModel.MODEL_NR_RESPONDER).id, Status.lookup(Status.RESPONDER_IDLE).id, Actions.ACTION_RESPONDER_RESPOND_YES ],
          [ StateModel.lookup(StateModel.MODEL_NR_RESPONDER).id, Status.lookup(Status.RESPONDER_IDLE).id, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY ],
          [ StateModel.lookup(StateModel.MODEL_NR_RESPONDER).id, Status.lookup(Status.RESPONDER_NEW_AWAIT_PULL_SLIP).id, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP ],
          [ StateModel.lookup(StateModel.MODEL_NR_RESPONDER).id, Status.lookup(Status.RESPONDER_NEW_AWAIT_PULL_SLIP).id, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY ],
          [ StateModel.lookup(StateModel.MODEL_NR_RESPONDER).id, Status.lookup(Status.RESPONDER_COPY_AWAIT_PICKING).id, Actions.ACTION_RESPONDER_SUPPLIER_ADD_URL_TO_DOCUMENT ],
        ].each { availableActionToRemove ->
            log.info("Remove available action ${availableActionToRemove}");
            try {
                AvailableAction.executeUpdate(
                    '''
                                    delete from AvailableAction
                                    where aa_model = :model and aa_from_state = :status and aa_action_code = :action
                                 ''',
                        [model:availableActionToRemove[0], status:availableActionToRemove[1], action:availableActionToRemove[2]]);
            } catch (Exception e) {
                log.error("Unable to delete action ${availableActionToRemove} - ${e.message}", e);
            }
        }


        //REQ_IDLE
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_IDLE, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_CANCEL);

        //REQ_VALIDATED
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_VALIDATED, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_CANCEL);

        //REQ_BLANK_FORM_REVIEW
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_RETRY_VALIDATION, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_RETRIED_VALIDATION);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_CANCEL);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_BLANK_FORM_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_BYPASS_VALIDATION, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_BYPASSED_VALIDATION);

        //REQ_LOCAL_REVIEW
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_FILL_LOCALLY, AvailableAction.TRIGGER_TYPE_MANUAL, null, null, Boolean.TRUE, Boolean.TRUE)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_CANCEL_LOCAL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, Actions.ACTION_NONRETURNABLE_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        
        //REQ_INVALID_PATRON
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_CANCEL)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_NONRETURNABLE_REQUESTER_RETRY_VALIDATION, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_RETRIED_VALIDATION)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, Actions.ACTION_NONRETURNABLE_REQUESTER_BYPASS_VALIDATION, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_BYPASSED_VALIDATION)

        // REQ_CANCEL_PENDING OR "Cancel pending"
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING, Actions.ACTION_REQUESTER_ISO18626_CANCEL_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_CANCEL_PENDING_ISO18626);

        //REQ_SENT_TO_SUPPLIER
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_CANCEL);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_SENT_TO_SUPPLIER_ISO18626);

        //REQ_CONDITIONAL_ANSWER_RECEIVED
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS, AvailableAction.TRIGGER_TYPE_MANUAL, null, null, Boolean.TRUE, Boolean.TRUE)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626);


        //REQ_EXPECTS_TO_SUPPLY
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_CANCEL);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_CHANGE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, Actions.ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_EXPECT_TO_SUPPLY_ISO18626);

        //REQ_DOCUMENT_DELIVERED
        //AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_DOCUMENT_DELIVERED, Actions.ACTION_NONRETURNABLE_REQUESTER_COMPLETE_REQUEST, AvailableAction.TRIGGER_TYPE_MANUAL);

        //REQ_END_OF_ROTA
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA, Actions.ACTION_NONRETURNABLE_REQUESTER_MARK_END_OF_ROTA_REVIEWED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_MARK_END_OF_ROTA_REVIEWED);
        AvailableAction.ensure(StateModel.MODEL_NR_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA, Actions.ACTION_NONRETURNABLE_REQUESTER_REREQUEST, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_REREQUEST);

        //RES_IDLE
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_NONRETURNABLE_RESPONDER_RESPOND_YES, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_ANSWER_YES);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_CANNOT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_RESPONDER_CANCEL_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_ANSWER_CONDITIONAL);


        //RES_NEW_AWAIT_PULL_SLIP
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_PRINT_PULL_SLIP);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_CANNOT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_RESPONDER_CANCEL_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_ADD_CONDITIONAL);


        //RES_COPY_AWAIT_PICKING
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_COPY_AWAIT_PICKING, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_ADD_URL_TO_DOCUMENT, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_ADD_URL_TO_DOCUMENT, null, Boolean.TRUE, Boolean.TRUE);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_COPY_AWAIT_PICKING, Actions.ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_CANNOT_SUPPLY);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_COPY_AWAIT_PICKING, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_RESPONDER_CANCEL_RECEIVED_ISO18626);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_COPY_AWAIT_PICKING, Actions.ACTION_RESPONDER_SUPPLIER_ADD_CONDITION, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_ADD_CONDITIONAL);

        //RES_CANCEL_RECEIVED

        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_CANCEL, null, Boolean.TRUE, Boolean.TRUE);

        //RES_PENDING_CONDITIONAL_ANSWER
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_MARK_CONDITIONS_AGREED);
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, AvailableAction.TRIGGER_TYPE_MANUAL)
        AvailableAction.ensure(StateModel.MODEL_NR_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Actions.ACTION_RESPONDER_ISO18626_CANCEL, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.RESPONDER_CANCEL_RECEIVED_ISO18626);

        //messageAllSeen
        AvailableActionData.assignToAllStates(StateModel.MODEL_NR_REQUESTER, Actions.ACTION_MESSAGES_ALL_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.NR_REQUESTER_NO_STATUS_CHANGE);
        AvailableActionData.assignToAllStates(StateModel.MODEL_NR_RESPONDER, Actions.ACTION_MESSAGES_ALL_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.NR_RESPONDER_NO_STATUS_CHANGE);

        //messageSeen
        AvailableActionData.assignToAllStates(StateModel.MODEL_NR_REQUESTER, Actions.ACTION_MESSAGE_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.NR_REQUESTER_NO_STATUS_CHANGE);
        AvailableActionData.assignToAllStates(StateModel.MODEL_NR_RESPONDER, Actions.ACTION_MESSAGE_SEEN, AvailableAction.TRIGGER_TYPE_SYSTEM, ActionEventResultList.NR_RESPONDER_NO_STATUS_CHANGE);

        //message
        AvailableActionData.assignToCompletedAndActiveStates(StateModel.MODEL_NR_REQUESTER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_NO_STATUS_CHANGE);
        AvailableActionData.assignToCompletedAndActiveStates(StateModel.MODEL_NR_RESPONDER, Actions.ACTION_MESSAGE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_NO_STATUS_CHANGE);

        //localNote
        AvailableActionData.assignToAllStates(StateModel.MODEL_NR_RESPONDER, Actions.ACTION_RESPONDER_LOCAL_NOTE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.RESPONDER_NO_STATUS_CHANGE);

        //manualClose
        AvailableActionData.assignToNonTerminalStates(StateModel.MODEL_NR_REQUESTER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_REQUESTER_CLOSE_MANUAL);
        AvailableActionData.assignToNonTerminalStates(StateModel.MODEL_NR_RESPONDER, Actions.ACTION_MANUAL_CLOSE, AvailableAction.TRIGGER_TYPE_MANUAL, ActionEventResultList.NR_RESPONDER_CLOSE_MANUAL);

        //ISO18686Notification
        AvailableActionData.assignToCompletedAndActiveStates(StateModel.MODEL_NR_REQUESTER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_REQUESTER_NOTIFICATION_RECEIVED_ISO18626);
        AvailableActionData.assignToCompletedAndActiveStates(StateModel.MODEL_NR_RESPONDER, Actions.ACTION_ISO18626_NOTIFICATION, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_RESPONDER_NOTIFICATION_RECEIVED_ISO18626);

        //ISO18626StatusRequest
        AvailableActionData.assignToActiveStates(StateModel.MODEL_NR_RESPONDER, Actions.ACTION_RESPONDER_ISO18626_STATUS_REQUEST, AvailableAction.TRIGGER_TYPE_PROTOCOL, ActionEventResultList.NR_RESPONDER_NO_STATUS_CHANGE);

    }

    public static void loadAll() {
        loadStatusData();
        loadActionEventResultData();
        loadActionEventData();
        loadStateModelData();
        loadAvailableActionData();
    }

}