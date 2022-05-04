package org.olf.rs.referenceData;

import org.olf.rs.statemodel.ActionEventResult;
import org.olf.rs.statemodel.ActionEventResultList;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

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
        nextAactionEvent: null
    ];

    private static Map requesterISO18626Conditional = [
        code: 'requesterISO18626LoanConditional',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Conditional',
        result: true,
        status: Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED,
        qualifier: 'Conditional',
        saveRestoreState: null,
        nextAactionEvent: null
    ];

    private static Map requesterISO18626ExpectToSupply = [
        code: 'requesterISO18626ExpectToSupply',
        description: 'An incoming ISO-18626 message for the requester has said that the status is ExpectToSupply',
        result: true,
        status: Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY,
        qualifier: 'ExpectToSupply',
        saveRestoreState: null,
        nextAactionEvent: null
    ];

    private static Map requesterISO18626LoanCompleted = [
        code: 'requesterISO18626LoanCompleted',
        description: 'An incoming ISO-18626 message for the requester has said that the status is LoanCompleted',
        result: true,
        status: Status.PATRON_REQUEST_REQUEST_COMPLETE,
        qualifier: 'LoanCompleted',
        saveRestoreState: null,
        nextAactionEvent: null
    ];

    private static Map requesterISO18626Loaned = [
        code: 'requesterISO18626Loaned',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Loaned',
        result: true,
        status: Status.PATRON_REQUEST_SHIPPED,
        qualifier: 'Loaned',
        saveRestoreState: null,
        nextAactionEvent: null
    ];

    private static Map requesterISO18626Overdue = [
        code: 'requesterISO18626Overdue',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Overdue',
        result: true,
        status: Status.PATRON_REQUEST_OVERDUE,
        qualifier: 'Overdue',
        saveRestoreState: null,
        nextAactionEvent: null
    ];

    private static Map requesterISO18626Recalled = [
        code: 'requesterISO18626Recalled',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Recalled',
        result: true,
        status: Status.PATRON_REQUEST_RECALLED,
        qualifier: 'Recalled',
        saveRestoreState: null,
        nextAactionEvent: null
    ];

    private static Map requesterISO18626Unfilled = [
        code: 'requesterISO18626Unfilled',
        description: 'An incoming ISO-18626 message for the requester has said that the status is Unfilled',
        result: true,
        status: Status.PATRON_REQUEST_UNFILLED,
        qualifier: 'Unfilled',
        saveRestoreState: null,
        nextAactionEvent: null
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
            requesterISO18626Loaned
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

    // Now specify all the ones we are going to load
    private static Map[] allResultLists = [
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
        requesterShippedToSupplierISO18626
    ];

	public void load() {
		log.info("Adding action and event result lists to the database");

        allResultLists.each { resultList ->
            // Process all the possible outcomes for this result list
            List<ActionEventResult> resultItems = new ArrayList<ActionEventResult>();
            resultList.results.each { result ->
                // We need to lookup the status
                resultItems.add(ActionEventResult.ensure(result.code, result.description, result.result, Status.lookup(resultList.model, result.status), result.qualifier, result.saveRestoreState, result.nextAactionEvent));
            }

            // Now create the result list
            ActionEventResultList.ensure(resultList.code, resultList.description, resultItems);
        }
	}

	public static void loadAll() {
		(new ActionEventResultData()).load();
	}
}
