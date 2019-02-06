package org.olf.rs.workflow

import org.olf.rs.PatronRequest

import com.k_int.web.toolkit.custprops.CustomProperties
import com.k_int.web.toolkit.tags.Taggable

import grails.gorm.MultiTenant
import grails.util.Holders;
import groovy.util.logging.Slf4j

// Do we also need to implement CustomProperties and Taggable as well, what is the purpose of these two ?
@Slf4j 
class Action implements MultiTenant<Action> {
	
	static public final String APPROVE          = "Approve";
	static public final String CHECK_IN         = "Check In";
	static public final String COLLECTED        = "Collected";
	static public final String NEW_REQUEST      = "New Request";
	static public final String NOT_SUPPLY       = "Not Supply";
	static public final String PATRON_RETURNED  = "Patron Returned";
	static public final String RECEIVE          = "Receive";
	static public final String RECEIVED_MESSAGE = "Received Message"; // Internal action that receives the message from the requester / responder
	static public final String RETURN           = "Return";
	static public final String SEND_MESSAGE     = "Send Message"; // Internal action to send to the requester / responder
	static public final String SHIP             = "Ship";
	static public final String VALIDATE         = "Validate";
	
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
		Action action = findById(id);
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
			action.statusSuccessYes = Status.findByValue(statusSuccessYes);
		}
		if (statusSuccessNo) {
			action.statusSuccessNo = Status.findByValue(statusSuccessNo);
		}
		if (statusFailure) {
			action.statusFailure = Status.findByValue(statusFailure);
		}
		action.bulkEnabled = bulkEnabled;
		action.areYouSureDialog = areYouSureDialog;
	
	
		if (!action.save(flush : true)) {
			log.error("Unable to save action \"" + action.name + "\", errors: " + action.errors);
		}

		return(action);
	}

	AbstractAction getServiceAction() {
		// If we havn't already set the service action then we need to set it
		if (serviceAction == null) {
			// But only if we have a service class
			if (serviceClass) {
				// Now setup the link to the service action that actually does the work
				try {
					serviceAction = Holders.grailsApplication.mainContext.getBean("action" + serviceClass + "Service");
				} catch (Exception e) {
					log.error("Unable to locate action bean: " + serviceClass);
				}
			}
		}
		return(serviceAction);
	}

	static def createDefault() {
		// ensure the stati are created first
		Status.createDefault();

		createIfNotExists(APPROVE, APPROVE, APPROVE, "Approve", true, Status.PENDING, null, null, true, false);
		createIfNotExists(CHECK_IN, CHECK_IN, CHECK_IN, "CheckIn", true, Status.CHECKED_IN, null, null, true, false);
		createIfNotExists(COLLECTED, COLLECTED, COLLECTED, "Collected", true, Status.COLLECTED, null, null, true, false);
		createIfNotExists(NEW_REQUEST, NEW_REQUEST, NEW_REQUEST, "NewRequest", true, Status.IN_PROCESS, null, null, false, false);
		createIfNotExists(NOT_SUPPLY, NOT_SUPPLY, NOT_SUPPLY, "NotSupply", true, Status.UNFILLED, null, null, true, false);
		createIfNotExists(PATRON_RETURNED, PATRON_RETURNED, PATRON_RETURNED, "Patron Returned", true, Status.PATRON_RETURNED, null, null, false, false);
		createIfNotExists(RECEIVE, RECEIVE, RECEIVE, "Receive", true, Status.AWAITING_COLLECTION, null, null, false, false);
		createIfNotExists(RECEIVED_MESSAGE, RECEIVED_MESSAGE, RECEIVED_MESSAGE, "ReceivedMessage", false, null, null, null, false, false);
		createIfNotExists(RETURN, RETURN, RETURN, "Return", true, Status.RETURNED, null, null, true, false);
		createIfNotExists(SEND_MESSAGE, SEND_MESSAGE, SEND_MESSAGE, "SendMessage", false, null, null, null, false, false);
		createIfNotExists(SHIP, SHIP, SHIP, "Ship", true, Status.FULFILLED, null, null, true, false);
		createIfNotExists(VALIDATE, VALIDATE, VALIDATE, "Validate", false, Status.VALIDATED, null, null, true, false);
	}
}
