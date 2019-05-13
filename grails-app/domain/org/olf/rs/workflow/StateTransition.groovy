package org.olf.rs.workflow

import grails.gorm.MultiTenant

class StateTransition implements MultiTenant<StateTransition> {

	static private final String QUALIFIER_REQUESTER = "R";
	static private final String QUALIFIER_SUPPLIER  = "S";

	/** We have the id as a uuid */
	String id;

	/** The status the request is currently at */
	Status fromStatus;

	/** The action being performed */
	Action action;

	/** The status this action takes us to */
	Status toStatus;

	/** Any follow up action that needs performing */
	Action nextAction;

	/** Who the state table belongs to, eg. requester / supplier */
	String qualifier;

	static belongsTo = [action     : Action,
		fromStatus : Status,
		toStatus   : Status];

	static mapping = {
		table      'wf_state_transition'
		id         column : 'st_id', generator: 'uuid2', length:36
		action     column : 'st_action'
		fromStatus column : 'st_from_status'
		nextAction column : 'st_next_action'
		qualifier  column : 'st_qualifier'
		toStatus   column : 'st_to_status'
	}

	static constraints = {
		action     nullable : false
		fromStatus nullable : false, unique : ['action', 'toStatus', 'qualifier']
		nextAction nullable : true
		qualifier  nullable : false
		toStatus   nullable : false
	}

	static def createIfNotExists(List<String> fromStatusList, String actionCode, String qualifier, String nextActionYesCode, String nextActionNoCode) {
		Action action = Action.findById(actionCode);
		if (action) {
			createIfNotExists1(fromStatusList, action, action.statusSuccessYes, qualifier, nextActionYesCode);
			createIfNotExists1(fromStatusList, action, action.statusSuccessNo, qualifier, nextActionNoCode);
			createIfNotExists1(fromStatusList, action, action.statusFailure, qualifier, null);

			// Everything can arrive at the gneneric status "error processing"
			//      createIfNotExists1(fromStatusList, action, Status.errorProcessing, null);
		}
	}

	static def createIfNotExists1(List<String> fromStatusList, Action action, Status toStatus, String qualifier, String nextActionCode) {
		if (toStatus && fromStatusList) {
			Action nextAction;
			if (nextActionCode) {
				nextAction = Action.get(nextActionCode);
			}
			fromStatusList.each() { String fromStatusCode ->
				Status fromStatus = Status.get(fromStatusCode);
				if (fromStatus != null) {
					StateTransition transition = StateTransition.findByFromStatusAndActionAndQualifierAndToStatus(fromStatus, action, qualifier, toStatus);
					if (transition == null) {
						transition = new StateTransition();
						transition.fromStatus = fromStatus;
						transition.action = action;
						transition.qualifier = qualifier;
						transition.toStatus = toStatus;
					}

					// Next action may have changed
					transition.nextAction = nextAction;
					if (!transition.save(flush : true)) {
						log.error("Unable to save transition, from status \"" + transition.fromStatus.name + "\", action: \"" + transition.action.name + "\", to status: \"" + transition.fromStatus.name + "\", errors: " + transition.errors);
					}
				}
			}
		}
	}

	static def createDefault() {

		// Action: Validate - Requester
		createIfNotExists([Status.IDLE], Action.VALIDATE, QUALIFIER_REQUESTER, Action.LOCATE, null);

		// Action: Approved - Requester
		createIfNotExists([Status.LOCATED], Action.APPROVE, QUALIFIER_REQUESTER, null, null);

                // Action: Located - Requester
		createIfNotExists([Status.LOCATED], Action.LOCATE, QUALIFIER_REQUESTER, Action.APPROVE, null);

                // Action: Located - Requester
		createIfNotExists([Status.NO_LOCATIONS], Action.APPROVE, QUALIFIER_REQUESTER, null, null);

		// Action: New Request - Responder
		createIfNotExists([Status.IDLE], Action.NEW_REQUEST, QUALIFIER_SUPPLIER, null, null);

		// Action: Shipped - Responder
		createIfNotExists([Status.IN_PROCESS], Action.SHIP, QUALIFIER_SUPPLIER, null, null);

		// Action: Not Supplied - Responder
		createIfNotExists([Status.IN_PROCESS], Action.NOT_SUPPLY, QUALIFIER_SUPPLIER, null, null);

		// Action: Received - requester
		createIfNotExists([Status.AWAITING_COLLECTION], Action.RECEIVE, QUALIFIER_REQUESTER, null, null);

		// Action: Collected - requester
		createIfNotExists([Status.COLLECTED], Action.COLLECTED, QUALIFIER_REQUESTER, null, null);

		// Action: Patron Returned - requester
		createIfNotExists([Status.PATRON_RETURNED], Action.PATRON_RETURNED, QUALIFIER_REQUESTER, null, null);

		// Action: Returned - requester
		createIfNotExists([Status.PATRON_RETURNED], Action.RETURN, QUALIFIER_REQUESTER, null, null);

		// Action: Checked In - responder
		createIfNotExists([Status.SHIPPED], Action.CHECK_IN, QUALIFIER_SUPPLIER, null, null);
	}

	static String GetQualifer(boolean isRequester) {
		return(isRequester ? QUALIFIER_REQUESTER : QUALIFIER_SUPPLIER)
	}

	static Action getNextAction(Status fromStatus, Action action, Status toStatus, boolean isRequester) {
		Action nextAction = null;
		StateTransition transition = findByFromStatusAndActionAndToStatusAndQualifier(fromStatus, action, toStatus, GetQualifer(isRequester));
		if (transition != null) {
			nextAction = transition.nextAction;
		}
		return(nextAction);
	}

	static boolean isValid(Status fromStatus, Action action, boolean isRequester) {
		return(countByFromStatusAndActionAndQualifier(fromStatus, action, GetQualifer(isRequester)) > 0);
	}
}
