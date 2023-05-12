package mod.rs

import org.olf.rs.logging.ContextLogging;

import grails.core.GrailsApplication;
import grails.plugins.GrailsPluginManager;
import grails.plugins.PluginManagerAware;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "/", tags = ["Application Controller"], description = "API for all things to do with the application")
class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    @ApiOperation(
        value = "The settings for the applications",
        nickname = "rs",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    def index() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_INDEX);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        render view: "index", model: [grailsApplication: grailsApplication, pluginManager: pluginManager]

        // Record how long it took and the request id
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }
}
