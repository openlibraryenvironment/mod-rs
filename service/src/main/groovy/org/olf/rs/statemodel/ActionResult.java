package org.olf.rs.statemodel;

/**
 * Provides the outcome of what happened when an action is performeed
 */
public enum ActionResult {

	/** We were successful at processing the action */
	SUCCESS,
	
	/** The parameters supplied for processing the action were invalid */
	INVALID_PARAMETERS,
	
	/** An error occurred while processing the action */ 
	ERROR
}
