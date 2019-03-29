package mod.rs

class BootStrap {

  def grailsApplication
  def housekeepingService

  def init = { servletContext ->

    housekeepingService.ensureSharedSchema();

    if ( grailsApplication.config.rabbitmq?.enabled ) {
      log.debug("mod-rs starting, with rabbitmq enabled");
    }
    else {
      log.debug("mod-rs starting, with rabbitmq disabled");
    }
  }

  def destroy = {
  }
}
