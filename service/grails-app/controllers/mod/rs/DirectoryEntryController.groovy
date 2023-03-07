package mod.rs

import org.olf.okapi.modules.directory.DirectoryEntry;

import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses

/**
 * Access to InternalContact resources
 */
@Slf4j
@CurrentTenant
@Api(value = "/rs/directoryEntry", tags = ["Directory Entry Controller"], description = "API for all things to do with directory entries")
class DirectoryEntryController extends OkapiTenantAwareSwaggerController<DirectoryEntry>  {

    DirectoryEntryController() {
        super(DirectoryEntry)
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
        ),
        @ApiImplicitParam(
            name = "full",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Do we want the full details being output",
            defaultValue = "false",
            dataType = "boolean"
        )
    ])
    def index(Integer max) {
        // Just call the base class
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
        ),
        @ApiImplicitParam(
            name = "full",
            paramType = "query",
            required = false,
            allowMultiple = false,
            value = "Do we want the full details being output",
            defaultValue = "false",
            dataType = "boolean"
        )
    ])
    def show() {
        // Just call the base class
        super.show();
   }
}
