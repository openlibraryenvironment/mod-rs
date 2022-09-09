package mod.rs;

import org.olf.rs.EmailService;
import org.olf.rs.statemodel.ActionEvent;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.AvailableAction;
import org.olf.rs.statemodel.GraphVizService;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.StatusService;
import org.olf.rs.statemodel.Transition;

import com.k_int.okapi.OkapiTenantAwareController;

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
class AvailableActionController extends OkapiTenantAwareController<AvailableAction>  {

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
        )
    ])
	def toStates() {

		def result = [ : ]
		if (request.method == 'GET') {
			if (params.stateModel && params.actionCode) {
                List<Transition> transitions = statusService.possibleActionTransitionsForModel(StateModel.lookup(params.stateModel), ActionEvent.lookup(params.actionCode));
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
		graphVizService.generateGraph(params.stateModel, includeProtocolActions, ignoredActions, outputStream, height);

		// Hopefully we have what we want in the output stream
		outputStream.flush();
		response.status = 200;
		response.setContentType("text/plain");
	}

    org.olf.rs.reporting.JasperReportService jasperReportService;
    EmailService emailService

    /**
     * Tests the jasper reports functionality by running the pullslip report and saving it to D:/Temp/TestPullSlip.pdf
     * Example call: curl --http1.1 -sSLf -H "accept: application/json" -H "X-Okapi-Tenant: diku" --connect-timeout 10 --max-time 300 -XGET http://localhost:8081/rs/availableAction/testReport?id=c2bc1883-5d10-4fb3-ad84-5120f743ffca&id=7a42ed5a-9608-4bef-9ba2-3cc79a377d47
     * The report is saved to the file D:/Temp/TestPullSlip.pdf and emailed to chas.
     */
    @ApiOperation(
        value = "Exercises the jasper report service ",
        nickname = "availableAction/testReport",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "id",
            paramType = "query",
            allowMultiple = true,
            required = true,
            value = "The id(s) that the picklist is to be printed for",
            dataType = "string"
        )
    ])
    def testReport() {
        String schema = "diku_mod_rs";
        List ids;
        if (params.id == null) {
            ids = new ArrayList();
        } else if (params.id instanceof String) {
            ids = new ArrayList();
            ids.add(params.id);
        } else {
            // it must be an array
            ids = params.id;
        }
        String outputFilename = 'D:/Temp/TestPullSlip.pdf';
        org.olf.rs.reports.Report report = org.olf.rs.reports.Report.lookupPredefinedId(org.olf.rs.referenceData.ReportData.ID_PATRON_REQUEST_PULL_SLIP_1);
        OutputStream outputStream = new FileOutputStream(new File(outputFilename));
        try {
            jasperReportService.executeReport(report.id, schema, outputStream, ids);
        } catch (Exception e) {
            log.error("Exception thrown generating report", e);
        } finally {
            outputStream.close();
        }

        // In order to test this ensure you have configured mod-email
        // also need to go through okapi, rather than local otherwise it will not find mod-email
        File file = new File(outputFilename);
        byte[] binaryContent = file.bytes;
        String encoded = binaryContent.encodeBase64().toString();
        Map emailParamaters = [
            notificationId: '1',
            to: 'chaswoodfield@gmail.com',
            header: 'Has the pull slip attached',
            body: 'Will it get through',
            outputFormat: 'text/plain',
            attachments: [
                [
                    contentType: 'application/pdf',
                    name: 'Pull Slip',
                    description: 'This is a Pull Slip',
                    data: encoded,
                    disposition: 'base64'
                ]
            ]
        ];

        // Send an email with the pull slip in
        emailService.sendEmail(emailParamaters);
    }
}
