package mod.rs

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
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }
}
