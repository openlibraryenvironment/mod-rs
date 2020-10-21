package mod.rs

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON
import org.olf.rs.BackgroundTaskService;

class ReshareSettingsController {

  GrailsApplication grailsApplication
  GrailsPluginManager pluginManager
  BackgroundTaskService backgroundTaskService

  def worker() {
    def result = [result:'OK']
    String tenant_header = request.getHeader('X-OKAPI-TENANT')
    log.debug("Worker thread invoked....${tenant_header}");
    backgroundTaskService.performReshareTasks(tenant_header+'_mod_rs');
    render result as JSON
  }
}
