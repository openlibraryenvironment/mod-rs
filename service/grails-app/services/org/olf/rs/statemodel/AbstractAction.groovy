package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.ReshareApplicationEventHandlerService;

public abstract class AbstractAction {

	// We automatically inject these 2 services as most actions use them
	ReshareActionService reshareActionService;
	ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
	
	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	abstract ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails);
	
}
