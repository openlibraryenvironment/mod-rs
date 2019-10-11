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
    log.debug("XML: ${request.XML}");
    render result as JSON
  }

  def symbol() {
    def result=[status:'ok']
    log.debug("iso18626Controller::symbol(${params})");
    render result as JSON
  }
}
