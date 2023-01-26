package mod.rs;

import org.olf.rs.statemodel.ActionEvent;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.AvailableAction;
import org.olf.rs.statemodel.GraphVizService;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.StatusService;
import org.olf.rs.statemodel.Transition;

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
@Api(value = "/rs", tags = ["Available Action Controller"], description = "AvailableAction Api")
class AvailableActionController extends OkapiTenantAwareSwaggerController<AvailableAction>  {

    GraphVizService graphVizService;
    StatusService statusService;

	AvailableActionController() {
		super(AvailableAction)
	}

    /**
     * Gets hold of the states an action can be called from
     * Example call: curl --http1.1 -sSLf -H "accept: application/json" -H "Content-type: application/json" -H "X-Okapi-Tenant: diku" --connect-timeout 10 --max-time 30 -XGET http://localhost:8081/rs/availableAction/toStates/Responder/respondYes
     * @return the array of states the action can be called from
     */
    @ApiOperation(
        value = "List the from states that an action can be triggered from",
        nickname = "availableAction/fromStates/{stateModel}/{actionCode}",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "stateModel",
            paramType = "path",
            required = true,
            value = "The state model the action is applicable for",
            dataType = "string",
            defaultValue = "PatronRequest"
        ),
        @ApiImplicitParam(
            name = "actionCode",
            paramType = "path",
            required = true,
            value = "The action that you want to know which states a re applicable for it",
            dataType = "string"
        )
    ])
	def fromStates() {

  		def result = [ : ]
		if (request.method == 'GET') {
			if (params.stateModel && params.actionCode) {
                result.fromStates = AvailableAction.getFromStates(params.stateModel, params.actionCode);
			} else {
				result.message = "Need to supply both action and state model , to see what states this action could transition from";
			}
		} else {
			request.message("Only GET requests are supported");
		}
		render result as JSON;
    }

	/**
	 * Gets hold of the states an action can lead to
	 * Example call: curl --http1.1 -sSLf -H "accept: application/json" -H "Content-type: application/json" -H "X-Okapi-Tenant: diku" --connect-timeout 10 --max-time 30 -XGET http://localhost:8081/rs/availableAction/toStates/Responder/respondYes
	 * @return the array of states a request can end up in after the action has been performed
	 */
    @ApiOperation(
        value = "List the states that a request can end up in after the action has been performed",
        nickname = "availableAction/toStates/{stateModel}/{actionCode}",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "stateModel",
            paramType = "path",
            required = true,
            value = "The state model the action is applicable for",
            dataType = "string",
            defaultValue = "PatronRequest"
        ),
        @ApiImplicitParam(
            name = "actionCode",
            paramType = "path",
            required = true,
            value = "The action that you want to know which states a request could move onto after the action has been performed",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "traverseHierarchy",
            paramType = "query",
            required = true,
            value = "Do we look at the state models we have inherited about",
            dataType = "boolean",
            defaultValue = "true"
        )
    ])
	def toStates() {

		def result = [ : ]
		if (request.method == 'GET') {
			if (params.stateModel && params.actionCode) {
                boolean traverseHierarchy = false;
                if (params.traverseHierarchy) {
                    traverseHierarchy = params.traverseHierarchy.toBoolean();
                }
                List<Transition> transitions = statusService.possibleActionTransitionsForModel(StateModel.lookup(params.stateModel), ActionEvent.lookup(params.actionCode), traverseHierarchy);
                result.toStates = [ ];
                transitions.forEach{transition ->
                    Map coreDetails = [ : ];
                    coreDetails.fromStatus = transition.fromStatus.code;
                    coreDetails.action = transition.actionEvent.code;
                    coreDetails.qualifier = transition.qualifier;
                    coreDetails.toStatus = transition.toStatus.code;
                    result.toStates.add(coreDetails);
                };
			} else {
				result.message = "Need to supply both action and state model , to see what states this action could transition to";
			}
		} else {
			request.message("Only GET requests are supported");
		}
		render result as JSON;
    }

	/**
	 * Builds a graph of the requested state model, excluding any actions / events specified in the query
	 * It saves the .dot and .svg files locally in a directory called D:/Temp/graphviz
	 * Example call: curl --http1.1 -sSLf -H "accept: image/png" -H "X-Okapi-Tenant: diku" --connect-timeout 10 --max-time 300 -XGET http://localhost:8081/rs/availableAction/createGraph/PatronRequest?height=4000\&excludeActions=requesterCancel,manualClose
	 * @return The .dot file that represents the graph
	 */
    @ApiOperation(
        value = "Builds a graph of the requested state model using the DOT language ",
        nickname = "availableAction/createGraph/{stateModel}",
        produces = "text/plain",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "stateModel",
            paramType = "path",
            required = true,
            value = "The state model the graph is for",
            dataType = "string",
            defaultValue = "PatronRequest"
        ),
        @ApiImplicitParam(
            name = "excludeActions",
            paramType = "query",
            required = false,
            value = "A comma separated list of actions that are to be excluded from the chart",
            dataType = "string"
        ),
        @ApiImplicitParam(
            name = "excludeProtocolActions",
            paramType = "query",
            required = false,
            value = "Do we exclude protocol actions or not",
            dataType = "boolean",
            defaultValue = "false"
        ),
        @ApiImplicitParam(
            name = "height",
            paramType = "query",
            required = false,
            value = "The height of the graph",
            dataType = "int",
            defaultValue = "2000"
        ),
        @ApiImplicitParam(
            name = "traverseHierarchy",
            paramType = "query",
            required = true,
            value = "Do we look at the state models we have inherited about",
            dataType = "boolean",
            defaultValue = "true"
        )
    ])
	def createGraph() {

		// Remove messagesAllSeen, messageSeen and message as they occur for all states
		// We also only want to keep those for the state model we are interested in
		String nameStartsWith = "action" + params.stateModel.capitalize();
		List<String> ignoredActions = [
            Actions.ACTION_MESSAGES_ALL_SEEN,
            Actions.ACTION_MESSAGE_SEEN,
            Actions.ACTION_MESSAGE,
            Actions.ACTION_INCOMING_ISO18626
        ];

        boolean traverseHierarchy = false;
        if (params.traverseHierarchy) {
            traverseHierarchy = params.traverseHierarchy.toBoolean();
        }

		if (params.excludeActions) {
			// They have specified some additional actions that should be ignored
			ignoredActions.addAll(params.excludeActions.split(","));
		}

		// Send it straight to the output stream
		OutputStream outputStream = response.getOutputStream();

		// Were we passed a height in the parameters
		int height = 2000;
		if (params.height) {
			try {
				height = params.height as int;
			} catch (Exception e) {
			}
		}

        // Do we want to include the protocol actions
        Boolean includeProtocolActions = !((params.excludeProtocolActions == null) ? false : params.excludeProtocolActions.toBoolean());

		// Tell it to build the graph, it should return the dot file in the output stream
		graphVizService.generateGraph(params.stateModel, includeProtocolActions, ignoredActions, outputStream, height, traverseHierarchy);

		// Hopefully we have what we want in the output stream
		outputStream.flush();
		response.status = 200;
		response.setContentType("text/plain");
	}
}
