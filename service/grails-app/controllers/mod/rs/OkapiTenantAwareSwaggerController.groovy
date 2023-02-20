package mod.rs;

import org.olf.rs.logging.ContextLogging;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

class OkapiTenantAwareSwaggerController<T> extends OkapiTenantAwareSwaggerGetController<T>  {

    OkapiTenantAwareSwaggerController(Class<T> resource, int maxRecordsPerPage = 1000) {
        this(resource, false, maxRecordsPerPage);
    }

    OkapiTenantAwareSwaggerController(Class<T> resource, boolean readOnly, int maxRecordsPerPage = 1000) {
        super(resource, readOnly, maxRecordsPerPage);
    }

    @ApiOperation(
        value = "Creates a new record with the supplied data",
        nickname = "/",
        httpMethod = "POST"
    )
    @ApiResponses([
        @ApiResponse(code = 201, message = "Created")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            paramType = "body",
            required = true,
            allowMultiple = false,
            value = "The json record that is going to be used for creation",
            defaultValue = "{}",
            dataType = "string"
        )
    ])
    def save() {
        // Setup the variables we want to log
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, resource.getSimpleName());
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_CREATE);
        ContextLogging.setValue(ContextLogging.FIELD_JSON, request.JSON);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        // Now do the work
        super.save();

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    @ApiOperation(
        value = "Updates the record with the supplied data",
        nickname = "{id}",
        produces = "application/json",
        httpMethod = "PUT"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "id",
            paramType = "path",
            required = true,
            allowMultiple = false,
            value = "The id of the record to be updated",
            dataType = "string"
        ),
        @ApiImplicitParam(
            paramType = "body",
            required = true,
            allowMultiple = false,
            value = "The json record that we are going to update the current record with",
            defaultValue = "{}",
            dataType = "string"
        )
    ])
    def update() {
        // Setup the variables we want to log
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, resource.getSimpleName());
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_UPDATE);
        ContextLogging.setValue(ContextLogging.FIELD_ID, params.id);
        ContextLogging.setValue(ContextLogging.FIELD_JSON, request.JSON);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        // Now do the work
        super.update();

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    @ApiOperation(
        value = "Deletes the record with the supplied identifier",
        nickname = "{id}",
        httpMethod = "DELETE"
    )
    @ApiResponses([
        @ApiResponse(code = 204, message = "Deleted")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "id",
            paramType = "path",
            required = true,
            allowMultiple = false,
            value = "The id of the record to be deleted",
            dataType = "string"
        )
    ])
    def delete() {
        // Setup the variables we want to log
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, resource.getSimpleName());
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_DELETE);
        ContextLogging.setValue(ContextLogging.FIELD_ID, params.id);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        // Now do the work
        super.delete();

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }
}
