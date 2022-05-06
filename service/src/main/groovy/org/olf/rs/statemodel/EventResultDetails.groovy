package org.olf.rs.statemodel

/**
 * Holds the outcome from processing an event
 */
public class EventResultDetails {
	/** The result of performing the event */
	ActionResult result;

	/** The status the request should move to on completion of the event */
	Status newStatus;

	/** The message that will be set in the audit record */
	String auditMessage;

	/** Any data that should be stored with the audit record */
	Map auditData;

	/** Do we add an audit record and Save the request at the end of processing */
	boolean saveData = true;

	/** Contains data that we may want to pass back to the caller */
	Map responseResult = [ : ];

    /** The qualifier for looking up the result record for setting the new status */
    String qualifier;
}
