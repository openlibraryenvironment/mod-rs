package org.olf.rs.statemodel

/**
 * Holds the outcome from processing an action
 */
public class ActionResultDetails {
	/** The result of performing the action */
	ActionResult result;
	
	/** The details that needs returning to the caller */
    Map responseResult = [ : ]
	
	/** The status the request should move to on completion of the action */
	Status newStatus; 

	/** The message that will be set in the audit record */	
	String auditMessage;
	
	/** Any data that should be stored with the audit record */
	Map auditData;
}
