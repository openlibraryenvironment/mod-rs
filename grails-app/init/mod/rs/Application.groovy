package mod.rs

import grails.boot.GrailsApp;
import grails.boot.config.GrailsAutoConfiguration;
import groovy.util.logging.Slf4j;

import org.grails.datastore.gorm.validation.constraints.registry.ConstraintRegistry;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity;
import org.grails.datastore.gorm.validation.constraints.registry.DefaultValidatorRegistry;
import org.olf.rs.PendingActionConstraint;

@Slf4j 
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    void doWithApplicationContext() {
  		try {
			// Register the pending action constraint
  			MappingContext mc = grailsApplication.getMappingContext();
			DefaultValidatorRegistry reg = mc.validatorRegistry
			reg.constraintRegistry.addConstraint(PendingActionConstraint.class)
			mc.getPersistentEntities().each { PersistentEntity pe ->
				reg = pe.mappingContext.validatorRegistry;
				reg.constraintRegistry.addConstraint(PendingActionConstraint.class);
	        }
  		} catch (Exception e) {
  			log.error("Exception thrown registering the pending action constraint", e);
  		}
    }
}
