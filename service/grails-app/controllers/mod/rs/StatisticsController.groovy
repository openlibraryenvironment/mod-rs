package mod.rs

import org.olf.rs.StatisticsService;
import org.olf.rs.logging.ContextLogging;

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

    StatisticsService statisticsService;

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
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_INDEX);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        def result = [
            asAt: new Date(),
            requestsByState: statisticsService.generateRequestsByState(),
            requestsByTag: statisticsService.generateRequestsByStateTag()
        ];

        render result as JSON

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }
}
