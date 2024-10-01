package mod.rs

import org.olf.rs.SettingsService;
import org.olf.rs.logging.ContextLogging;

import com.k_int.okapi.OkapiTenantAwareController;
import com.k_int.web.toolkit.SimpleLookupService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse;

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
    SimpleLookupService simpleLookupService
    SettingsService settingsService

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
        // Setup the variables we want to log
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, resource.getSimpleName());
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_SEARCH);
        ContextLogging.setValue(ContextLogging.FIELD_TERM, params.term);
        ContextLogging.setValue(ContextLogging.FIELD_FIELDS_TO_MATCH, params.match);
        ContextLogging.setValue(ContextLogging.FIELD_FILTERS, params.filters);
        ContextLogging.setValue(ContextLogging.FIELD_SORT, params.sort);
        ContextLogging.setValue(ContextLogging.FIELD_MAXIMUM_RESULTS, max);
        ContextLogging.setValue(ContextLogging.FIELD_NUMBER_PER_PAGE, params.perPage);
        ContextLogging.setValue(ContextLogging.FIELD_OFFSET, params.offset);
        ContextLogging.setValue(ContextLogging.FIELD_PAGE, params.page);
        ContextLogging.setValue(ContextLogging.FIELD_STATISTICS_REQUIRED, params.stats);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        // Check feature flag and handle 404 if necessary
        if (isFeatureDisabled(request, response)) {
            return
        }

        super.index(max);

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    private boolean isFeatureDisabled(HttpServletRequest request, HttpServletResponse response) {
        // Get URI without query parameters
        String uri = request.getRequestURI()

        // Extract the last segment after /rs/
        String lastSegmentAfterRs = extractLastSegmentAfterRs(uri)

        // Construct the feature flag key and get possible value
        String featureFlagKey = lastSegmentAfterRs + ".featureFlag"
        String featureFlagValue = settingsService.getFeatureFlagValue(featureFlagKey)

        // If the feature flag is set and is "false", return 404
        if (featureFlagValue != null && featureFlagValue == "false") {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND)
            return true // Indicate that feature is disabled
        }
        return false // Feature is enabled
    }

    private static String extractLastSegmentAfterRs(String uri) {
        String pattern = "/rs/"
        int index = uri.indexOf(pattern)
        if (index >= 0) {
            // Extract everything after "/rs/"
            String afterRs = uri.substring(index + pattern.length())
            // Split path into segments
            String[] pathSegments = afterRs.split("/")
            // Return the last segment
            return pathSegments.length > 0 ? pathSegments[pathSegments.length - 1] : null
        }
        return null
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
        // Setup the variables we want to log
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, resource.getSimpleName());
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_FETCH);
        ContextLogging.setValue(ContextLogging.FIELD_ID, params.id);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        // Now do the work
        super.show();

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
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
