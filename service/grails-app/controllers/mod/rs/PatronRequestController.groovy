package mod.rs;

import org.olf.rs.Batch;
import org.olf.rs.BatchService;
import org.olf.rs.OpenUrlService;
import org.olf.rs.PatronRequest;
import org.olf.rs.Result;
import org.olf.rs.reporting.ReportService;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionService;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Slf4j
@CurrentTenant
@Api(value = "/rs/patronrequests", tags = ["Patron Request Controller"], description = "API for all things to do with patron requests")
class PatronRequestController extends OkapiTenantAwareSwaggerController<PatronRequest>  {

	ActionService actionService;
    BatchService batchService;
    OpenUrlService openUrlService;
    ReportService reportService;

	PatronRequestController() {
		super(PatronRequest, 100);
	}

    /**
     *  Controller action that takes a POST containing a json payload with the following parameters
     *   {
     *     action:"StartRota",
     *     actionParams:{}
     *   }
     */
    @ApiOperation(
        value = "Performs the action the posted action for the given request",
        nickname = "{patronRequestId}/performAction",
        produces = "application/json",
        httpMethod = "POST",
        notes = 'Need to describe the actions here some how'
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "patronRequestId",
            paramType = "path",
            required = true,
            value = "The identifier of the request that the action is to be performed on",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "actionParameters",
            paramType = "body",
            required = false,
            value = "The parameters required to perform the action, in a json format",
            dataType = "string"
        )
    ])
	def performAction() {

		def result = [:]
		if ( request.method=='POST' ) {
			log.debug("PatronRequestController::performAction(${request.JSON})...");
			if ( params.patronRequestId ) {
				PatronRequest.withTransaction { tstatus ->
                    // Execute the action
                    result = actionService.executeAction(params.patronRequestId, request.JSON.action, request.JSON.actionParams);
                    response.status = (result.actionResult == ActionResult.SUCCESS ? 200 : (result.actionResult == ActionResult.INVALID_PARAMETERS ? 400 : 500));

                    // We do not want to pass the internal action result back to the caller, so we need to remove it
                    result.remove('actionResult');
				}
			}
		}
		log.debug("PatronRequestController::performAction exiting");
		render result as JSON;
    }

    /**
     * Allows a limited number of fields on a request to be updated, why isn't this implemented as an action,
     * to all intents and purposes I will implement this as an action so it should be straight forward to swap it over
     */
    def update() {
        def result = [:]
        log.debug("PatronRequestController::update(${request.JSON})...");
        if ( params.id ) {
            PatronRequest.withTransaction { tstatus ->
                // Execute the action
                result = actionService.executeAction(params.id, Actions.ACTION_REQUESTER_EDIT, request.JSON);
                response.status = (result.actionResult == ActionResult.SUCCESS ? 200 : (result.actionResult == ActionResult.INVALID_PARAMETERS ? 400 : 500));

                // We do not want to pass the internal action result back to the caller, so we need to remove it
                result.remove('actionResult');
            }
        }
        log.debug("PatronRequestController::update exiting");
        render result as JSON;
    }

    /**
     *  Controller action that takes a POST containing a json payload with the following parameters
     *   {
     *     target:{
     *       requestList:[uuid-123,uuid-456,uuid-788], // NOT IMPLEMENTED YET
     *       query:"title=%"  // NOT IMPLEMENTED YET
     *     }
     *     action:"StartRota",
     *     actionParams:{}
     *   }
     */
    @ApiOperation(
        value = "Performs the specified action against the specified requests (NOT IMPLEMENTED)",
        nickname = "bulkAction",
        produces = "application/json",
        httpMethod = "POST"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
  	def bulkAction() {
		  def result = [:]
		  render result as JSON;
	  }

    /**
     * list the valid actions for this request
     */
    @ApiOperation(
        value = "List the actions that are available for the request",
        nickname = "{patronRequestId}/validActions",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "patronRequestId",
            paramType = "path",
            required = true,
            value = "The identifier of the request that the valid actions are required for",
            dataType = "string"
        )
    ])
	def validActions() {
		log.debug("PatronRequestController::validActions() ${params}");
		def result = [:];

		if ( params.patronRequestId ) {
			PatronRequest patron_request = PatronRequest.get(params.patronRequestId)

			if (  patron_request != null ) {
				result.actions = actionService.getValidActions(patron_request);
			} else {
				result.actions=[];
				result.message="Unable to locate request for ID ${params.patronRequestId}";
			}
		} else {
			result.actions=[];
			result.message="No ID provided in call to validActions";
		}

		render result as JSON
	}

    /**
     * list the close states that are valid for for this request
     */
    @ApiOperation(
        value = "List the close states that are valid for the request",
        nickname = "{patronRequestId}/manualCloseStates",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "patronRequestId",
            paramType = "path",
            required = true,
            value = "The identifier of the request that the close states are required for",
            dataType = "string"
        )
    ])
    def manualCloseStates() {
        log.debug("PatronRequestController::manualCloseStates() ${params}");
        def result = [ ];

        // Cannot proceed if we do not have a requestId
        if (params.patronRequestId) {
            // Grab the request
            PatronRequest request = PatronRequest.get(params.patronRequestId);

            // Did we find the request
            if (request != null) {
                // Get hold of the states from the model
                result = StateModel.getVisibleTerminalStates(request.stateModel.shortcode);
            }
        }
        render result as JSON
    }

    /**
     * Generates a batch from the passed in list of filters
     */
    @ApiOperation(
        value = "Generates the pick list batch based on the passed in filter",
        nickname = "generatePickListBatch",
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
            name = "filters",
            paramType = "query",
            required = false,
            allowMultiple = true,
            value = "The filters to be applied",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "match",
            paramType = "query",
            required = false,
            allowMultiple = true,
            value = "The properties the match is to be applied to",
            dataType = "string"
        )
    ])
    def generatePickListBatch() {
        List<String> filters = getParamList("filters");
        List<String> searchFields = getParamList("match");
        String term = params.term;
        Map result = [ : ];

        Batch.withTransaction { tstatus ->
            // Generate the batch for the pick list
            result = batchService.generatePickListBatchFromFilter(
                term,
                searchFields,
                filters,
                reportService.getMaxRequestsInPullSlipManual(),
                "User generated pick list:",
                true
            );
        }

        // Give the result back to the caller
        render result as JSON, status: result.error ? 400 : 200, contentType: "application/json";
    }

    /**
     * Marks the requests in a batch as being printed if that action is valid for the request
     * This will return the following arrays:
     *  1. The request ids that were successfully marked as printed
     *  2. The request ids that failed to be marked as printed that were valid to mark as printed
     *  3. The requests ids that were not valid to mark as printed
     */
    @ApiOperation(
        value = "Actions the requests in the batch as printed",
        nickname = "markBatchAsPrinted",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 400, message = "Invalid parameters have been supplied")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "batchId",
            paramType = "query",
            required = true,
            allowMultiple = false,
            value = "The batch to mark the requests it contains as printed",
            dataType = "string"
        )
    ])
    def markBatchAsPrinted() {
        Map result = [ : ];
        int resultStatus = 200;

        // Have we been supplied a batch id
        if (params.batchId) {
            // Good start, so get hod of the batch
            Batch batch = Batch.get(params.batchId);

            // Did we find the batch
            if (batch) {
                // Start a transaction to perform the updates against
                PatronRequest.withTransaction { tstatus ->
                    result = batchService.markRequestsInBatchAsPrinted(batch);
                }
            } else {
                // No we did not
                result.error = "unknown batch id supplied: " + params.batchId;
                resultStatus = 400;
            }
        } else {
            result.error = "No batch id was supplied";
            resultStatus = 400;
        }

        render result as JSON, status: resultStatus, contentType: "application/json";
    }

    /**
     * Receives an OpenURL either version 0.1 or 1.0
     */
    @ApiOperation(
        value = "Interprets an OpenUrl version 0.1 or 1.0",
        nickname = "openURL",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "req.emailAddress",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: EMail address of the patron",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "req.id",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Id of this request",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.artnum",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Article number",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.atitle",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Article title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.au",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Author",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.aufirst",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Author first name",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.auinitl",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: First author initials",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.auinit",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Author initials",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.auinitm",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Author middle initial",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.aulast",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Author last name",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.bici",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Bici",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.btitle",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Book title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.coden",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Coden",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.creator",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Creator",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.edition",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Edition",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.eissn",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: EISSN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.genre",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: ",
            dataType = "Genre"
        ),
        @ApiImplicitParam(
            name = "rft.id",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Identifier of the item",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.identifier.illiad",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Illiad identifier",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.isbn",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: ISBN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.issn",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: ISSN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.issue",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Issue",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.jtitle",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Journal title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.epage",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Last page number",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.spage",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Start page",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.pages",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Number of pages",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.part",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Part",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.date",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Publication dste",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.place",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Publication place",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.pub",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Publisher",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.quarter",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Quarter",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.sici",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: SICI",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.ssn",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: SSN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.title",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "rft.volume",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Volume",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "svc.neededBy",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Needed by",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "svc.note",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Note",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "svc.pickupLocation",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Pickup location",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "svc.id",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Service type",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "artnum",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v1.0: Article number",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "aufirst",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Author first name",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "auinitl",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Author first initial",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "auinit",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Author middle initial",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "aulast",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Author last name",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "auinitm",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Author middle initial",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "bici",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: BICI",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "coden",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Coden",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "genre",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Genre",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "issn",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: ISSN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "eissn",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: EISSN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "isbn",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: ISBN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "issue",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Issue",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "epage",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: End page",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "spage",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Start page",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "pages",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Number of pages",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "part",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Part",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "pickupLocation",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Pickup location",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "date",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Publication date",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "quarter",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Quarter",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "ssn",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: SSN",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "sici",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: SICI",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "title",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "stitle",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Abbreviated title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "atitle",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Article Title",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "volume",
            paramType = "query",
            allowMultiple = false,
            required = false,
            value = "v0.1: Volume",
            dataType = "string"
        )
    ])
    def openURL() {
        // Maps an OpenURL onto a request, originally taken from here https://github.com/openlibraryenvironment/listener-openurl/blob/master/src/ReshareRequest.js
        Result result = openUrlService.mapToRequest(params) ;
        response.status = 200;

        render result as JSON
    }
}
