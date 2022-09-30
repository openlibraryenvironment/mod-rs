package mod.rs

import org.olf.rs.Counter
import org.olf.rs.PatronRequest;

import grails.converters.JSON
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j

@Slf4j
@CurrentTenant
class StatisticsController {

  long totalBorrowing=10
  long totalLending=5

  def index() {

    def result = [
      asAt:new Date(),
      current:Counter.list().collect { [ context:it.context, value:it.value, description:it.description ] },
      requestsByState:generateRequestsByState()
    ]

    render result as JSON
  }

  private Map generateRequestsByState() {
    Map result = [:]
    PatronRequest.executeQuery('select pr.stateModel.shortcode, pr.state.code, count(pr.id) from PatronRequest as pr group by pr.stateModel, pr.state.code').each { sl ->
      result[sl[0]+':'+sl[1]] = sl[2]
    }
    return result;
  }

}
