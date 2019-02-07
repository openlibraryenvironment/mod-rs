package org.olf.rs.workflow

import grails.gorm.MultiTenant
import groovy.util.logging.Slf4j

@Slf4j 
class Status implements MultiTenant<Status> {

	static public final String APPROVED            = "approved";	
	static public final String AWAITING_COLLECTION = "awaiting collection";	
	static public final String CANCELLED           = "cancelled";	
	static public final String CHECKED_IN          = "checked in";	
	static public final String COLLECTED           = "collected";	
	static public final String FULFILLED           = "fulfilled";	
	static public final String IDLE                = "idle";	
	static public final String IN_PROCESS          = "in process";	
	static public final String PATRON_RETURNED     = "patron returned";	
	static public final String PENDING             = "pending";	
	static public final String RECEIVED            = "received";	
	static public final String RETURNED            = "returned";	
	static public final String SENDING_MESSAGE     = "sending message";	
	static public final String SHIPPED             = "shipped";	
	static public final String UNFILLED            = "unfilled";	
	static public final String VALIDATED           = "validated";

	static public final String NAME_APPROVED            = "Approved";
	static public final String NAME_AWAITING_COLLECTION = "Awaiting Collection";
	static public final String NAME_CANCELLED           = "Cancelled";
	static public final String NAME_CHECKED_IN          = "Checked In";
	static public final String NAME_COLLECTED           = "Collected";
	static public final String NAME_FULFILLED           = "Fulfilled";
	static public final String NAME_IDLE                = "Idle";
	static public final String NAME_IN_PROCESS          = "In Process";
	static public final String NAME_PATRON_RETURNED     = "Patron Returned";
	static public final String NAME_PENDING             = "Pending";
	static public final String NAME_RECEIVED            = "Received";
	static public final String NAME_RETURNED            = "Returned";
	static public final String NAME_SENDING_MESSAGE     = "Sending Message";
	static public final String NAME_SHIPPED             = "Shipped";
	static public final String NAME_UNFILLED            = "Unfilled";
	static public final String NAME_VALIDATED           = "Validated";

	static public final String DESCRIPTION_APPROVED            = "The request has been approved by approved and can be sent to a supplier";
	static public final String DESCRIPTION_AWAITING_COLLECTION = "The requesting library is waiting for the patron to collect the item";
	static public final String DESCRIPTION_CANCELLED           = "The request is no longer required and has been cancelled";
	static public final String DESCRIPTION_CHECKED_IN          = "The supplier has checked the item back in and nothing further needs to happen with the request";
	static public final String DESCRIPTION_COLLECTED           = "The patron has collected the item from the requester";
	static public final String DESCRIPTION_FULFILLED           = "The request has been fulfilled and nothing further needs to happen with the request";
	static public final String DESCRIPTION_IDLE                = "The requester has received a new request from a patron";
	static public final String DESCRIPTION_IN_PROCESS          = "The supplier is looking into whether they can supply the item or not";
	static public final String DESCRIPTION_PATRON_RETURNED     = "The patron has returned the item to the requester";
	static public final String DESCRIPTION_PENDING             = "The requester is awaiting a decision by the potential supplier";
	static public final String DESCRIPTION_RECEIVED            = "The item has been received by the requester";
	static public final String DESCRIPTION_RETURNED            = "The item has been returned to the supplier";
	static public final String DESCRIPTION_SENDING_MESSAGE     = "Sending Message";
	static public final String DESCRIPTION_SHIPPED             = "The item has been shipped to the requester";
	static public final String DESCRIPTION_UNFILLED            = "We have not been able to supply the request";
	static public final String DESCRIPTION_VALIDATED           = "The request has been validated";

	/** The identifier / code of the status */	
	String id;

	/** Name */
	String name;

	/** Description for the status */
	String description;

	static hasMany = [transitions : StateTransition];
	
    static mappedBy = [transitions : 'fromStatus']

	static mapping = {
		transitions lazy : false;
		table       'wf_status'
        description column : "st_description"
        id          column : 'st_id'
        name        column : "st_name"
	}

    static constraints = {
			description maxSize : 512, nullable : false, blank : false
			id          maxSize : 40,  nullable : false, blank : false, unique : true, generator : 'assigned'
			name        maxSize : 40,  nullable : false, blank : false
    }

	static Status createIfNotExists(String id, String name, String description) {
		Status status = get(id);
		if (status == null) {
			status = new Status();
			status.id = id;
		}

		// Always update these fields
		status.name = name;
		status.description = description;

		if (!status.save(flush : true)) {
			log.error("Unable to save status \"" + status.name + "\", errors: " + status.errors);
		}

		// return the status
		return(status);
	}

		static def createDefault() {

		createIfNotExists(APPROVED,            NAME_APPROVED,            DESCRIPTION_APPROVED);
		createIfNotExists(AWAITING_COLLECTION, NAME_AWAITING_COLLECTION, DESCRIPTION_AWAITING_COLLECTION);
		createIfNotExists(CANCELLED,           NAME_CANCELLED,           DESCRIPTION_CANCELLED);
		createIfNotExists(CHECKED_IN,          NAME_CHECKED_IN,          DESCRIPTION_CHECKED_IN);
		createIfNotExists(COLLECTED,           NAME_COLLECTED,           DESCRIPTION_COLLECTED);
		createIfNotExists(FULFILLED,           NAME_FULFILLED,           DESCRIPTION_FULFILLED);
		createIfNotExists(IDLE,                NAME_IDLE,                DESCRIPTION_IDLE);
		createIfNotExists(IN_PROCESS,          NAME_IN_PROCESS,          DESCRIPTION_IN_PROCESS);
		createIfNotExists(PATRON_RETURNED,     NAME_PATRON_RETURNED,     DESCRIPTION_PATRON_RETURNED);
		createIfNotExists(PENDING,             NAME_PENDING,             DESCRIPTION_PENDING);
		createIfNotExists(RECEIVED,            NAME_RECEIVED,            DESCRIPTION_RECEIVED);
		createIfNotExists(RETURNED,            NAME_RETURNED,            DESCRIPTION_RETURNED);
		createIfNotExists(SENDING_MESSAGE,     NAME_SENDING_MESSAGE,     DESCRIPTION_SENDING_MESSAGE);
		createIfNotExists(SHIPPED,             NAME_SHIPPED,             DESCRIPTION_SHIPPED);
		createIfNotExists(UNFILLED,            NAME_UNFILLED,            DESCRIPTION_UNFILLED);
		createIfNotExists(VALIDATED,           NAME_VALIDATED,           DESCRIPTION_VALIDATED);
	}
}
