package org.olf.rs.workflow

import com.k_int.web.toolkit.refdata.RefdataValue;

class Status extends RefdataValue {
	static private final String CATEGORY_STATUS = "Reshare ILL Status";

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
	static public final String SHIPPED             = "shipped";	
	static public final String UNFILLED            = "unfilled";	
	static public final String VALIDATED           = "validated";
	
	static public final String LABEL_APPROVED            = "Approved";	
	static public final String LABEL_AWAITING_COLLECTION = "Awaiting Collection";	
	static public final String LABEL_CANCELLED           = "Cancelled";	
	static public final String LABEL_CHECKED_IN          = "Checked In";	
	static public final String LABEL_COLLECTED           = "Collected";	
	static public final String LABEL_FULFILLED           = "Fulfilled";	
	static public final String LABEL_IDLE                = "Idle";	
	static public final String LABEL_IN_PROCESS          = "In Process";	
	static public final String LABEL_PATRON_RETURNED     = "Patron Returned";	
	static public final String LABEL_PENDING             = "Pending";	
	static public final String LABEL_RECEIVED            = "Received";	
	static public final String LABEL_RETURNED            = "Returned";	
	static public final String LABEL_SHIPPED             = "Shipped";	
	static public final String LABEL_UNFILLED            = "Unfilled";	
	static public final String LABEL_VALIDATED           = "Validated";
	
	static hasMany = [transitions : StateTransition];
	
    static mappedBy = [transitions : 'fromStatus']

	static def createDefault() {

		lookupOrCreate(CATEGORY_STATUS, APPROVED, LABEL_APPROVED);
		lookupOrCreate(CATEGORY_STATUS, AWAITING_COLLECTION, LABEL_AWAITING_COLLECTION);
		lookupOrCreate(CATEGORY_STATUS, CANCELLED, LABEL_CANCELLED);
		lookupOrCreate(CATEGORY_STATUS, CHECKED_IN, LABEL_CHECKED_IN);
		lookupOrCreate(CATEGORY_STATUS, COLLECTED, LABEL_COLLECTED);
		lookupOrCreate(CATEGORY_STATUS, FULFILLED, LABEL_FULFILLED);
		lookupOrCreate(CATEGORY_STATUS, IDLE, LABEL_IDLE);
		lookupOrCreate(CATEGORY_STATUS, IN_PROCESS, LABEL_IN_PROCESS);
		lookupOrCreate(CATEGORY_STATUS, PATRON_RETURNED, LABEL_PATRON_RETURNED);
		lookupOrCreate(CATEGORY_STATUS, PENDING, LABEL_PENDING);
		lookupOrCreate(CATEGORY_STATUS, RECEIVED, LABEL_RECEIVED);
		lookupOrCreate(CATEGORY_STATUS, RETURNED, LABEL_RETURNED);
		lookupOrCreate(CATEGORY_STATUS, SHIPPED, LABEL_SHIPPED);
		lookupOrCreate(CATEGORY_STATUS, UNFILLED, LABEL_UNFILLED);
		lookupOrCreate(CATEGORY_STATUS, VALIDATED, LABEL_VALIDATED);
	}
}
