package mod.rs

import grails.gorm.multitenancy.CurrentTenant
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import grails.converters.JSON
import org.olf.rs.Counter

@Slf4j
@CurrentTenant
class StatisticsController {
  
  long totalBorrowing=10
  long totalLending=5

  def index() {

    def result = [
      asAt:new Date(),
      current:Counter.list().collect { [ context:it.context, value:it.value, description:it.description ] }
    ]

    render result as JSON
  }
  
}
