package mod.rs

import grails.boot.GrailsApp;
import grails.boot.config.GrailsAutoConfiguration;
import org.grails.datastore.gorm.validation.constraints.registry.DefaultConstraintRegistry;
import org.grails.datastore.gorm.validation.constraints.registry.ConstraintRegistry;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.gorm.validation.constraints.registry.DefaultValidatorRegistry;
import grails.util.Holders;
import org.olf.rs.PendingActionConstraint;

class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    void doWithApplicationContext() {
  		try {
  			MappingContext mc = grailsApplication.mainContext.getBean('grailsDomainClassMappingContext')
        DefaultValidatorRegistry reg = mc.validatorRegistry
        reg.constraintRegistry.addConstraint(PendingActionConstraint.class)
                
        mc.getPersistentEntities().each { PersistentEntity pe ->
          reg = pe.mappingContext.validatorRegistry
          reg.constraintRegistry.addConstraint(PendingActionConstraint.class)
        }
  		} catch (Exception e) {
  			def chas = 1;
  		}
    }
}
