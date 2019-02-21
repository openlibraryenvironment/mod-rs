package org.olf.rs

import org.grails.datastore.gorm.validation.constraints.AbstractConstraint;
//import org.grails.datastore.gorm.validation.constraints.AbstractVetoingConstraint
import org.olf.rs.workflow.Status
import org.springframework.validation.Errors

class PendingActionValidator extends AbstractConstraint  {
	static NAME = 'actionValidator'

	PendingActionValidator(java.lang.Class constraintOwningClass, java.lang.String constraintPropertyName, java.lang.Object constraintParameter, org.springframework.context.MessageSource messageSource) {
		super(constraintOwningClass, constraintPropertyName, constraintParameter, messageSource);
		def chas = 1;
	}
	
	boolean supports(Class type) {
		return(true)
	}

	String getName() {
		NAME
	}
	
	protected void processValidate(Object target, Object value, Errors errors) {
		boolean valid = true;
		if (value instanceof PatronRequest) {
			PatronRequest updatedPatronRequest = (PatronRequest)value; 
			if (!updatedPatronRequest.systemUpdate) {
				try {
//			String tenantId = Tenants.currentId();
//			Tenants.withId(tenantId) {
				// Do we have access to the database
					Status idle = Status.get(Status.IDLE);
					def chas = 1;
//				}
				} catch (Exception e) {
					// Any sort of exception means it is not valid
					valid = false;
				}
			}
		}
	}

	@Override
	protected Object validateParameter(Object arg0) {
		return(true);
	}
}
