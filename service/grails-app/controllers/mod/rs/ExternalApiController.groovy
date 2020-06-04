package mod.rs

import org.olf.okapi.modules.directory.DirectoryEntry

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.GlobalConfigService
import org.olf.rs.BackgroundTaskService;
import org.olf.rs.ReshareApplicationEventHandlerService
import grails.gorm.multitenancy.Tenants
import java.text.SimpleDateFormat
import groovy.xml.StreamingMarkupBuilder
import grails.gorm.multitenancy.Tenants
import groovy.util.logging.Slf4j
import org.olf.rs.Counter



/**
 * External Read-Only APIs for resource sharing network connectivity
 */
@Slf4j
@CurrentTenant
class externalApi {

  GrailsApplication grailsApplication
  GlobalConfigService globalConfigService

  def index() {
  }

  def statistics() {

    def result=[
    ]
   
    try {
      result = [
        asAt:new Date(),
        current:Counter.list().collect { [ context:it.context, value:it.value, description:it.description ] }
      ]
    }
    catch ( Exception e ) {
      result.error=e.message;
    }

    render result as JSON
  }


  // Experiment
  def iso18626() {
    log.debug("externalApi::iso18626(${tenant})");

    render [
      status:'OK'
    ] as JSON
  }
}
