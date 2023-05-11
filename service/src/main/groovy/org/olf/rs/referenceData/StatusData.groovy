package org.olf.rs.referenceData

import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StatusStage;

import groovy.util.logging.Slf4j

/**
 * Loads the Status data required for the system to process requests
 */
@Slf4j
public class StatusData {

	public void load() {
		log.info("Adding status records to the database");

        // Requester / Borrower State Model
        Status requesterIdle = Status.ensure(Status.PATRON_REQUEST_IDLE, StatusStage.PREPARING, '0005', true, true);
        Status.ensure(Status.PATRON_REQUEST_VALIDATED, StatusStage.PREPARING, '0010', true);
        Status.ensure(Status.PATRON_REQUEST_INVALID_PATRON, StatusStage.PREPARING, '0011', true, true);
        Status.ensure(Status.PATRON_REQUEST_SOURCING_ITEM, StatusStage.PREPARING, '0015', true);
        Status.ensure(Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED, StatusStage.PREPARING, '0020', true);
        Status.ensure(Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, StatusStage.ACTIVE, '0025', true, null, false, null );
        Status.ensure(Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, StatusStage.ACTIVE, '0026', true, true);
        Status.ensure(Status.PATRON_REQUEST_CANCEL_PENDING, StatusStage.ACTIVE, '0027', true);
        Status.ensure(Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER, StatusStage.COMPLETED, '0028', true);
        Status.ensure(Status.PATRON_REQUEST_UNABLE_TO_CONTACT_SUPPLIER, StatusStage.PREPARING);
        Status.ensure(Status.PATRON_REQUEST_OVERDUE, StatusStage.ACTIVE_SHIPPED, '0036', true);
        Status.ensure(Status.PATRON_REQUEST_RECALLED, StatusStage.ACTIVE_SHIPPED, '0037', true);
        Status.ensure(Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, StatusStage.ACTIVE_SHIPPED, '0040', true, true);
        Status.ensure(Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, StatusStage.ACTIVE_SHIPPED, '0045', true);
        Status.ensure(Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, StatusStage.ACTIVE_SHIPPED, '0046', true);
        Status.ensure(Status.PATRON_REQUEST_BORROWER_RETURNED, StatusStage.ACTIVE_SHIPPED, '0050', true);
        Status.ensure(Status.PATRON_REQUEST_REQUEST_COMPLETE, StatusStage.COMPLETED, '0055', true, null, true, 1);
        Status.ensure(Status.PATRON_REQUEST_PENDING, StatusStage.ACTIVE, '0060', true);
        Status.ensure(Status.PATRON_REQUEST_WILL_SUPPLY, StatusStage.ACTIVE, '0065', true);
        Status.ensure(Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, StatusStage.ACTIVE, '0070', true);
        Status.ensure(Status.PATRON_REQUEST_UNFILLED, StatusStage.COMPLETED, '0075', true);

        // Add in the ability to tag states with meaningful semantics for reporting
        // model, code, presSeq, visible, needsAttention, terminal, tags
        Status.ensure(Status.PATRON_REQUEST_SHIPPED, StatusStage.ACTIVE_SHIPPED, '0076', true, null, false, null, [ 'ACTIVE_BORROW' ]);
        Status.ensure(Status.PATRON_REQUEST_CHECKED_IN, StatusStage.ACTIVE_SHIPPED, '0077', true);
        Status.ensure(Status.PATRON_REQUEST_LOCAL_REVIEW, StatusStage.LOCAL, '0079', true);
        Status.ensure(Status.PATRON_REQUEST_FILLED_LOCALLY, StatusStage.COMPLETED, '0081', true, null, true, 2);
        // This one doesn't appear to be in use
        // Status.lookupOrCreate('REQ_AWAIT_RETURN_SHIPPING', StatusStage.ACTIVE_SHIPPED, '0078', true);
        Status.ensure(Status.PATRON_REQUEST_END_OF_ROTA, StatusStage.COMPLETED, '0080', true, null, true, 3);
        Status.ensure(Status.PATRON_REQUEST_CANCELLED, StatusStage.COMPLETED, '9998', true, null, true, 4);
        Status.ensure(Status.PATRON_REQUEST_ERROR, StatusStage.ACTIVE, '9999', true, true);

        // Digital Returnable Requester
        Status.ensure(Status.REQUESTER_LOANED_DIGITALLY, StatusStage.ACTIVE_SHIPPED, '0076', true, null, false);

        // Responder / Lender State Model
        Status responderIdle = Status.ensure(Status.RESPONDER_IDLE, StatusStage.ACTIVE, '0005', true);
        Status.ensure(Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, StatusStage.ACTIVE_PENDING_CONDITIONAL_ANSWER, '0006', true);
        Status.ensure(Status.RESPONDER_NEW_AWAIT_PULL_SLIP, StatusStage.ACTIVE, '0010', true);
        Status.ensure(Status.RESPONDER_AWAIT_PICKING, StatusStage.ACTIVE, '0015', true);
        Status.ensure(Status.RESPONDER_AWAIT_SHIP, StatusStage.ACTIVE, '0021', true);
        Status.ensure(Status.RESPONDER_HOLD_PLACED, StatusStage.ACTIVE, '0025', true);
        Status.ensure(Status.RESPONDER_UNFILLED, StatusStage.COMPLETED, '0030', true, null, true, 2);
        Status.ensure(Status.RESPONDER_NOT_SUPPLIED, StatusStage.COMPLETED, '0035', false, null, true);
        Status.ensure(Status.RESPONDER_ITEM_SHIPPED, StatusStage.ACTIVE_SHIPPED, '0040', true, null, false, null, [ 'ACTIVE_LOAN' ] );
        Status.ensure(Status.RESPONDER_ITEM_RETURNED, StatusStage.ACTIVE_SHIPPED, '0040', true);
        Status.ensure(Status.RESPONDER_COMPLETE, StatusStage.COMPLETED, '0040', true, null, true, 1);
        Status.ensure(Status.RESPONDER_CANCEL_REQUEST_RECEIVED, StatusStage.ACTIVE, '9998', true, true);
        Status.ensure(Status.RESPONDER_CANCELLED, StatusStage.COMPLETED, '9999', true, null, true, 3);
        Status.ensure(Status.RESPONDER_ERROR, StatusStage.ACTIVE, '9999', true, true);
        Status.ensure(Status.RESPONDER_OVERDUE, StatusStage.ACTIVE_SHIPPED, '9997', true);

        // CDL Responder
        Status.ensure(Status.RESPONDER_SEQUESTERED, StatusStage.ACTIVE, '0020', true);
        Status.ensure(Status.RESPONDER_LOANED_DIGITALLY, StatusStage.ACTIVE_SHIPPED, '0040', true);
        Status.ensure(Status.RESPONDER_AWAIT_DESEQUESTRATION, StatusStage.ACTIVE_SHIPPED, '0040', true);

	}

	public static void loadAll() {
		(new StatusData()).load();
	}
}
