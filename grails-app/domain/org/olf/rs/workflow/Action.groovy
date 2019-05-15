package org.olf.rs.workflow

import grails.events.annotation.Subscriber;
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant;
import groovy.util.logging.Slf4j;

@Slf4j
class Action implements MultiTenant<Action> {

	static public final String APPROVE               = "approve";
	static public final String CHECK_IN              = "check in";
	static public final String COLLECTED             = "collected";
	static public final String INFORMED_NOT_SUPPLIED = "informedNotSupplied"; // Using spaces for this ones, ends up being to long for the field
	static public final String INFORMED_RECEIVED     = "informed received";
	static public final String INFORMED_RETURNED     = "informed returned";
	static public final String INFORMED_SHIPPED      = "informed shipped";
	static public final String LOCATE                = "locate"; // attempt to locate potential suppliers, rank according to routing policy
	static public final String NEW_REQUEST           = "new request";
	static public final String NEXT_RESPONDER        = "next responder";
	static public final String NOT_SUPPLY            = "not supply";
	static public final String NOTIFY_PATRON         = "notify patron";
	static public final String PATRON_RETURNED       = "patron returned";
	static public final String RECEIVE               = "receive";
	static public final String RECEIVED_MESSAGE      = "received message"; // Internal action that receives the message from the requester / responder
	static public final String RETURN                = "return";
	static public final String SEND_MESSAGE          = "send message"; // Internal action to send to the requester / responder
	static public final String SHIP                  = "ship";
	static public final String VALIDATE              = "validate";

	static public final String NAME_APPROVE               = "Approve";
	static public final String NAME_CHECK_IN              = "Check In";
	static public final String NAME_COLLECTED             = "Collected";
	static public final String NAME_INFORMED_NOT_SUPPLIED = "Informed not Supplied";
	static public final String NAME_INFORMED_RECEIVED     = "Informed Received";
	static public final String NAME_INFORMED_RETURNED     = "Informed Returned";
	static public final String NAME_INFORMED_SHIPPED      = "Informed Shipped";
	static public final String NAME_LOCATE                = "Locate";
	static public final String NAME_NEW_REQUEST           = "New Request";
	static public final String NAME_NEXT_RESPONDER        = "Next Responder";
	static public final String NAME_NOT_SUPPLY            = "Not Supply";
	static public final String NAME_NOTIFY_PATRON         = "Notify Patron";
	static public final String NAME_PATRON_RETURNED       = "Patron Returned";
	static public final String NAME_RECEIVE               = "Receive";
	static public final String NAME_RECEIVED_MESSAGE      = "Received Message"; // Internal action that receives the message from the requester / responder
	static public final String NAME_RETURN                = "Return";
	static public final String NAME_SEND_MESSAGE          = "Send Message"; // Internal action to send to the requester / responder
	static public final String NAME_SHIP                  = "Ship";
	static public final String NAME_VALIDATE              = "Validate";

