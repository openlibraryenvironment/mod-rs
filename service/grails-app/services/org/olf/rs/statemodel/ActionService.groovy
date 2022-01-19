package org.olf.rs.statemodel;

/**
 * Checks the incoming action to ensure it is valid and dispatches it to the appropriate service 
 */
public class ActionService {

	/**
	 * Checks whether an action being performed is valid
	 */
	private boolean isValid(boolean isRequester, Status status, String action) {

		// We default to not being valid		
		boolean isValid = false;

		// Can only continue if we have been supplied the values
		if (action && status) {
			// Get hold of the state model id		
			StateModel stateModel = StateModel.stateModelCode(isRequester);
			if ( stateModel ) {
				// It is a valid state model
				// Now is this a valid action for this state
				isValid = (AvailableAction.countByModelAndFromStateAndActionCode(stateModel, status, action) == 1); 
			}
		}
		return(isValid);
	}
}
