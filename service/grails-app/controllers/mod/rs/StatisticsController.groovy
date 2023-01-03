package mod.rs

import org.olf.rs.Counter;
import org.olf.rs.PatronRequest;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Slf4j
@CurrentTenant
@Api(value = "/rs/", tags = ["Statistics Controller"], description = "Statistics Api")
class StatisticsController {

    long totalBorrowing=10
    long totalLending=5

    @ApiOperation(
        value = "Generates the statistics for this tenant",
        nickname = "statistics",
        httpMethod = "GET",
        produces = "application/json"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    def index() {

        def result = [
            asAt:new Date(),
            current:Counter.list().collect { [ context:it.context, value:it.value, description:it.description ] },
            requestsByState:generateRequestsByState()
        ];

        render result as JSON
    }

    private Map generateRequestsByState() {
        Map result = [:]
        PatronRequest.executeQuery('select pr.stateModel.shortcode, pr.state.code, count(pr.id) from PatronRequest as pr group by pr.stateModel.shortcode, pr.state.code').each { sl ->
            result[sl[0]+':'+sl[1]] = sl[2]
        }
        return result;
    }
}
