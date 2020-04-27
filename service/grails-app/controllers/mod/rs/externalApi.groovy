package mod.rs

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
class externalApi {

  GrailsApplication grailsApplication
  GlobalConfigService globalConfigService

  def index() {
  }

  def statistics(String tenant) {

    def result=[
      getStatistics:tenant
    ]
   
    try {
      Tenants.withId(tenant) {
        result = [
          asAt:new Date(),
          current:Counter.list().collect { [ context:it.context, value:it.value, description:it.description ] }
        ]
      }
    }
    catch ( Exception e ) {
      result.error=e.message;
    }

    render result as JSON
  }

  def directoryIndex(String tenant) {
    def result =  [
      status:'OK',
      tenant: tenant
    ] 
    render result as JSON;
  }

  def directoryEntry(String tenant) {
    def result =  [
      status:'OK',
      tenant: tenant
    ] 
    render result as JSON;
  }

}
