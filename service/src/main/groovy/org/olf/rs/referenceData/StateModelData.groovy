package org.olf.rs.referenceData

import org.apache.kafka.common.metrics.Stat
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import groovy.util.logging.Slf4j;

/**
 * Loads the State model data required for the system to process requests
 */
@Slf4j
public class StateModelData {

    // The states available to the default requester state model
    static private final List requesterStates = [
        [ status : Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING ],
        [ status : Status.PATRON_REQUEST_BORROWER_RETURNED ],
        [ status : Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED ],
        [ status : Status.PATRON_REQUEST_CANCEL_PENDING ],
        [ status : Status.PATRON_REQUEST_CANCELLED, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER ],
        [ status : Status.PATRON_REQUEST_CHECKED_IN ],
        [ status : Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED ],
        [ status : Status.PATRON_REQUEST_END_OF_ROTA, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_ERROR ],
        [ status : Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY ],
        [ status : Status.PATRON_REQUEST_FILLED_LOCALLY, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_IDLE ],
        [ status : Status.PATRON_REQUEST_INVALID_PATRON ],
        [ status : Status.PATRON_REQUEST_LOCAL_REVIEW ],
        [ status : Status.PATRON_REQUEST_OVERDUE ],
        [ status : Status.PATRON_REQUEST_PENDING ],
        [ status : Status.PATRON_REQUEST_RECALLED ],
        [ status : Status.PATRON_REQUEST_REQUEST_COMPLETE, isTerminal : true ],
        [ status : Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER ],
        [ status : Status.PATRON_REQUEST_SHIPPED ],
        [ status : Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER ],
        [ status : Status.PATRON_REQUEST_SOURCING_ITEM ],
        [ status : Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED ],
        [ status : Status.PATRON_REQUEST_UNABLE_TO_CONTACT_SUPPLIER ],
        [ status : Status.PATRON_REQUEST_UNFILLED ],
        [ status : Status.PATRON_REQUEST_VALIDATED ],
        [ status : Status.PATRON_REQUEST_WILL_SUPPLY ]
    ];

  // The states available to the default digital returnable requester state model
  static private final List digitalReturnableRequesterStates = [
    [ status : Status.PATRON_REQUEST_AWAITING_RETURN_SHIPPING ],
    [ status : Status.PATRON_REQUEST_BORROWER_RETURNED ],
    [ status : Status.PATRON_REQUEST_BORROWING_LIBRARY_RECEIVED ],
    [ status : Status.PATRON_REQUEST_CANCEL_PENDING ],
    [ status : Status.PATRON_REQUEST_CANCELLED, isTerminal : true ],
    [ status : Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER ],
    [ status : Status.PATRON_REQUEST_CHECKED_IN ],
    [ status : Status.PATRON_REQUEST_CONDITIONAL_ANSWER_RECEIVED ],
    [ status : Status.PATRON_REQUEST_END_OF_ROTA, isTerminal : true ],
    [ status : Status.PATRON_REQUEST_ERROR ],
    [ status : Status.PATRON_REQUEST_EXPECTS_TO_SUPPLY ],
    [ status : Status.PATRON_REQUEST_FILLED_LOCALLY, isTerminal : true ],
    [ status : Status.PATRON_REQUEST_IDLE ],
    [ status : Status.PATRON_REQUEST_INVALID_PATRON ],
    [ status : Status.PATRON_REQUEST_LOCAL_REVIEW ],
    [ status : Status.PATRON_REQUEST_OVERDUE ],
    [ status : Status.PATRON_REQUEST_PENDING ],
    [ status : Status.PATRON_REQUEST_RECALLED ],
    [ status : Status.PATRON_REQUEST_REQUEST_COMPLETE, isTerminal : true ],
    [ status : Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER ],
    [ status : Status.PATRON_REQUEST_SHIPPED ],
    [ status : Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER ],
    [ status : Status.PATRON_REQUEST_SOURCING_ITEM ],
    [ status : Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED ],
    [ status : Status.PATRON_REQUEST_UNABLE_TO_CONTACT_SUPPLIER ],
    [ status : Status.PATRON_REQUEST_UNFILLED ],
    [ status : Status.PATRON_REQUEST_VALIDATED ],
    [ status : Status.PATRON_REQUEST_WILL_SUPPLY ]
  ];

