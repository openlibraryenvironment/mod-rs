package org.olf.rs.workflow

import grails.gorm.MultiTenant

class StateTransition implements MultiTenant<StateTransition> {

	static public final String QUALIFIER_REQUESTER = "R";
	static public final String QUALIFIER_SUPPLIER  = "S";

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
        id         column : 'st_id', generator: 'uuid', length:36
		action     column : 'st_action'
		fromStatus column : 'st_from_status'
		nextAction column : 'st_next_action'
		qualifier  column : 'st_qualifier'
		toStatus   column : 'st_to_status'
	}

    static constraints = {
			action     nullable : false
			fromStatus nullable : false
			nextAction nullable : true
			toStatus   nullable : false
    }

	static def createIfNotExists(List<String> fromStatusList, String actionCode, String qualifier, String nextActionYesCode, String nextActionNoCode) {
		Action action = Action.findById(actionCode);
		if (action) {
			createIfNotExists1(fromStatusList, action, action.statusSuccessYes, qualifier, nextActionYesCode);
			createIfNotExists1(fromStatusList, action, action.statusSuccessNo, qualifier, nextActionNoCode);
			createIfNotExists1(fromStatusList, action, action.statusFailure, qualifier, null);
		
			// Everything can arrive at the gneneric status "error processing"
//			createIfNotExists1(fromStatusList, action, Status.errorProcessing, null);
		}
	}

	static def createIfNotExists1(List<Status> fromStatusList, Action action, Status toStatus, String qualifier, String nextActionCode) {
		if (toStatus && fromStatusList) {
			Action nextAction;
			if (nextActionCode) {
				nextAction = Action.findById(nextActionCode);
			}
			fromStatusList.each() { Status fromStatus ->
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
				transition.save();
			}
		}		
	}
	
	static def createDefault() {
	
		// Action: Validate - Requester
		createIfNotExists([Status.IDLE], Action.VALIDATE, QUALIFIER_REQUESTER, null, null);

		// Action: Approved - Requester
		createIfNotExists([Status.VALIDATED], Action.APPROVE, QUALIFIER_REQUESTER, null, null);

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
	
	static Action getNextAction(Status fromStatus, Action action, Status toStatus) {
		Action nextAction = null;
		StateTransition transition = findByFromStatusAndActionAndToStatus(fromStatus, action, toStatus);
		if (transition != null) {
			nextAction = transition.nextAction;
		}
		return(nextAction);
	}
}
