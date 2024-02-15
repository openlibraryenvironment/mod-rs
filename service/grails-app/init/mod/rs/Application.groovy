package mod.rs

import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.validation.ValidatorRegistry

import grails.boot.GrailsApp;
import grails.boot.config.GrailsAutoConfiguration;

class Application extends GrailsAutoConfiguration {

  static void main(String[] args) {
    GrailsApp.run(Application, args)
  }

  void doWithApplicationContext() {
    try {
      // Register the pending action constraint
      MappingContext mc = grailsApplication.getMappingContext();
      ValidatorRegistry reg = mc.getValidatorRegistry()
    } catch (Exception e) {
      log.error("Exception thrown", e);
    }
  }

}
