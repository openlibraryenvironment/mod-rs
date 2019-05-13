package org.olf.rs.workflow

import grails.gorm.transactions.Transactional;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota
import org.olf.rs.workflow.AbstractAction.ActionResponse;
import groovy.util.logging.Slf4j

@Slf4j
@Transactional
class ActionApproveService extends AbstractAction {

	/** Returns the action that this class represents */
	@Override
	String getActionCode() {
		return(Action.APPROVE);
	}

	/** Performs the action */
	@Override
	ActionResponse perform(PatronRequest requestToBeProcessed) {
		ActionResponse result = ActionResponse.ERROR;
		
		// For the time being we just return OK as we do not do anything
		log.debug("ActionApproveService::perform(${requestToBeProcessed})");

		// The action cannot be performed if we do not have a rota item at the current rota position
		List<PatronRequestRota> rota = requestToBeProcessed.rota;
		if ((rota == null) || (requestToBeProcessed.rotaPosition >= rota.size())) {
			
		} else {
			result = ActionResponse.SUCCESS;
		}
		
		return(result);
	}
}
