package org.olf.rs.workflow

import grails.events.annotation.Subscriber;
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant;
import groovy.util.logging.Slf4j;

@Slf4j 
class Action implements MultiTenant<Action> {
	
	static public final String APPROVE          = "approve";
	static public final String CHECK_IN         = "check in";
	static public final String COLLECTED        = "collected";
	static public final String NEW_REQUEST      = "new request";
	static public final String NOT_SUPPLY       = "not supply";
	static public final String PATRON_RETURNED  = "patron returned";
	static public final String RECEIVE          = "receive";
	static public final String RECEIVED_MESSAGE = "received message"; // Internal action that receives the message from the requester / responder
	static public final String RETURN           = "return";
	static public final String SEND_MESSAGE     = "send message"; // Internal action to send to the requester / responder
	static public final String SHIP             = "ship";
	static public final String VALIDATE         = "validate";
	
	static public final String NAME_APPROVE          = "Approve";
	static public final String NAME_CHECK_IN         = "Check In";
	static public final String NAME_COLLECTED        = "Collected";
	static public final String NAME_NEW_REQUEST      = "New Request";
	static public final String NAME_NOT_SUPPLY       = "Not Supply";
	static public final String NAME_PATRON_RETURNED  = "Patron Returned";
	static public final String NAME_RECEIVE          = "Receive";
	static public final String NAME_RECEIVED_MESSAGE = "Received Message"; // Internal action that receives the message from the requester / responder
	static public final String NAME_RETURN           = "Return";
	static public final String NAME_SEND_MESSAGE     = "Send Message"; // Internal action to send to the requester / responder
	static public final String NAME_SHIP             = "Ship";
	static public final String NAME_VALIDATE         = "Validate";

	static public final String DESCRIPTION_APPROVE          = "Users approves the request so that it can be sent to a supplier";
	static public final String DESCRIPTION_CHECK_IN         = "Supplying library checks in the request, triggers a check in on the suppliers circ system";
	static public final String DESCRIPTION_COLLECTED        = "The patron has collected the item, triggers a checkout of the temporary item on the requesterd circ system";
	static public final String DESCRIPTION_NEW_REQUEST      = "A new request has been received";
	static public final String DESCRIPTION_NOT_SUPPLY       = "The supplying library has decided they are not going to supply the request";
	static public final String DESCRIPTION_PATRON_RETURNED  = "The patron has returned the item to the requesting library, triggers a check in of the temporary item in the requesters circ system";
	static public final String DESCRIPTION_RECEIVE          = "The item has been received from the supplier, triggers a new item on the requesters circ system";
	static public final String DESCRIPTION_RECEIVED_MESSAGE = "Received Message"; // Internal action that receives the message from the requester / responder
	static public final String DESCRIPTION_RETURN           = "The requesting library has returned the item to the supplier, triggers a delete item on the requesters circ system";
	static public final String DESCRIPTION_SEND_MESSAGE     = "Send Message"; // Internal action to send to the requester / responder
	static public final String DESCRIPTION_SHIP             = "The supplier has shipped the item to the requester, triggers a check out on the suppliers circ system";
	static public final String DESCRIPTION_VALIDATE         = "Validates a requester side request";

	String id;
	String name;
	String description;
	
	/** The service clas that performs this action */
	String serviceClass;

	/** Is the option selectable by the user */
	Boolean selectable;

	/** The status we move onto if the action is successful and if conditional the result is yes */
	Status statusSuccessYes;
	
	/** The status we move onto if the action is successful and if conditional the result is no */
	Status statusSuccessNo;
	
	/** The status we move onto if the action is a failure */
	Status statusFailure;
	
	/** The permission required to perform this action */
//	Permission permission
	
	/** Is this action selectable for bulk action */
	Boolean bulkEnabled;

	/** When the action is performed, display an "Are you sure" dialog */
	Boolean areYouSureDialog;

	/** Transient variable that holds the bean that will do the processing for this action */
	AbstractAction serviceAction = null;
	static transients = ['serviceAction']
	
	static hasMany = [transitions : StateTransition];

	static mappedBy = [transitions : 'action']
	
	static mapping = {
		table            'wf_action'
        areYouSureDialog column : "act_are_you_sure_dialog"
        bulkEnabled      column : "act_bulk_enabled"
        description      column : "act_description"
        id               column : 'act_id'
        name             column : "act_name"
        selectable       column : "act_selectable"
        serviceClass     column : "act_service_class"
        statusFailure    column : "act_status_failure"
        statusSuccessNo  column : "act_status_success_no"
        statusSuccessYes column : "act_status_success_yes"
	}

