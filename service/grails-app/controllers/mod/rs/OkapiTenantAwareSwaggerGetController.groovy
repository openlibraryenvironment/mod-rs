package mod.rs;

import com.k_int.okapi.OkapiTenantAwareController;
import com.k_int.web.toolkit.SimpleLookupService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class was written so that we had a swagger interface for all the controller end points
 * that are implemented byOkapiTenantAwareController, with one addition in that we override doTheLookup
 * so that we can change the maximum number of records that can be returned per page
 * @author Chas
 *
 * @param <T> The class of record that we are offering the end points for
 */
class OkapiTenantAwareSwaggerGetController<T> extends OkapiTenantAwareController<T>  {

    // Required as we override the maximum number of records that can be returned in index
    SimpleLookupService simpleLookupService;

    /** Specifies the maximum number of records to return for a page */
    private int maxRecordsPerPage = 1000;

    OkapiTenantAwareSwaggerGetController(Class<T> resource, int maxRecordsPerPage = 1000) {
        this(resource, false, maxRecordsPerPage);
    }

    OkapiTenantAwareSwaggerGetController(Class<T> resource, boolean readOnly, int maxRecordsPerPage = 1000) {
        super(resource, readOnly);
        this.maxRecordsPerPage = maxRecordsPerPage;
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

    @Override
    /**
     * We override this method so we can vary what the maximum number of items that get returned is.
     * So for a large record we can restrict it to the default of 100 that is in the library
     * and for smaller records we can make it a larger number.
     * The method was copied from com.k_int.web.toolkit.rest.RestfulController.groovy with maxRecordsPerPage replacing 100
     * Note: simpleLookupService still caps it at 1000 regardless
     *
     * @param res The class that we are returning records for
     * @param baseQuery A closure that contains any extra queries beyond what has been asked for in the parameters
     * @return The results of the query
     */
    protected def doTheLookup (Class<T> res = this.resource, Closure baseQuery) {
        final int offset = params.int("offset") ?: 0;
        final int perPage = Math.min(params.int('perPage') ?: params.int('max') ?: 10, maxRecordsPerPage);
        final int page = params.int("page") ?: (offset ? (offset / perPage) + 1 : 1);
        final List<String> filters = getParamList("filters");
        final List<String> match_in = getParamList("match");
        final List<String> sorts = getParamList("sort");

        if (params.boolean('stats')) {
            return(simpleLookupService.lookupWithStats(res, params.term, perPage, page, filters, match_in, sorts, null, baseQuery));
        } else {
            return(simpleLookupService.lookup(res, params.term, perPage, page, filters, match_in, sorts, baseQuery));
        }
    }
}
