package mod.rs;

import com.k_int.okapi.OkapiTenantAwareController;

import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

class OkapiTenantAwareSwaggerController<T> extends OkapiTenantAwareController<T>  {

    OkapiTenantAwareSwaggerController(Class<T> resource) {
        this(resource, false);
    }

    OkapiTenantAwareSwaggerController(Class<T> resource, boolean readOnly) {
        super(resource, readOnly);
    }

    @ApiOperation(
        value = "Search with the supplied criteria",
        nickname = "/",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "term",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "The term to be searched for",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "match",
            paramType = "query",
            required = false,
            allowMultiple = true,
            value = "The properties the match is to be applied to",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "filters",
            paramType = "query",
            required = false,
            allowMultiple = true,
            value = "The filters to be applied",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "sort",
            paramType = "query",
            required = false,
            allowMultiple = true,
            value = "The properties to sort the items by",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "max",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Maximum number of items to return",
            dataType = "int"
        ),
        @ApiImplicitParam(
            name = "perPage",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Number of items per page",
            dataType = "int"
        ),
        @ApiImplicitParam(
            name = "offset",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Offset from the becoming of the result set to start returning results",
            dataType = "int"
        ),
        @ApiImplicitParam(
            name = "page",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "The page you wnat the results being returned from",
            dataType = "int"
        ),
        @ApiImplicitParam(
            name = "stats",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Do we return statistics about the search",
            dataType = "boolean"
        )
    ])
    def index(Integer max) {
        super.index(max);
    }

    @ApiOperation(
        value = "Returns the supplied record",
        nickname = "{id}",
        httpMethod = "GET"
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
            value = "The id of the record to return",
            dataType = "string"
        )
    ])
    def show() {
        super.show();
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
