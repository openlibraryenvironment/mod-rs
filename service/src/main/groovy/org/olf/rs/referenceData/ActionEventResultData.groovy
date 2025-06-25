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
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626CancelNo = [
        code: 'requesterISO18626CancelNo',
        description: 'An incoming ISO-18626 message for the requester has said that we are not cancelling the request',
        result: true,
        status: Status.PATRON_REQUEST_CANCEL_PENDING,
        qualifier: ActionEventResultQualifier.QUALIFIER_NO,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Conditional = [
        code: 'requesterISO18626Conditional',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Conditional',
        result: true,
        status: Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
        qualifier: ActionEventResultQualifier.QUALIFIER_CONDITIONAL,
        saveRestoreState: null,
        fromStatus: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626NotificationConditionalExpectToSupply = [
        code: 'requesterISO18626Conditional',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Conditional',
        result: true,
        status: Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
        qualifier: ActionEventResultQualifier.QUALIFIER_CONDITIONAL,
//        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
        saveRestoreState: null,
        fromStatus: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    /*
        This is a little bit odd. When we get a "Will Supply" message, we transition to "Expects to Supply."
        When we get "Expects to Supply" we do not change state.
     */

    private static Map requesterISO18626ExpectToSupply = [
        code: 'requesterISO18626ExpectToSupply',
        description: 'An incoming ISO-18626 message for the requester has said that the status is ExpectToSupply',
        result: true,
        status: null,
        qualifier: ActionEventResultQualifier.QUALIFIER_EXPECT_TO_SUPPLY,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626WillSupply = [
        code: 'requesterISO18626WillSupply',
        description: 'An incoming ISO-18626 message for the requester has said that the status is WillSupply',
        result: true,
        status: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, //See comment above
        qualifier: ActionEventResultQualifier.QUALIFIER_WILL_SUPPLY,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626LoanCompleted = [
        code: 'requesterISO18626LoanCompleted',
        description: 'An incoming ISO-18626 message for the requester has said that the status is LoanCompleted',
        result: true,
        status: Status.PATRON_REQUEST_REQUEST_COMPLETE,
        qualifier: 'LoanCompleted',
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Loaned = [
        code: 'requesterISO18626Loaned',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Loaned',
        result: true,
        status: Status.PATRON_REQUEST_SHIPPED,
        qualifier: ActionEventResultQualifier.QUALIFIER_LOANED,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Overdue = [
        code: 'requesterISO18626Overdue',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Overdue',
        result: true,
        status: Status.PATRON_REQUEST_OVERDUE,
        qualifier: 'Overdue',
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Recalled = [
        code: 'requesterISO18626Recalled',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Recalled',
        result: true,
        status: Status.PATRON_REQUEST_RECALLED,
        qualifier: 'Recalled',
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626Unfilled = [
        code: 'requesterISO18626Unfilled',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Unfilled',
        result: true,
        status: Status.PATRON_REQUEST_UNFILLED,
        qualifier: 'Unfilled',
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterISO18626UnfilledTransfer = [
        code: 'requesterISO18626UnfilledTransfer',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Unfilled, but the reason is "transfer"',
        result: true,
        status: Status.PATRON_REQUEST_REREQUESTED,
        qualifier: 'UnfilledTransfer',
        saveRestoreState: null,
        updateRotaLocation: true,
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
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map responderISO18626Cancel = [
        code: 'responderISO18626Cancel',
        result: true,
        description: 'Requester has said they want to cancel the request',
        status: Status.RESPONDER_CANCEL_REQUEST_RECEIVED,
        qualifier: null,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
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

    private static Map requesterBorrowerCheckOK = [
        code: 'requesterBorrowerCheckOK',
        description: 'The borrower id has been verified',
        result: true,
        status: Status.PATRON_REQUEST_VALIDATED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterBorrowerCheckInvalidPatronError = [
        code: 'requesterBorrowerCheckInvalidPatronError',
        description: 'The borrower id is not valid',
        result: false,
        status: Status.PATRON_REQUEST_INVALID_PATRON,
        qualifier: ActionEventResultQualifier.QUALIFIER_INVALID_PATRON,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterBorrowerCheckHostLMSCallFailedError = [
        code: 'requesterBorrowerCheckHostLMSCallFailedError',
        description: 'Failed to talk to the host LMS to validate the patron',
        result: false,
        status: null,
        qualifier: ActionEventResultQualifier.QUALIFIER_HOST_LMS_CALL_FAILED,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterCancelOK = [
        code: 'requesterCancelOK',
        description: 'Requester has said they want to cancel the request',
        result: true,
        status: Status.PATRON_REQUEST_CANCEL_PENDING,
        qualifier: null,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
        nextActionEvent: null
    ];

    private static Map requesterCancelOKNoSupplier = [
        code: 'requesterCancelOKNoSupplier',
        description: 'Requester has said they want to cancel the request, but there is no supplier',
        result: true,
        status: Status.PATRON_REQUEST_CANCELLED,
        qualifier: ActionEventResultQualifier.QUALIFIER_NO_SUPPLIER,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterCancelLocalOK = [
        code: 'requesterCancelLocalOK',
        description: 'Requester is cancelling the request for a local item',
        result: true,
        status: Status.PATRON_REQUEST_CANCELLED,
        qualifier: null,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterCancelDuplicateOK = [
        code: 'requesterCancelDuplicateOK',
        description: 'Requester is cancelling the duplicate request',
        result: true,
        status: Status.PATRON_REQUEST_CANCELLED,
        qualifier: null,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ]


    private static Map requesterFillLocallyOK = [
        code: 'requesterFillLocallyOK',
        description: 'Requester is is fulfilling the request locally',
        result: true,
        status: Status.PATRON_REQUEST_FILLED_LOCALLY,
        qualifier: null,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterLocalCannotSupplyOK = [
        code: 'requesterLocalCannotSupplyOK',
        description: 'Local supplier cannot supply',
        result: true,
        status: Status.PATRON_REQUEST_UNFILLED,
        qualifier: null,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterLocalCannotSupplyContinue = [
        code: 'requesterLocalCannotSupplyContinue',
        description: 'Local supplier cannot supply continue with next supplier',
        result: true,
        status: Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
        qualifier: ActionEventResultQualifier.QUALIFIER_CONTINUE,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterManualCheckInOK = [
        code: 'requesterManualCheckInOK',
        description: 'Requester has manually checked the item(s) into the local LMS',
        result: true,
        status: Status.PATRON_REQUEST_CHECKED_IN,
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

    private static Map requesterPatronReturnedShippedOK = [
        code: 'requesterPatronReturnedShippedOK',
        description: 'The patron returned the item(s) and they have been shipped back to the responder',
        result: true,
        status: Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterPatronReturnedShippedShipItemFailure = [
        code: 'requesterPatronReturnedShippedShipItemFailure',
        description: 'The patron returned the item(s) but then we failed to return it to the supplier',
        result: false,
        status: Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING,
        qualifier: ActionEventResultQualifier.QUALIFIER_SHIP_ITEM,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterPatronReturnedShippedFailure = [
        code: 'requesterPatronReturnedShippedFailure',
        description: 'We failed to receive the item back from the patron',
        result: false,
        status: null,
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

    private static Map requesterRerequestCancelledOK = [
        code: 'requesterRerequestCancelledOK',
        description: 'Re-requested with changes',
        result: true,
        status: null,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterRerequestEndOfRotaOK = [
        code: 'requesterRerequestEndOfRotaOK',
        description: 'Re-requested with changes',
        result: true,
        status: Status.PATRON_REQUEST_END_OF_ROTA_REVIEWED,
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

    // Requester Events
    private static Map requesterCancelledWithSupplierIndOK = [
        code: 'requesterCancelledWithSupplierIndOK',
        description: 'Event triggered by a change of state to Cancelled with supplier',
        result: true,
        status: Status.PATRON_REQUEST_CANCELLED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterCancelledWithSupplierIndOKcontinue = [
        code: 'requesterCancelledWithSupplierIndOKcontinue',
        description: 'Event triggered by a change of state to Cancelled with supplier but we want to continue with next supplier',
        result: true,
        status: Status.PATRON_REQUEST_UNFILLED,
        qualifier: ActionEventResultQualifier.QUALIFIER_CONTINUE,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterMarkEndOfRotaReviewed = [
        code: 'requesterMarkEndOfRotaReviewed',
        description: 'Marked a request at the end of rota as reviewed',
        result: true,
        status: Status.PATRON_REQUEST_END_OF_ROTA_REVIEWED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterNewPatronRequestRetry = [
        code: 'requesterNewPatronRequestRetry',
        description: 'Retry a patron request as if new',
        result: true,
        status: Status.PATRON_REQUEST_IDLE,
        qualifer: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterNewPatronRequestIndOK = [
        code: 'requesterNewPatronRequestIndOK',
        description: 'Event triggered by a new patron request',
        result: true,
        status: Status.PATRON_REQUEST_VALIDATED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterNewPatronRequestIndOKnoInstitutionSymbol = [
        code: 'requesterNewPatronRequestIndOKnoInstitutionSymbol',
        description: 'Event triggered by a new patron request, but there is no institution symbol',
        result: true,
        status: Status.PATRON_REQUEST_ERROR,
        qualifier: ActionEventResultQualifier.QUALIFIER_NO_INSTITUTION_SYMBOL,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterNewPatronRequestIndOKinvalidPatron = [
        code: 'requesterNewPatronRequestIndOKinvalidPatron',
        description: 'Event triggered by a new patron request, but the patron is invalid',
        result: true,
        status: Status.PATRON_REQUEST_INVALID_PATRON,
        qualifier: ActionEventResultQualifier.QUALIFIER_INVALID_PATRON,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterNewPatronRequestIndOKhostLMSCallFailed = [
        code: 'requesterNewPatronRequestIndOKhostLMSCallFailed',
        description: 'Failed to talk to the host LMS to validate the patron',
        status: Status.PATRON_REQUEST_INVALID_PATRON,
        qualifier: ActionEventResultQualifier.QUALIFIER_HOST_LMS_CALL_FAILED,
        result: true,
        saveRestoreState: null,
        nextActionEvent: null
    ];


    private static Map requesterNewPatronRequestIndOKDuplicateReview = [
        code: 'requesterNewPatronRequestIndOKDuplicateReview',
        description: 'New request flagged as possible duplicate, needs review',
        result: true,
        status: Status.PATRON_REQUEST_DUPLICATE_REVIEW,
        qualifier: ActionEventResultQualifier.QUALIFIER_DUPLICATE_REVIEW,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterNewPatronRequestIndOKBlankFormReview = [
        code: 'requesterNewPatronRequestIndOKBlankFormReview',
        description: 'New request flagged as not having cluster ID, needs review',
        result: true,
        status: Status.PATRON_REQUEST_BLANK_FORM_REVIEW,
        qualifier: ActionEventResultQualifier.QUALIFIER_BLANK_FORM_REVIEW,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterNewPatronRequestIndOverLimit = [
      code: 'requesterNewPatronRequestIndOverLimit',
      description: 'New request exceeds request limit for this patron',
      result: true,
      status: Status.PATRON_REQUEST_OVER_LIMIT,
      qualifier: ActionEventResultQualifier.QUALIFIER_OVER_LIMIT,
      saveRestoreState: null,
      nextActionEvent: null
    ];

    private static Map requesterRequestSentToSupplierOK = [
        code: 'requesterRequestSentToSupplierOK',
        description: 'Request has been sent to supplier',
        result: true,
        status: null,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterRequestSentToSupplierLocalReview = [
        code: 'requesterRequestSentToSupplierFillLocally',
        description: 'Request to be filled locally',
        result: true,
        status: Status.PATRON_REQUEST_LOCAL_REVIEW,
        qualifier: ActionEventResultQualifier.QUALIFIER_LOCAL_REVIEW,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterValidateIndIndOK = [
        code: 'requesterValidateIndIndOK',
        description: 'Event triggered by a status change to Validate',
        result: true,
        status: Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterValidateIndIndOKsourcing = [
        code: 'requesterValidateIndIndOKsourcing',
        description: 'Event triggered by a status change to Validate and the item is being sourced',
        result: true,
        status: Status.PATRON_REQUEST_SOURCING_ITEM,
        qualifier: ActionEventResultQualifier.QUALIFIER_SOURCING,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterValidateIndIndOKendOfRota = [
        code: 'requesterValidateIndIndOKendOfRota',
        description: 'Event triggered by a status change to Validate nut no locations were found',
        result: true,
        status: Status.PATRON_REQUEST_END_OF_ROTA,
        qualifier: ActionEventResultQualifier.QUALIFIER_END_OF_ROTA,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map requesterSendToNextLocationOK = [
        code: 'requesterSendToNextLocationOK',
        description: 'Event triggered by a status that sends to the next location',
        result: true,
        status: Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
        qualifier: null,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterSendToNextLocationOKlocalReview = [
        code: 'requesterSendToNextLocationOKlocalReview',
        description: 'Event triggered by a status that sends to the next location, but now needs to goto local review',
        result: true,
        status: Status.PATRON_REQUEST_LOCAL_REVIEW,
        qualifier: ActionEventResultQualifier.QUALIFIER_LOCAL_REVIEW,
        saveRestoreState: null,
        updateRotaLocation: true,
        nextActionEvent: null
    ];

    private static Map requesterSendToNextLocationOKendOfRota = [
        code: 'requesterSendToNextLocationOKendOfRota',
        description: 'Event triggered by a status that sends to the next location, but now at end of rota',
        result: true,
        status: Status.PATRON_REQUEST_END_OF_ROTA,
        qualifier: ActionEventResultQualifier.QUALIFIER_END_OF_ROTA,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    // The responder actions
    private static Map responderAddConditionalHoldingOK = [
        code: 'responderAddConditionalHoldingOK',
        description: 'Responder has added a conditional to the request placing the request on hold',
        result: true,
        status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
        qualifier: ActionEventResultQualifier.QUALIFIER_HOLDING,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
        overrideSaveStatus: null,
        nextActionEvent: null
    ];

    private static Map responderAddConditionalOK = [
        code: 'responderAddConditionalOK',
        description: 'Responder has added a conditional without placing the request on hold',
        result: true,
        status: null,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderAnswerYesOK = [
        code: 'responderAnswerYesOK',
        description: 'Responder has said that they will supply',
        result: true,
        status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderAnswerConditionalHoldingOK = [
        code: 'responderAnswerConditionalHoldingOK',
        description: 'Responder has responded with a conditional and placed the request on hold',
        result: true,
        status: Status.RESPONDER_PENDING_CONDITIONAL_ANSWER,
        qualifier: ActionEventResultQualifier.QUALIFIER_HOLDING,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE,
        overrideSaveStatus: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        nextActionEvent: null
    ];

    private static Map responderAnswerConditionalOK = [
        code: 'responderAnswerConditionalOK',
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

    private static Map responderCannotSupplyOK = [
        code: 'responderCannotSupplyOK',
        description: 'Responder is unable to supply the items',
        result: true,
        status: Status.RESPONDER_UNFILLED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderCheckInAndShipOK = [
        code: 'responderCheckInAndShipOK',
        description: 'Responder has successfully checked the item(s) out of the LMS into reshare and shipped the item',
        result: true,
        status: Status.RESPONDER_ITEM_SHIPPED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderCheckInAndShipFailure = [
        code: 'responderCheckInAndShipFailure',
        description: 'Responder has failed to check the item(s) out of the LMS',
        result: false,
        status: Status.RESPONDER_AWAIT_PICKING,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderCheckInAndShipCheckedInFailure = [
        code: 'responderCheckInAndShipCheckedInFailure',
        description: 'Responder has successfully checked the item(s) into reshare but failed to ship the item(s)',
        result: false,
        status: Status.RESPONDER_AWAIT_SHIP,
        qualifier: ActionEventResultQualifier.QUALIFIER_CHECKED_IN,
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

    private static Map responderManualCheckOutOK = [
        code: 'responderManualCheckOutOK',
        description: 'Responder has manually checked it out from the LMS',
        result: false,
        status: Status.RESPONDER_AWAIT_SHIP,
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
        description: 'The requester has returned the items to the supplier successfully',
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


    // Responder Events
    private static Map responderCancelRequestReceivedIndOK = [
        code: 'responderCancelRequestReceivedIndOK',
        description: 'Event triggered by a new cancel request being received',
        result: true,
        status: null, // No state change
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderCancelRequestReceivedIndOKcancelled = [
        code: 'responderCancelRequestReceivedIndOKcancelled',
        description: 'Event triggered by a new cancel request being received and autorespond is set to Yes',
        result: true,
        status: Status.RESPONDER_CANCELLED,
        qualifier: ActionEventResultQualifier.QUALIFIER_CANCELLED,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderCancelRequestReceivedIndOKshipped = [
        code: 'responderCancelRequestReceivedIndOKshipped',
        description: 'Event triggered by a new cancel request being received and autorespond is set to Yes but the item has been shipped',
        result: true,
        status: Status.RESPONDER_CANCEL_REQUEST_RECEIVED,
        qualifier: ActionEventResultQualifier.QUALIFIER_SHIPPED,
        saveRestoreState: RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE,
        nextActionEvent: null
    ];

    private static Map responderCheckedIntoReshareIndOK = [
        code: 'responderCheckedIntoReshareIndOK',
        description: 'Status change to Checked into Reshare',
        result: true,
        status: Status.RESPONDER_AWAIT_SHIP,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderNewPatronRequestIndOK = [
        code: 'responderNewPatronRequestIndOK',
        description: 'Event triggered by a new incoming request',
        result: true,
        status: Status.RESPONDER_IDLE,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderNewPatronRequestIndOKlocated = [
        code: 'responderNewPatronRequestIndOKlocated',
        description: 'Event triggered by a new incoming request and the item has been located',
        result: true,
        status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        qualifier: ActionEventResultQualifier.QUALIFIER_LOCATED,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderNewPatronRequestIndOKRequestItem = [
        code: 'responderNewPatronRequestIndOKRequestItem',
        description: 'Event triggered by a new incoming request, the item has been located and we are configured to use request item',
        result: true,
        status: Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        qualifier: ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM,
        saveRestoreState: null,
        nextActionEvent: null
    ];

    private static Map responderNewPatronRequestIndOKunfilled = [
        code: 'responderNewPatronRequestIndOKunfilled',
        description: 'Event triggered by a new incoming request and the item was not found',
        result: true,
        status: Status.RESPONDER_UNFILLED,
        qualifier: ActionEventResultQualifier.QUALIFIER_UNFILLED,
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
            requesterISO18626Recalled,
            defaultNoStatusChangeOK
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
            requesterISO18626Recalled,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterConditionalAnswerReceivedISO18626 = [
        code: ActionEventResultList.REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Loaned,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterExpectToSupplyISO18626 = [
        code: ActionEventResultList.REQUESTER_EXPECTS_TO_SUPPLY_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Conditional,
            requesterISO18626Loaned,
            requesterISO18626Unfilled,
            requesterISO18626UnfilledTransfer,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterNotificationReceivedISO18626 = [
        code: ActionEventResultList.REQUESTER_NOTIFICATION_RECEIVED_ISO18626,
        description: 'An incoming ISO18626 notifications has been received by the requester',
        model: StateModel.MODEL_RESPONDER,
        results: [
            requesterISO18626NotificationConditionalExpectToSupply,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterOverdueISO18626 = [
        code: ActionEventResultList.REQUESTER_OVERDUE_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_OVERDUE,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626LoanCompleted,
            requesterISO18626Overdue,
            requesterISO18626Recalled,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterRecalledISO18626 = [
        code: ActionEventResultList.REQUESTER_RECALLED_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_RECALLED,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626LoanCompleted,
            requesterISO18626Overdue,
            requesterISO18626Recalled,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterSentToSupplierISO18626 = [
        code: ActionEventResultList.REQUESTER_SENT_TO_SUPPLIER_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Conditional,
            requesterISO18626ExpectToSupply,
            requesterISO18626WillSupply,
            requesterISO18626Unfilled,
            requesterISO18626UnfilledTransfer,
            requesterISO18626Loaned,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterShippedISO18626 = [
        code: ActionEventResultList.REQUESTER_SHIPPED_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_SHIPPED,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626Overdue,
            requesterISO18626Recalled,
            defaultNoStatusChangeOK
        ]
    ];

    private static Map requesterShippedToSupplierISO18626 = [
        code: ActionEventResultList.REQUESTER_SHIPPED_TO_SUPPLIER_ISO18626,
        description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER,
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterISO18626LoanCompleted,
            requesterISO18626Overdue,
            requesterISO18626Recalled,
            defaultNoStatusChangeOK
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

    private static Map requesterBorrowerCheckList = [
        code: ActionEventResultList.REQUESTER_BORROWER_CHECK,
        description: 'Requester is validating the user against the LMS',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterBorrowerCheckOK,
            requesterBorrowerCheckInvalidPatronError,
            requesterBorrowerCheckHostLMSCallFailedError
        ]
    ];

    private static Map requesterCancelList = [
        code: ActionEventResultList.REQUESTER_CANCEL,
        description: 'Requester has requested that the request be cancelled',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterCancelOK,
            requesterCancelOKNoSupplier
        ]
    ];

    private static Map requesterCancelLocalList = [
        code: ActionEventResultList.REQUESTER_CANCEL_LOCAL,
        description: 'Requester has requested that the request be cancelled',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterCancelLocalOK
        ]
    ];

    private static Map requesterCancelWithSupplierIndList = [
        code: ActionEventResultList.REQUESTER_CANCEL_WITH_SUPPLER_INDICATION,
        description: 'Status has changed to cancelled with supplier',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterCancelledWithSupplierIndOK,
            requesterCancelledWithSupplierIndOKcontinue
        ]
    ];


    private static Map requesterFilledLocallyList = [
        code: ActionEventResultList.REQUESTER_FILLED_LOCALLY,
        description: 'Requester has fulfilled the request locally',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterFillLocallyOK
        ]
    ];

    private static Map requesterLocalCannotSupplyList = [
        code: ActionEventResultList.REQUESTER_LOCAL_CANNOT_SUPPLY,
        description: 'It cannot be supplied locally',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterLocalCannotSupplyOK,
            requesterLocalCannotSupplyContinue
        ]
    ];

    private static Map requesterManualCheckInList = [
        code: ActionEventResultList.REQUESTER_MANUAL_CHECK_IN,
        description: 'Requester has manually chcked the item(s) into the local LMS',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterManualCheckInOK
        ]
    ];

    private static Map requesterManualCloseList = [
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

    private static Map requesterNewPatronRequestIndList = [
        code: ActionEventResultList.REQUESTER_EVENT_NEW_PATRON_REQUEST,
        description: 'Event for new patron request indication',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterNewPatronRequestIndOK,
            requesterNewPatronRequestIndOKnoInstitutionSymbol,
            requesterNewPatronRequestIndOKinvalidPatron,
            requesterNewPatronRequestIndOKhostLMSCallFailed,
            requesterNewPatronRequestIndOKDuplicateReview,
            requesterNewPatronRequestIndOKBlankFormReview,
            requesterNewPatronRequestIndOverLimit
        ]
    ];

    private static Map requesterBypassedValidationList = [
        code: ActionEventResultList.REQUESTER_BYPASSED_VALIDATION,
        description: 'Requester has forced progression to validated status',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterNewPatronRequestIndOK
        ]
    ];

    private static Map requesterRequestSentToSupplierIndList = [
        code: ActionEventResultList.REQUESTER_REQUEST_SENT_TO_SUPPLIER_INDICATION,
        description: 'Event for patron request being sent to supplier',
        model: StateModel.MODEL_REQUESTER,
        results: [
                requesterRequestSentToSupplierOK,
                requesterRequestSentToSupplierLocalReview
        ]
    ]

    private static Map requesterCancelledDuplicateList = [
        code: ActionEventResultList.REQUESTER_CANCEL_DUPLICATE,
        description: 'Requester has cancelled the duplicate request',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterCancelDuplicateOK
        ]
    ]

    private static Map requesterMarkEndOfRotaReviewedList = [
        code: ActionEventResultList.REQUESTER_MARK_END_OF_ROTA_REVIEWED,
        description: 'Marked a request at the end of rota as reviewed',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterMarkEndOfRotaReviewed
        ]
    ]

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

    private static Map requesterPatronReturnedShippedList = [
        code: ActionEventResultList.REQUESTER_PATRON_RETURNED_SHIPPED,
        description: 'Patron has returned and it has been returned to the responder',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterPatronReturnedShippedOK,
            requesterPatronReturnedShippedShipItemFailure,
            requesterPatronReturnedShippedFailure
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

    private static Map requesterRetriedValidationList = [
        code: ActionEventResultList.REQUESTER_RETRIED_VALIDATION,
        description: 'Requester is re-trying evaluation of the request',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterNewPatronRequestRetry
        ]
    ];

    private static Map requesterCancelledBlankFormList = [
        code: ActionEventResultList.REQUESTER_CANCELLED_BLANK_FORM,
        description: 'Blank form request is being cancelled',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterCancelOK
        ]
    ]

    private static Map requesterRerequestCancelledList = [
        code: ActionEventResultList.REQUESTER_REREQUEST_CANCELLED,
        description: 'Re-requested with changes',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterRerequestCancelledOK
        ]
    ];

    private static Map requesterRerequestEndOfRotaList = [
        code: ActionEventResultList.REQUESTER_REREQUEST_END_OF_ROTA,
        description: 'Re-requested with changes',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterRerequestEndOfRotaOK
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

    private static Map requesterSendToNextLocationList = [
        code: ActionEventResultList.REQUESTER_SEND_TO_NEXT_LOCATION,
        description: 'Status has changed to Supplier Identified',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterSendToNextLocationOK,
            requesterSendToNextLocationOKlocalReview,
            requesterSendToNextLocationOKendOfRota
        ]
    ];

    private static Map requesterValidateIndList = [
        code: ActionEventResultList.REQUESTER_VALIDATE_INDICATION,
        description: 'Status has changed to Validate',
        model: StateModel.MODEL_REQUESTER,
        results: [
            requesterValidateIndIndOK,
            requesterValidateIndIndOKsourcing,
            requesterValidateIndIndOKendOfRota
        ]
    ];

    private static Map responderAddConditionalList = [
        code: ActionEventResultList.RESPONDER_ADD_CONDITIONAL,
        description: 'The responder is adding a conditional',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderAddConditionalHoldingOK,
            responderAddConditionalOK
        ]
    ];

    private static Map responderAnswerYesList = [
        code: ActionEventResultList.RESPONDER_ANWSER_YES,
        description: 'The responder has said that they will supply the item(s) ',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderAnswerYesOK
        ]
    ];

    private static Map responderAnswerConditionalList = [
        code: ActionEventResultList.RESPONDER_ANWSER_CONDITIONAL,
        description: 'The responder has said that they will supply the item(s) ',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderAnswerConditionalOK,
            responderAnswerConditionalHoldingOK
        ]
    ];

    private static Map responderCancelList = [
        code: ActionEventResultList.RESPONDER_CANCEL,
        description: 'The responder has reported that the requester has agreed to the conditions',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderCancelOK,
            responderCancelNoOK
        ]
    ];

    private static Map responderCancelRequestReceivedIndList = [
        code: ActionEventResultList.RESPONDER_CANCEL_RECEIVED_INDICATION,
        description: 'Event has been triggered for an incoming cancel request',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderCancelRequestReceivedIndOK,
            responderCancelRequestReceivedIndOKcancelled,
            responderCancelRequestReceivedIndOKshipped
        ]
    ];

    private static Map responderCannotSupplyList = [
        code: ActionEventResultList.RESPONDER_CANNOT_SUPPLY,
        description: 'The responder cannot supply the item(s)',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderCannotSupplyOK
        ]
    ];

    private static Map responderCheckInAndShipList = [
        code: ActionEventResultList.RESPONDER_CHECK_IN_AND_SHIP,
        description: 'The responder checks the item(s) into reshare and ships them',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderCheckInAndShipOK,
            responderCheckInAndShipFailure,
            responderCheckInAndShipCheckedInFailure
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

    private static Map responderCheckedIntoReshareIndList = [
        code: ActionEventResultList.RESPONDER_CHECKED_INTO_RESHARE_IND,
        description: 'Status has changed to Checked into Reshare',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderCheckedIntoReshareIndOK
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

    private static Map responderManualCheckOutList = [
        code: ActionEventResultList.RESPONDER_MANUAL_CHECK_OUT,
        description: 'The responder has manually checked the item(s) out of the LMS',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderManualCheckOutOK
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

    private static Map responderNewPatronRequestIndList = [
        code: ActionEventResultList.RESPONDER_EVENT_NEW_PATRON_REQUEST,
        description: 'Event for new incoming request indication',
        model: StateModel.MODEL_RESPONDER,
        results: [
            responderNewPatronRequestIndOK,
            responderNewPatronRequestIndOKlocated,
            responderNewPatronRequestIndOKRequestItem,
            responderNewPatronRequestIndOKunfilled
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

    // Digital returnable requester
    private static Map digitalReturnableRequesterISO18626Overdue = [
      code: 'digitalReturnableRequesterISO18626Overdue',
      description: 'An incoming ISO-18626 message for the requester has said that the status is Overdue',
      result: true,
      status: Status.PATRON_REQUEST_REQUEST_COMPLETE,
      qualifier: 'Overdue',
      saveRestoreState: null,
      nextActionEvent: null
    ];
    private static Map digitalReturnableRequesterISO18626Loaned = [
      code: 'digitalReturnableRequesterISO18626Loaned',
      description: 'An incoming ISO-18626 message for the requester has said that the status is Loaned',
      result: true,
      status: Status.REQUESTER_LOANED_DIGITALLY,
      qualifier: ActionEventResultQualifier.QUALIFIER_LOANED,
      saveRestoreState: null,
      nextActionEvent: null
    ];

    private static Map digitalReturnableRequesterExpectToSupplyISO18626 = [
      code: ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_EXPECTS_TO_SUPPLY_ISO18626,
      description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
      model: StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER,
      results: [
        requesterISO18626Conditional,
        digitalReturnableRequesterISO18626Loaned,
        requesterISO18626Unfilled,
        defaultNoStatusChangeOK
      ]
    ];
    private static Map digitalReturnableRequesterLoanedDigitallyISO18626 = [
      code: ActionEventResultList.DIGITAL_RETURNABLE_REQUESTER_LOANED_DIGITALLY_ISO18626,
      description: 'Maps the incoming ISO-18626 incoming status to one of our internal status when we are in the state ' + Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
      model: StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER,
      results: [
        digitalReturnableRequesterISO18626Overdue,
        defaultNoStatusChangeOK
      ]
    ];



    // CDL responder
    private static Map cdlResponderCheckInToReshareOK = [
        code: 'cdlResponderCheckInToReshareOK',
        description: 'Responder has successfully checked the items out of the LMS into reshare',
        result: true,
        status: Status.RESPONDER_SEQUESTERED,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];
    private static Map cdlResponderFillDigitalLoanOK = [
        code: 'cdlResponderFillDigitalLoanOK',
        description: 'Responder has successfully filled the loan digitally',
        result: true,
        status: Status.RESPONDER_LOANED_DIGITALLY,
        qualifier: null,
        saveRestoreState: null,
        nextActionEvent: null
    ];
    private static Map cdlResponderCheckInToReshareList = [
        code: ActionEventResultList.CDL_RESPONDER_CHECK_INTO_RESHARE,
        description: 'The responder has said that they will supply the item(s) ',
        model: StateModel.MODEL_CDL_RESPONDER,
        results: [
            responderCheckInToReshareFailure,
            cdlResponderCheckInToReshareOK,
        ]
    ];
    private static Map cdlResponderFillDigitalLoanList = [
        code: ActionEventResultList.CDL_RESPONDER_FILL_DIGITAL_LOAN,
        description: 'The responder has said that they will supply the item(s) ',
        model: StateModel.MODEL_CDL_RESPONDER,
        results: [
            cdlResponderFillDigitalLoanOK
        ]
    ];


    // Now specify all the ones we are going to load
    private static Map[] allResultLists = [
            // Requester lists
            requesterAgreeConditionsList,
            requesterBorrowerCheckList,
            requesterCancelList,
            requesterCancelLocalList,
            requesterCancelWithSupplierIndList,
            requesterFilledLocallyList,
            requesterLocalCannotSupplyList,
            requesterMarkEndOfRotaReviewedList,
            requesterManualCheckInList,
            requesterManualCloseList,
            requesterNewPatronRequestIndList,
            requesterNoStatusChangeList,
            requesterPatronReturnedList,
            requesterPatronReturnedShippedList,
            requesterReceivedList,
            requesterRejectConditionsList,
            requesterRerequestCancelledList,
            requesterRerequestEndOfRotaList,
            requesterShippedReturnList,
            requesterSendToNextLocationList,
            requesterValidateIndList,
            requesterBypassedValidationList,
            requesterCancelledDuplicateList,
            requesterRetriedValidationList,
            requesterCancelledBlankFormList,
            requesterRequestSentToSupplierIndList,

            // Digital returnable requester lists
            digitalReturnableRequesterExpectToSupplyISO18626,
            digitalReturnableRequesterLoanedDigitallyISO18626,

            // Responder lists
            responderAddConditionalList,
            responderAnswerConditionalList,
            responderAnswerYesList,
            responderCancelList,
            responderCancelRequestReceivedIndList,
            responderCannotSupplyList,
            responderCheckInAndShipList,
            responderCheckInToReshareList,
            responderCheckedIntoReshareIndList,
            responderCloseManualList,
            responderItemReturnedList,
            responderManualCheckOutList,
            responderMarkConditionsAgreedList,
            responderMarkShippedList,
            responderNewPatronRequestIndList,
            responderNoStatusChangeList,
            responderPrintPullSlipList,

            // CDL responder lists
            cdlResponderCheckInToReshareList,
            cdlResponderFillDigitalLoanList,

            // ISO18626 lists
            requesterAwaitingReturnShippingISO18626,
            requesterBorrowerReturnedISO18626,
            requesterBorrowingLibraryReceivedISO18626,
            requesterCancelPendingISO18626,
            requesterCheckedInISO18626,
            requesterConditionalAnswerReceivedISO18626,
            requesterExpectToSupplyISO18626,
            requesterNotificationReceivedISO18626,
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

	public static void load(Map[] resultLists) {
		log.info("Adding action and event result lists to the database");

        // We are not a service, so we need to look it up
        ReferenceDataService referenceDataService = ReferenceDataService.getInstance();

        resultLists.each { resultList ->
            // Process all the possible outcomes for this result list
            List<ActionEventResult> resultItems = new ArrayList<ActionEventResult>();
            resultList.results.each { result ->
                // Lookup the save / restore state
                RefdataValue saveRestoreState = referenceDataService.lookup(RefdataValueData.VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, result.saveRestoreState);

                // We need to lookup the status
                log.info("Chas: Ensuring ActionEventResult " + result.code + " exists");
                resultItems.add(
                    ActionEventResult.ensure(
                        result.code,
                        result.description,
                        result.result,
                        Status.lookup(result.status),
                        result.qualifier,
                        saveRestoreState,
                        Status.lookup(result.overrideSaveStatus),
                        Status.lookup(result.fromStatus),
                        result.updateRotaLocation == null ? false : result.updateRotaLocation,
                        result.nextActionEvent
                    )
                );
            }

            // Now create the result list
            ActionEventResultList.ensure(resultList.code, resultList.description, resultItems);
        }
	}

	public static void loadAll() {
		(new ActionEventResultData()).load(allResultLists);
	}
}
