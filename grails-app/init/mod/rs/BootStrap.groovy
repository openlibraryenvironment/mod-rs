package mod.rs

class BootStrap {

  def grailsApplication

  def init = { servletContext ->
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
