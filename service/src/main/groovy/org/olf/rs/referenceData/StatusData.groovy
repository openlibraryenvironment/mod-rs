package org.olf.rs.referenceData

import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import groovy.util.logging.Slf4j

/**
 * This service works at the module level, it's often called without a tenant context.
 */
@Slf4j
public class StatusData {

	public void load() {
		log.info("Adding naming authorities to the database");
		
        // Requester / Borrower State Model
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE, '0005', true, true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED, '0010', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON, '0011', true, true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SOURCING_ITEM, '0015', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED, '0020', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER, '0025', true, null, false, null );
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED, '0026', true, true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING, '0027', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER, '0028', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNABLE_TO_CONTACT_SUPPLIER);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_OVERDUE, '0036', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_RECALLED, '0037', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED, '0040', true, true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING, '0045', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER, '0046', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_BORROWER_RETURNED, '0050', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_COMPLETE, '0055', true, null, true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_PENDING, '0060', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_WILL_SUPPLY, '0065', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY, '0070', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNFILLED, '0075', true);

        // Add in the ability to tag states with meaningful semantics for reporting
        // model, code, presSeq, visible, needsAttention, terminal, tags
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED, '0076', true, null, false, [ 'ACTIVE_BORROW' ]);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN, '0077', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW, '0079', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_FILLED_LOCALLY, '0081', true, null, true);
        // This one doesn't appear to be in use
        // Status.lookupOrCreate(StateModel.MODEL_REQUESTER, 'REQ_AWAIT_RETURN_SHIPPING', '0078', true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA, '0080', true, null, true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED, '9998', true, null, true);
        Status.ensure(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_ERROR, '9999', true, true);

        // Responder / Lender State Model
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_IDLE, '0005', true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, '0006', true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP, '0010', true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_PICKING, '0015', true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP, '0021', true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_HOLD_PLACED, '0025', true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_UNFILLED, '0030', true, null, true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_NOT_SUPPLIED, '0035', false, null, true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_SHIPPED, '0040', true, null, false, [ 'ACTIVE_LOAN' ] );
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_RETURNED, '0040', true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_COMPLETE, '0040', true, null, true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED, '9998', true, true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCELLED, '9999', true, null, true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_ERROR, '9999', true, true);
        Status.ensure(StateModel.MODEL_RESPONDER, Status.RESPONDER_OVERDUE, '9997', true);
	}
	
	public static void loadAll() {
		(new StatusData()).load();
	}
}