    static constraints = {
		areYouSureDialog                nullable : true
		bulkEnabled                     nullable : true
		description      maxSize : 512, nullable : false, blank : false
		id               maxSize : 20,  nullable : false, blank : false, unique : true, generator : 'assigned'
		name             maxSize : 40,  nullable : false, blank : false, unique : true
		selectable                      nullable : true
		serviceClass     maxSize : 64,  nullable : true, blank : true
		statusFailure                   nullable : true
		statusSuccessNo                 nullable : true
		statusSuccessYes                nullable : false
    }

	static Action createIfNotExists(String id,
									String name,
							        String description,
									String serviceClass,
									Boolean selectable,
		                            String statusSuccessYes,
		                            String statusSuccessNo,
									String statusFailure,
									Boolean bulkEnabled,
									Boolean areYouSureDialog) {
		Action action = get(id);
		if (action == null) {
			action = new Action();
			action.id =id;
		}

		// We always update the following fields as they may have changed, these are not something that should change on an implementation basis
		action.name = name;
		action.description = description;
		action.serviceClass = serviceClass;
		action.selectable = selectable;
		if (statusSuccessYes) {
			action.statusSuccessYes = Status.get(statusSuccessYes);
		}
		if (statusSuccessNo) {
			action.statusSuccessNo = Status.get(statusSuccessNo);
		}
		if (statusFailure) {
			action.statusFailure = Status.get(statusFailure);
		}
		action.bulkEnabled = bulkEnabled;
		action.areYouSureDialog = areYouSureDialog;
	
	
		if (!action.save(flush : true)) {
			log.error("Unable to save action \"" + action.name + "\", errors: " + action.errors);
		}

		ReShareMessageService.registerClass(id, serviceClass);
		return(action);
	}

    public static void CreateDefault() {
		// ensure the stati are created first
		Status.createDefault();

		createIfNotExists(APPROVE,          NAME_APPROVE,          DESCRIPTION_APPROVE, "ActionApprove", true, Status.PENDING, null, null, true, false);
		createIfNotExists(CHECK_IN,         NAME_CHECK_IN,         DESCRIPTION_CHECK_IN, "ActionCheckIn", true, Status.CHECKED_IN, null, null, true, false);
		createIfNotExists(COLLECTED,        NAME_COLLECTED,        DESCRIPTION_COLLECTED, "ActionCollected", true, Status.COLLECTED, null, null, true, false);
		createIfNotExists(NEW_REQUEST,      NAME_NEW_REQUEST,      DESCRIPTION_NEW_REQUEST, "ActionNewRequest", true, Status.IN_PROCESS, null, null, false, false);
		createIfNotExists(NOT_SUPPLY,       NAME_NOT_SUPPLY,       DESCRIPTION_NOT_SUPPLY, "ActionNotSupply", true, Status.UNFILLED, null, null, true, false);
		createIfNotExists(PATRON_RETURNED,  NAME_PATRON_RETURNED,  DESCRIPTION_PATRON_RETURNED, "ActionPatronReturned", true, Status.PATRON_RETURNED, null, null, false, false);
		createIfNotExists(RECEIVE,          NAME_RECEIVE,          DESCRIPTION_RECEIVE, "ActionReceive", true, Status.AWAITING_COLLECTION, null, null, false, false);
		createIfNotExists(RECEIVED_MESSAGE, NAME_RECEIVED_MESSAGE, DESCRIPTION_RECEIVED_MESSAGE, "ActionReceivedMessage", false, Status.RECEIVED, null, null, false, false);
		createIfNotExists(RETURN,           NAME_RETURN,           DESCRIPTION_RETURN, "ActionReturn", true, Status.RETURNED, null, null, true, false);
		createIfNotExists(SEND_MESSAGE,     NAME_SEND_MESSAGE,     DESCRIPTION_SEND_MESSAGE, "ActionSendMessage", false, Status.SENDING_MESSAGE, null, null, false, false);
		createIfNotExists(SHIP,             NAME_SHIP,             DESCRIPTION_SHIP, "ActionShip", true, Status.FULFILLED, null, null, true, false);
		createIfNotExists(VALIDATE,         NAME_VALIDATE,         DESCRIPTION_VALIDATE, "ActionValidate", false, Status.VALIDATED, null, null, true, false);

		// Now create the state transitions
		StateTransition.createDefault();
	}
}