	static public final String DESCRIPTION_APPROVE               = "Users approves the request so that it can be sent to a supplier";
	static public final String DESCRIPTION_CHECK_IN              = "Supplying library checks in the request, triggers a check in on the suppliers circ system";
	static public final String DESCRIPTION_COLLECTED             = "The patron has collected the item, triggers a checkout of the temporary item on the requesterd circ system";
	static public final String DESCRIPTION_INFORMED_NOT_SUPPLIED = "The requester has been informed that the supplier will not supply the item";
	static public final String DESCRIPTION_INFORMED_RECEIVED     = "The supplier has been informed that the requester has received the item";
	static public final String DESCRIPTION_INFORMED_RETURNED     = "The supplier has been informed that the requester has returned the item";
	static public final String DESCRIPTION_INFORMED_SHIPPED      = "The requester has been informed that the supplier has shipped the item";
	static public final String DESCRIPTION_LOCATE                = 'Attempt to locate appropriate copies and rank the rota according to routing policies';
	static public final String DESCRIPTION_NEW_REQUEST           = "A new request has been received";
	static public final String DESCRIPTION_NEXT_RESPONDER        = "Move onto the next responder if we have one";
	static public final String DESCRIPTION_NOT_SUPPLY            = "The supplying library has decided they are not going to supply the request";
	static public final String DESCRIPTION_NOTIFY_PATRON         = "Item has been put on the shelf for the patron to come and collect";
	static public final String DESCRIPTION_PATRON_RETURNED       = "The patron has returned the item to the requesting library, triggers a check in of the temporary item in the requesters circ system";
	static public final String DESCRIPTION_RECEIVE               = "The item has been received from the supplier, triggers a new item on the requesters circ system";
	static public final String DESCRIPTION_RECEIVED_MESSAGE      = "Received Message"; // Internal action that receives the message from the requester / responder
	static public final String DESCRIPTION_RETURN                = "The requesting library has returned the item to the supplier, triggers a delete item on the requesters circ system";
	static public final String DESCRIPTION_SEND_MESSAGE          = "Send Message"; // Internal action to send to the requester / responder
	static public final String DESCRIPTION_SHIP                  = "The supplier has shipped the item to the requester, triggers a check out on the suppliers circ system";
	static public final String DESCRIPTION_VALIDATE              = "Validates a requester side request";

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
	//  Permission permission

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
			Boolean areYouSureDialog,
			ReShareMessageService reShareMessageService) {
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

		reShareMessageService.registerClass(id, serviceClass);
		return(action);
	}

	public static void CreateDefault(ReShareMessageService reShareMessageService) {
		// ensure the stati are created first
		Status.createDefault();

		//                id,                    name,                       description,                       serviceClass,          selectable, statusSuccessYes,           statusSuccessNo,     statusFailure, bulkEnabled, areYouSureDialog
		createIfNotExists(APPROVE,               NAME_APPROVE,               DESCRIPTION_APPROVE,               "Approve",             true,       Status.PENDING,             null,                null,          true,        false, reShareMessageService);
		createIfNotExists(CHECK_IN,              NAME_CHECK_IN,              DESCRIPTION_CHECK_IN,              "CheckIn",             true,       Status.CHECKED_IN,          null,                null,          true,        false, reShareMessageService);
		createIfNotExists(COLLECTED,             NAME_COLLECTED,             DESCRIPTION_COLLECTED,             "Collected",           true,       Status.COLLECTED,           null,                null,          true,        false, reShareMessageService);
		createIfNotExists(INFORMED_NOT_SUPPLIED, NAME_INFORMED_NOT_SUPPLIED, DESCRIPTION_INFORMED_NOT_SUPPLIED, "InformedNotSupplied", false,      Status.UNFILLED,            null,                null,          true,        false, reShareMessageService);
		createIfNotExists(INFORMED_RECEIVED,     NAME_INFORMED_RECEIVED,     DESCRIPTION_INFORMED_RECEIVED,     "InformedReceived",    false,      Status.RECEIVED,            null,                null,          true,        false, reShareMessageService);
		createIfNotExists(INFORMED_RETURNED,     NAME_INFORMED_RETURNED,     DESCRIPTION_INFORMED_RETURNED,     "InformedReturned",    false,      Status.RETURNED,            null,                null,          true,        false, reShareMessageService);
		createIfNotExists(INFORMED_SHIPPED,      NAME_INFORMED_SHIPPED,      DESCRIPTION_INFORMED_SHIPPED,      "InformedShipped",     false,      Status.SHIPPED,             null,                null,          true,        false, reShareMessageService);
		createIfNotExists(LOCATE,                NAME_LOCATE,                DESCRIPTION_LOCATE,                "Locate",              false,      Status.LOCATED,             Status.NO_LOCATIONS, null,          true,        false, reShareMessageService);
		createIfNotExists(NEW_REQUEST,           NAME_NEW_REQUEST,           DESCRIPTION_NEW_REQUEST,           "NewRequest",          false,      Status.IN_PROCESS,          null,                null,          false,       false, reShareMessageService);
		createIfNotExists(NEXT_RESPONDER,        NAME_NEXT_RESPONDER,        DESCRIPTION_NEXT_RESPONDER,        "NextResponder",       false,      Status.LOCATED,             Status.NO_LOCATIONS, null,          true,        false, reShareMessageService);
		createIfNotExists(NOT_SUPPLY,            NAME_NOT_SUPPLY,            DESCRIPTION_NOT_SUPPLY,            "NotSupply",           true,       Status.UNFILLED,            null,                null,          true,        false, reShareMessageService);
		createIfNotExists(NOTIFY_PATRON,         NAME_NOTIFY_PATRON,         DESCRIPTION_NOTIFY_PATRON,         "NotifyPatron",        true,       Status.AWAITING_COLLECTION, null,                null,          true,        false, reShareMessageService);
		createIfNotExists(PATRON_RETURNED,       NAME_PATRON_RETURNED,       DESCRIPTION_PATRON_RETURNED,       "PatronReturned",      true,       Status.PATRON_RETURNED,     null,                null,          false,       false, reShareMessageService);
		createIfNotExists(RECEIVE,               NAME_RECEIVE,               DESCRIPTION_RECEIVE,               "Receive",             true,       Status.AWAITING_COLLECTION, null,                null,          false,       false, reShareMessageService);
		createIfNotExists(RECEIVED_MESSAGE,      NAME_RECEIVED_MESSAGE,      DESCRIPTION_RECEIVED_MESSAGE,      "ReceivedMessage",     false,      Status.RECEIVED,            null,                null,          false,       false, reShareMessageService);
		createIfNotExists(RETURN,                NAME_RETURN,                DESCRIPTION_RETURN,                "Return",              true,       Status.RETURNED,            null,                null,          true,        false, reShareMessageService);
		createIfNotExists(SEND_MESSAGE,          NAME_SEND_MESSAGE,          DESCRIPTION_SEND_MESSAGE,          "SendMessage",         false,      Status.SENDING_MESSAGE,     null,                null,          false,       false, reShareMessageService);
		createIfNotExists(SHIP,                  NAME_SHIP,                  DESCRIPTION_SHIP,                  "Ship",                true,       Status.FULFILLED,           null,                null,          true,        false, reShareMessageService);
		createIfNotExists(VALIDATE,              NAME_VALIDATE,              DESCRIPTION_VALIDATE,              "Validate",            true,       Status.VALIDATED,           null,                null,          true,        false, reShareMessageService);

		// Now create the state transitions
		StateTransition.createDefault();
	}
}
