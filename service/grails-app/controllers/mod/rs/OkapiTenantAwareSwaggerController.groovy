package mod.rs;

import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

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
    def createResource() {
        super.createResource(request.JSON);
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
            required = false,
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
        super.update();
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
            required = false,
            allowMultiple = false,
            value = "The id of the record to be deleted",
            dataType = "string"
        )
    ])
    def delete() {
        super.delete();
    }
}
