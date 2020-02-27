package mod.rs

import grails.gorm.multitenancy.CurrentTenant
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import grails.converters.JSON

@Slf4j
@CurrentTenant
class StatisticsController {
  
  def index() {
    def result = [:]
    render result as JSON
  }
  
}
