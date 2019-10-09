package mod.rs

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.GlobalConfigService
import org.olf.rs.BackgroundTaskService;

class iso18626Controller {

  GrailsApplication grailsApplication

  def index() {
    def result=[status:'ok']
    log.debug("iso18626Controller::index(${params})");
    render result as JSON
  }
}
