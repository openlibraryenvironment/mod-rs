package org.olf.rs

import groovy.util.logging.Slf4j;
import org.grails.datastore.gorm.validation.constraints.AbstractConstraint;
import org.olf.rs.workflow.Action;
import org.olf.rs.workflow.StateTransition;
import org.springframework.validation.Errors;

@Slf4j 
class PendingActionConstraint extends AbstractConstraint  {
	static final String NAME = 'pendingAction'

	static private final String ERROR_CODE_ACTION_NOT_APPLICABLE = "PatronRequest.ActionNotApplicable";
	static private final String ERROR_CODE_INVALID_ACTION        = "PatronRequest.InvalidAction";
	static private final String ERROR_CODE_NO_REQUEST_RECORD     = "PatronRequest.RecordNotFound";
	static private final String ERROR_CODE_SYSTEM_ERROR          = "PatronRequest.SystemError";
	
	PendingActionConstraint(java.lang.Class constraintOwningClass, java.lang.String constraintPropertyName, java.lang.Object constraintParameter, org.springframework.context.MessageSource messageSource) {
		super(constraintOwningClass, constraintPropertyName, constraintParameter, messageSource);
	}
	
	boolean supports(Class type) {
		return(true)
	}

	String getName() {
		NAME
	}

	@Override
	protected void processValidate(Object target, Object value, Errors errors) {
		boolean valid = true;
		if (target instanceof PatronRequest && value instanceof Action) {
			PatronRequest updatedPatronRequest = (PatronRequest)target;
			Action action  = (Action)value; 
			if (!updatedPatronRequest.systemUpdate) {
				try {
					// This is an update by a user, so we need to validate that the action is legitimate
					// First step we need to go to the database and retrieve the record to verify that this action is valid
					if (updatedPatronRequest.id) {
						PatronRequest storedRequest = PatronRequest.get(updatedPatronRequest.id);
						if (storedRequest) {
							// Now lookup to see if we can find a transition for the status and action
							if (!StateTransition.isValid(storedRequest.state, action, storedRequest.isRequester)) {
								// Not a valid transition for the current state
								errors.reject(ERROR_CODE_INVALID_ACTION, [ Action.name, storedRequest.state.name ], "Action " + Action.name + " is not valid for state " + storedRequest.state.name);
							}
						} else {
							errors.reject(ERROR_CODE_NO_REQUEST_RECORD, [ updatedPatronRequest.id ], "Unable to find existing request record: " + updatedPatronRequest.id); 
						}
					} else {
						errors.reject(ERROR_CODE_ACTION_NOT_APPLICABLE, null, "Action is not applicable for a new request");
					}
				} catch (Exception e) {
					// Any sort of exception means it is not valid
					errors.reject(ERROR_CODE_SYSTEM_ERROR, [ action.name, updatedPatronRequest.id ], "Syatem error occured while checking to see if the action " +  action.name);
					log.error("Exception thrown while changing if the action " + action.name + " is valid for request " + updatedPatronRequest.id);
				}
			}
		}
	}

	@Override
	protected Object validateParameter(Object arg0) {
		// We have 
		return(true);
	}
}
