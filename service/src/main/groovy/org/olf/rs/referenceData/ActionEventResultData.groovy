package org.olf.rs.referenceData;

import org.olf.rs.ReferenceDataService;
import org.olf.rs.statemodel.ActionEventResult;
import org.olf.rs.statemodel.ActionEventResultList;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.refdata.RefdataValue

import groovy.util.logging.Slf4j

/**
 * Loads the ActionEvent data required for the system to process requests
 */
@Slf4j
public class ActionEventResultData {

    // We start with defining the individual results
    private static Map requesterISO18626Cancelled = [
        code: 'requesterISO18626Cancelled',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Cancelled',
        result: true,
        status: Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER,
        qualifier: 'Cancelled',
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterISO18626CancelNo = [
        code: 'requesterISO18626CancelNo',
        description: 'An incoming ISO-18626 message for the requester has said that we are not cancelling the request',
        result: true,
        status: Status.PATRON_REQUEST_CANCEL_PENDING,
        qualifier: ActionEventResultQualifier.QUALIFIER_NO,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Conditional = [
        code: 'requesterISO18626LoanConditional',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Conditional',
        result: true,
        status: Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
        qualifier: 'Conditional',
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterISO18626ExpectToSupply = [
        code: 'requesterISO18626ExpectToSupply',
        description: 'An incoming ISO-18626 message for the requester has said that the status is ExpectToSupply',
        result: true,
        status: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
        qualifier: 'ExpectToSupply',
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterISO18626LoanCompleted = [
        code: 'requesterISO18626LoanCompleted',
        description: 'An incoming ISO-18626 message for the requester has said that the status is LoanCompleted',
        result: true,
        status: Status.PATRON_REQUEST_REQUEST_COMPLETE,
        qualifier: 'LoanCompleted',
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Loaned = [
        code: 'requesterISO18626Loaned',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Loaned',
        result: true,
        status: Status.PATRON_REQUEST_SHIPPED,
        qualifier: 'Loaned',
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Overdue = [
        code: 'requesterISO18626Overdue',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Overdue',
        result: true,
        status: Status.PATRON_REQUEST_OVERDUE,
        qualifier: 'Overdue',
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Recalled = [
        code: 'requesterISO18626Recalled',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Recalled',
        result: true,
        status: Status.PATRON_REQUEST_RECALLED,
        qualifier: 'Recalled',
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Unfilled = [
        code: 'requesterISO18626Unfilled',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Unfilled',
        result: true,
        status: Status.PATRON_REQUEST_UNFILLED,
        qualifier: 'Unfilled',
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderISO18626AgreeConditions = [
        code: 'responderISO18626AgreeConditions',
        description: 'Requester has said they want to agree to the conditions',
        result: true,
        status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
        qualifier: ActionEventResultQualifier.QUALIFIER_CONDITIONS_AGREED,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
        fromStatus: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
        nextActionEvent: null
    ];

    private static Map responderISO18626Cancel = [
        code: 'responderISO18626Cancel',
        description: 'Requester has said they want to cancel the request',
        result: true,
        status: Status.RESPONDER_CANCEL_REQUEST_RECEIVED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderISO18626Received = [
        code: 'responderISO18626Received',
        description: 'Requester has said they have received the items',
        result: true,
        status: Status.RESPONDER_AWAITING_RETURN_SHIPPING,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderISO18626DhippedReturn = [
        code: 'responderISO18626DhippedReturn',
        description: 'Requester has said they have shipped the item(s) back to the responder',
        result: true,
        status: Status.RESPONDER_ITEM_RETURNED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    // The requester actions
    private static Map requesterAgreeConditionsOK = [
        code: 'requesterAgreeConditionsOK',
        description: 'Requester has agreed to the conditions',
        result: true,
        status: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterCancelOK = [
        code: 'requesterCancelOK',
        description: 'Requester has said they want to cancel the request',
        result: true,
        status: Status.PATRON_REQUEST_CANCEL_PENDING,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterManualCloseCancelledOK = [
        code: 'requesterManualCloseCancelledOK',
        description: 'Requester is closing this request as cancelled',
        result: true,
        status: Status.PATRON_REQUEST_CANCELLED,
        qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_CANCELLED,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterManualCloseCompleteOK = [
        code: 'requesterManualCloseCompleteOK',
        description: 'Requester is closing this request as completed',
        result: true,
        status: Status.PATRON_REQUEST_REQUEST_COMPLETE,
        qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_COMPLETE,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterManualCloseEndOfRotaOK = [
        code: 'requesterManualCloseEndOfRotaK',
        description: 'Requester is closing this request as end of rota',
        result: true,
        status: Status.PATRON_REQUEST_END_OF_ROTA,
        qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_END_OF_ROTA,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterManualCloseLocallyFilledOK = [
        code: 'requesterManualCloseLocallyFilledOK',
        description: 'Requester is closing this request as it has been filled locally',
        result: true,
        status: Status.PATRON_REQUEST_FILLED_LOCALLY,
        qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_FILLED_LOCALLY,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterManualCloseFailure = [
        code: 'requesterManualCloseFailure',
        description: 'Incorrect parameters were passed in to close the request',
        result: false,
        status: null,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterPatronReturnedOK = [
        code: 'requesterPatronReturnedOK',
        description: 'Patron has returned the item(s) to the requester',
        result: true,
        status: Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterRejectConditionsOK = [
        code: 'requesterRejectConditionsOK',
        description: 'Requester has rejected the conditions from the responder',
        result: true,
        status: Status.PATRON_REQUEST_CANCEL_PENDING,
        qualifier: null,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
        nextActionEvent: null
    ];

    private static Map requesterReceivedOK = [
        code: 'requesterReceivedOK',
        description: 'Requester has received the item(s) from the responder',
        result: true,
        status: Status.PATRON_REQUEST_CHECKED_IN,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterShippedReturnOK = [
        code: 'requesterShippedReturnOK',
        description: 'Requester has sent the the item(s) to the responder',
        result: true,
        status: Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    // The responder actions
    private static Map responderAnwserYesOK = [
        code: 'responderAnwserYesOK',
        description: 'Responder has said that they will supply',
        result: true,
        status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderAnwserConditionalHoldingOK = [
        code: 'responderAnwserConditionalHoldingOK',
        description: 'Responder has responded with a conditional and placed the request on hold',
        result: true,
        status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
        qualifier: ActionEventResultQualifier.QUALIFIER_HOLDING,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
        overrideSaveStatus: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        nextActionEvent: null
    ];

    private static Map responderAnwserConditionalOK = [
        code: 'responderAnwserConditionalOK',
        description: 'Responder has responded with a conditional without placing the request on hold',
        result: true,
        status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderCancelNoOK = [
        code: 'responderCancelNoOK',
        description: 'Responder is denying the cancel request',
        result: true,
        status: Status.RESPONDER_CANCEL_REQUEST_RECEIVED,
        qualifier: ActionEventResultQualifier.QUALIFIER_NO,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
        nextActionEvent: null
    ];

    private static Map responderCancelOK = [
        code: 'responderCancelOK',
        description: 'Responder is accepting the cancel request',
        result: true,
        status: Status.RESPONDER_CANCELLED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderCheckInToReshareOK = [
        code: 'responderCheckInToReshareOK',
        description: 'Responder has successfully checked the items out of the LMS into reshare',
        result: true,
        status: Status.RESPONDER_AWAIT_SHIP,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderCheckInToReshareFailure = [
        code: 'responderCheckInToReshareFailure',
        description: 'Responder has failed to check out 1 or more items out of the LMS into reshare',
        result: false,
        status: Status.RESPONDER_AWAIT_PICKING,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderManualCloseCancelledOK = [
        code: 'responderManualCloseCancelledOK',
        description: 'Responder is closing this request as being cancelled',
        result: true,
        status: Status.RESPONDER_CANCELLED,
        qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_RESP_CANCELLED,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderManualCloseCompleteOK = [
        code: 'responderManualCloseCompleteOK',
        description: 'Responder is closing this request as completed',
        result: true,
        status: Status.RESPONDER_COMPLETE,
        qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_RESP_COMPLETE,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderManualCloseNotSuppliedOK = [
        code: 'responderManualCloseNotSuppliedOK',
        description: 'Responder is closing this request as not supplied',
        result: true,
        status: Status.RESPONDER_NOT_SUPPLIED,
        qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_NOT_SUPPLIED,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderManualCloseUnfilledOK = [
        code: 'responderManualCloseUnfilledOK',
        description: 'Responder is closing this request as unfilled',
        result: true,
        status: Status.RESPONDER_UNFILLED,
        qualifier: ActionEventResultQualifier.QUALIFIER_CLOSE_UNFILLED,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderManualCloseFailure = [
        code: 'responderManualCloseFailure',
        description: 'Incorrect parameters passed to manual close',
        result: false,
        status: null,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderItemReturnedOK = [
        code: 'responderItemReturnedOK',
        description: 'The requester has returned the itrm)s) to the supplier successfully',
        result: true,
        status: Status.RESPONDER_COMPLETE,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderMarkConditionsAgreedOK = [
        code: 'responderMarkConditionsAgreedOK',
        description: 'Responder is saying that the requester has agreed to the conditions placed on the request',
        result: true,
        status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
        qualifier: null,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
        nextActionEvent: null
    ];

    private static Map responderMarkShippedOK = [
        code: 'responderMarkShippedOK',
        description: 'Responder is saying that the item(s) have been shipped',
        result: true,
        status: Status.RESPONDER_ITEM_SHIPPED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderPullSlipPrintedOK = [
        code: 'responderPullSlipPrintedOK',
        description: 'Responder has printed the pull slip',
        result: true,
        status: Status.RESPONDER_AWAIT_PICKING,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    // One for both requester and responder, where we do not change status
    private static Map defaultNoStatusChangeOK = [
        code: 'defaultNoStatusChangeOK',
        description: 'Default scenario, status is not changing',
        result: true,
        status: null,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    // Now we can define the lists
    private static Map requesterAwaitingReturnShippingISO18626 = [
        code: ActionEventResultList.REQUESTER_AWAITING_RETURN_SHIPPING_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626LoanCompleted,
            requesterISO18626Overdue,
            requesterISO18626Recalled
        ]
    ];

    private static Map requesterBorrowerReturnedISO18626 = [
        code: ActionEventResultList.REQUESTER_BORROWER_RETURNED_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_BORROWER_RETURNED,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Overdue,
            requesterISO18626Recalled
        ]
    ];

    private static Map requesterBorrowingLibraryReceivedISO18626 = [
        code: ActionEventResultList.REQUESTER_BORROWING_LIBRARY_RECEIVED_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Overdue,
            requesterISO18626Recalled
        ]
    ];

    private static Map requesterCancelPendingISO18626 = [
        code: ActionEventResultList.REQUESTER_CANCEL_PENDING_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_CANCEL_PENDING,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Cancelled,
            requesterISO18626CancelNo,
            requesterISO18626Loaned
        ]
    ];

    private static Map requesterCheckedInISO18626 = [
        code: ActionEventResultList.REQUESTER_CHECKED_IN_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_CHECKED_IN,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Overdue,
            requesterISO18626Recalled
        ]
    ];

    private static Map requesterConditionalAnswerReceivedISO18626 = [
        code: ActionEventResultList.REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Loaned
        ]
    ];

    private static Map requesterExpectToSupplyISO18626 = [
        code: ActionEventResultList.REQUESTER_EXPECTS_TO_SUPPLY_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Conditional,
            requesterISO18626Loaned,
            requesterISO18626Unfilled
        ]
    ];

    private static Map requesterOverdueISO18626 = [
        code: ActionEventResultList.REQUESTER_OVERDUE_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_OVERDUE,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626LoanCompleted,
            requesterISO18626Overdue,
            requesterISO18626Recalled
        ]
    ];

    private static Map requesterRecalledISO18626 = [
        code: ActionEventResultList.REQUESTER_RECALLED_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_RECALLED,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626LoanCompleted,
            requesterISO18626Overdue,
            requesterISO18626Recalled
        ]
    ];

    private static Map requesterSentToSupplierISO18626 = [
        code: ActionEventResultList.REQUESTER_SENT_TO_SUPPLIER_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Conditional,
            requesterISO18626ExpectToSupply,
            requesterISO18626Unfilled
        ]
    ];

    private static Map requesterShippedISO18626 = [
        code: ActionEventResultList.REQUESTER_SHIPPED_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_SHIPPED,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Overdue,
            requesterISO18626Recalled
        ]
    ];

    private static Map requesterShippedToSupplierISO18626 = [
        code: ActionEventResultList.REQUESTER_SHIPPED_TO_SUPPLIER_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626LoanCompleted,
            requesterISO18626Overdue,
            requesterISO18626Recalled
        ]
    ];

    private static Map responderNotificationReceivedISO18626 = [
        code: ActionEventResultList.RESPONDER_NOTIFICATION_RECEIVED_ISO18626,
        description: 'An incoming ISO18626 notifications has been received by the supplier',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderISO18626AgreeConditions,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map responderCancelReceivedISO18626 = [
        code: ActionEventResultList.RESPONDER_CANCEL_RECEIVED_ISO18626,
        description: 'An incoming ISO18626 Cancel has been received by the supplier',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderISO18626Cancel
        ]
    ];

    private static Map responderReceivedISO18626 = [
        code: ActionEventResultList.RESPONDER_RECEIVED_ISO18626,
        description: 'An incoming ISO18626 message has been received to inform us the requester has received the item(s)',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderISO18626Received
        ]
    ];

    private static Map responderShippedReturnISO18626 = [
        code: ActionEventResultList.RESPONDER_SHIPPED_RETURN_ISO18626,
        description: 'An incoming ISO18626 message has been received to inform us the requester has returned the item(s)',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderISO18626DhippedReturn
        ]
    ];

    private static Map requesterAgreeConditionsList = [
        code: ActionEventResultList.REQUESTER_AGREE_CONDITIONS,
        description: 'Requester has agreed to the conditions',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterAgreeConditionsOK
        ]
    ];

    private static Map requesterCancelList = [
        code: ActionEventResultList.REQUESTER_CANCEL,
        description: 'Requester has that the request be cancelled',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterCancelOK
        ]
    ];

    private static Map requesterCloseManualList = [
        code: ActionEventResultList.REQUESTER_CLOSE_MANUAL,
        description: 'Requester is manually closing down the request',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterManualCloseCancelledOK,
            requesterManualCloseCompleteOK,
            requesterManualCloseEndOfRotaOK,
            requesterManualCloseLocallyFilledOK,
            requesterManualCloseFailure
        ]
    ];

    private static Map requesterNoStatusChangeList = [
        code: ActionEventResultList.REQUESTER_NO_STATUS_CHANGE,
        description: 'Default for when we do not have a status change for the requester',
        model: StateModel.MODEL_REQUESTER,
        results: [
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterPatronReturnedList = [
        code: ActionEventResultList.REQUESTER_PATRON_RETURNED,
        description: 'Requester has received the item',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterPatronReturnedOK
        ]
    ];

    private static Map requesterReceivedList = [
        code: ActionEventResultList.REQUESTER_RECEIVED,
        description: 'Requester has received the item',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterReceivedOK
        ]
    ];

    private static Map requesterRejectConditionsList = [
        code: ActionEventResultList.REQUESTER_REJECT_CONDITIONS,
        description: 'Requester has rejected the conditions',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterRejectConditionsOK
        ]
    ];

    private static Map requesterShippedReturnList = [
        code: ActionEventResultList.REQUESTER_SHIPPED_RETURN,
        description: 'Requester has received the item',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterShippedReturnOK
        ]
    ];

    private static Map responderAnswerYesList = [
        code: ActionEventResultList.RESPONDER_ANWSER_YES,
        description: 'The responder has said that they will supply the item(s) ',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderAnwserYesOK
        ]
    ];

    private static Map responderAnswerConditionalList = [
        code: ActionEventResultList.RESPONDER_ANWSER_CONDITIONAL,
        description: 'The responder has said that they will supply the item(s) ',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderAnwserConditionalOK,
            responderAnwserConditionalHoldingOK
        ]
    ];

    private static Map responderCancelList = [
        code: ActionEventResultList.RESPONDER_CANCEL,
        description: 'The responder has reported that the reqquester has agreed to the conditions',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderCancelOK,
            responderCancelNoOK
        ]
    ];

    private static Map responderCheckInToReshareList = [
        code: ActionEventResultList.RESPONDER_CHECK_INTO_RESHARE,
        description: 'The responder has said that they will supply the item(s) ',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderCheckInToReshareFailure,
            responderCheckInToReshareOK
        ]
    ];

    private static Map responderCloseManualList = [
        code: ActionEventResultList.RESPONDER_CLOSE_MANUAL,
        description: 'The responder is terminating this request',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderManualCloseCancelledOK,
            responderManualCloseCompleteOK,
            responderManualCloseNotSuppliedOK,
            responderManualCloseUnfilledOK,
            responderManualCloseFailure
        ]
    ];

    private static Map responderItemReturnedList = [
        code: ActionEventResultList.RESPONDER_ITEM_RETURNED,
        description: 'Requester has returned the item(s) to the responder',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderItemReturnedOK
        ]
    ];

    private static Map responderMarkConditionsAgreedList = [
        code: ActionEventResultList.RESPONDER_MARK_CONDITIONS_AGREED,
        description: 'The responder has reported that the reqquester has agreed to the conditions',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderMarkConditionsAgreedOK
        ]
    ];

    private static Map responderMarkShippedList = [
        code: ActionEventResultList.RESPONDER_MARK_SHIPPED,
        description: 'The responder has printed the pull slip',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderMarkShippedOK
        ]
    ];

    private static Map responderPrintPullSlipList = [
        code: ActionEventResultList.RESPONDER_PRINT_PULL_SLIP,
        description: 'The responder has printed the pull slip',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderPullSlipPrintedOK
        ]
    ];

    private static Map responderNoStatusChangeList = [
        code: ActionEventResultList.RESPONDER_NO_STATUS_CHANGE,
        description: 'Default for when we do not have a status change for the responder',
        model: StateModel.MODEL_RESPONDER,
        results: [
            defaultNoStatusChangeOK
        ]
    ];

    // Now specify all the ones we are going to load
    private static Map[] allResultLists = [
        // Requester lists
        requesterAgreeConditionsList,
        requesterCancelList,
        requesterCloseManualList,
        requesterNoStatusChangeList,
        requesterPatronReturnedList,
        requesterReceivedList,
        requesterRejectConditionsList,
        requesterShippedReturnList,

        // Responder lists
        responderAnswerConditionalList,
        responderAnswerYesList,
        responderCancelList,
        responderCheckInToReshareList,
        responderCloseManualList,
        responderItemReturnedList,
        responderMarkConditionsAgreedList,
        responderMarkShippedList,
        responderNoStatusChangeList,
        responderPrintPullSlipList,

        // ISO18626 lists
        requesterAwaitingReturnShippingISO18626,
        requesterBorrowerReturnedISO18626,
        requesterBorrowingLibraryReceivedISO18626,
        requesterCancelPendingISO18626,
        requesterCheckedInISO18626,
        requesterConditionalAnswerReceivedISO18626,
        requesterExpectToSupplyISO18626,
        requesterOverdueISO18626,
        requesterRecalledISO18626,
        requesterSentToSupplierISO18626,
        requesterShippedISO18626,
        requesterShippedToSupplierISO18626,
        responderCancelReceivedISO18626,
        responderNotificationReceivedISO18626,
        responderReceivedISO18626,
        responderShippedReturnISO18626
    ];

	public void load() {
		log.info("Adding action and event result lists to the database");

        // We are not a service, so we need to look it up
        ReferenceDataService referenceDataService = ReferenceDataService.getInstance();

        allResultLists.each { resultList ->
            // Process all the possible outcomes for this result list
            List<ActionEventResult> resultItems = new ArrayList<ActionEventResult>();
            resultList.results.each { result ->
                // Lookup the save / restore state
                RefdataValue saveRestoreState = referenceDataService.lookup(RefdataValueData.VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, result.saveRestoreState);

                // We need to lookup the status
                resultItems.add(
                    ActionEventResult.ensure(
                        result.code,
                        result.description,
                        result.result,
                        Status.lookup(resultList.model, result.status),
                        result.qualifier,
                        saveRestoreState,
                        Status.lookup(resultList.model, result.overrideSaveStatus),
                        Status.lookup(resultList.model, result.fromStatus),
                        result.nextActionEvent
                    )
                );
            }

            // Now create the result list
            ActionEventResultList.ensure(resultList.code, resultList.description, resultItems);
        }
	}

	public static void loadAll() {
		(new ActionEventResultData()).load();
	}
}