    // The states available to the default responder state model
    static private final List responderStates = [
        [ status : Status.RESPONDER_AWAIT_PICKING ],
        [ status : Status.RESPONDER_AWAIT_SHIP ],
        [ status : Status.RESPONDER_AWAITING_RETURN_SHIPPING ],
        [ status : Status.RESPONDER_CANCEL_REQUEST_RECEIVED ],
        [ status : Status.RESPONDER_CANCELLED, isTerminal : true ],
        [ status : Status.RESPONDER_COMPLETE, isTerminal : true ],
        [ status : Status.RESPONDER_IDLE, canTriggerStaleRequest : true ],
        [ status : Status.RESPONDER_ITEM_RETURNED ],
        [ status : Status.RESPONDER_ITEM_SHIPPED, canTriggerOverdueRequest : true ],
        [ status : Status.RESPONDER_NEW_AWAIT_PULL_SLIP, canTriggerStaleRequest : true, triggerPullSlipEmail : true ],
        [ status : Status.RESPONDER_NOT_SUPPLIED, isTerminal : true ],
        [ status : Status.RESPONDER_OVERDUE ],
        [ status : Status.RESPONDER_PENDING_CONDITIONAL_ANSWER ],
        [ status : Status.RESPONDER_UNFILLED, isTerminal : true ]
    ];

    // The states available to the default CDL responder state model
    static private final List cdlResponderStates = [
            [ status : Status.RESPONDER_AWAIT_PICKING ],
            [ status : Status.RESPONDER_AWAIT_SHIP ],
            [ status : Status.RESPONDER_AWAITING_RETURN_SHIPPING ],
            [ status : Status.RESPONDER_CANCEL_REQUEST_RECEIVED ],
            [ status : Status.RESPONDER_CANCELLED, isTerminal : true ],
            [ status : Status.RESPONDER_COMPLETE, isTerminal : true ],
            [ status : Status.RESPONDER_IDLE, canTriggerStaleRequest : true ],
            [ status : Status.RESPONDER_ITEM_RETURNED ],
            [ status : Status.RESPONDER_LOANED_DIGITALLY, canTriggerOverdueRequest : true ],
            [ status : Status.RESPONDER_NEW_AWAIT_PULL_SLIP, canTriggerStaleRequest : true, triggerPullSlipEmail : true ],
            [ status : Status.RESPONDER_NOT_SUPPLIED, isTerminal : true ],
            [ status : Status.RESPONDER_OVERDUE ],
            [ status : Status.RESPONDER_PENDING_CONDITIONAL_ANSWER ],
            [ status : Status.RESPONDER_UNFILLED, isTerminal : true ]
    ];

	public void load() {
		log.info("Adding state model records to the database");

        // Now update the state models with the initial state
        StateModel.ensure(StateModel.MODEL_REQUESTER, null, Status.PATRON_REQUEST_IDLE, null, null, null, requesterStates);
        StateModel.ensure(StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, null, Status.PATRON_REQUEST_IDLE, null, null, null, digitalReturnableRequesterStates, [[stateModel: StateModel.MODEL_REQUESTER, priority: 5]]);
        StateModel.ensure(StateModel.MODEL_RESPONDER, null, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, Status.RESPONDER_OVERDUE, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, responderStates);
        StateModel.ensure(StateModel.MODEL_CDL_RESPONDER, null, Status.RESPONDER_IDLE, Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, Status.RESPONDER_AWAIT_DESEQUESTRATION, Actions.ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP, cdlResponderStates, [[ stateModel: StateModel.MODEL_RESPONDER, priority: 5 ]]);
	}

	public static void loadAll() {
		(new StateModelData()).load();
	}
}
