package mod.rs;

import org.olf.rs.logging.ContextLogging;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import grails.converters.JSON;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Slf4j
@Api(value = "/rs/logging", tags = ["Logging Controller"], description = "Logging Api")
class LoggingController {

    /**
     * allows you to change the loglevel for a class or if no class is specified the root class
     * @return the old and new log level for the specified class
     */
    @ApiOperation(
        value = "Sets the log level for the specified class, returning what it was set to previously",
        nickname = "/",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "classPath",
            paramType = "query",
            required = false,
            value = "The class you want to set or obtain the log level for",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "logLevel",
            paramType = "query",
            required = false,
            value = "The log level that you want to set the specified class path to",
            allowableValues = "ALL,DEBUG,DEFAULT,ERROR,INFO,OFF,TRACE,WARN",
            dataType = "string"
        )
    ])
	def index() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_INDEX);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        Map result = [ : ];

        // This always gives us a logger, it swwms if it dosn't exist it creates it
        Logger logger = LoggerFactory.getLogger(params.classPath ? params.classPath : Logger.ROOT_LOGGER_NAME);

        // Return the previous log level
        result.previousLogLevel = logger.getLevel().toString();

        // Check if we have a valid log level
        if (params.logLevel) {
            // They want to set the log level
            if ("DEFAULT".equals(params.logLevel)) {
                if (logger.name.equals(Logger.ROOT_LOGGER_NAME)) {
                    // Cannot set the root logger to default
                    result.error = "Cannot set the root logger to DEFAULT";
                } else {
                    // Clear the log level, so that it picks it up from the parent
                    logger.setLevel(null);
                }
            } else {
                // It is a standard log level
                logger.setLevel(Level.toLevel(params.logLevel));
            }
        }

        // Return the current log level
        result.currentLogLevel = logger.getLevel().toString();

        // The effective logging level
        result.effectiveLogLevel = logger.getEffectiveLevel().toString();

        // Finally return the name of the logger
        result.loggerName = logger.name;

		render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }
}
