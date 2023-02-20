package mod.rs;

import org.grails.web.json.JSONArray;
import org.olf.rs.logging.ContextLogging;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.StateModelService;

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
@Api(value = "/rs/stateModel", tags = ["State Model Controller"], description = "StateModel Api")
class StateModelController extends OkapiTenantAwareController<StateModel>  {

    private static final String RESOURCE_STATE_MODEL = StateModel.getSimpleName();

    StateModelService stateModelService;

	StateModelController() {
		super(StateModel)
	}

	/**
	 * Exports the tables that defines the state models, if a state model is specified in the parameters then only that state model is output
	 * Ids are not returned only the codes at the various levels
	 */
    @ApiOperation(
        value = "Exports the full definitions of the state models, without any the ids (just the codes), if the stateModel query parameter is specified then we only return that state model",
        nickname = "export",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "stateModel",
            paramType = "query",
            required = false,
            value = "The state model code ",
            dataType = "string",
            defaultValue = "PatronRequest"
        )
    ])
	def export() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, RESOURCE_STATE_MODEL);
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_EXPORT);
        log.debug(ContextLogging.MESSAGE_ENTERING);

		Map result = [ : ]
        result.stateModels = [ ];

        // Have we been supplied a state model
		if (params.stateModel) {
            // Have we been supplied a valid code
            StateModel stateModel = StateModel.lookup(params.stateModel);
            if (stateModel == null) {
                // No we havn't
                result.message("No state model with code \"" + params.stateModel + "\" exists");
            } else {
                // Excellent start we have a state model to be returned
                log.info("Exporting state model: " + stateModel.shortcode);
                result.stateModels.add(stateModelService.exportStateModel(stateModel));
            }
		} else {
            // We need to return all the state models
            StateModel.findAll().each { stateModel ->
                log.info("Exporting state model: " + stateModel.shortcode);
                result.stateModels.add(stateModelService.exportStateModel(stateModel));
            }
		}

        // Are we returning any state models
        if (result.stateModels) {
            // Now add all the other data
            result.actions = stateModelService.exportActionEvents(Boolean.TRUE);
            result.events = stateModelService.exportActionEvents(Boolean.FALSE);
            result.stati = stateModelService.exportStati();
            result.actionEventResultLists = stateModelService.exportActionEventResultLists();
            result.actionEventResults = stateModelService.exportActionEventResults();
        }

        // Just return the result as json
		render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    /**
     *  Imports the state model and all the associated reference data from a JSON payload,
     *  I would call the method import but that is a reserved word
     */
    @ApiOperation(
        value = "Imports the json file that is equivalent to what is created by an export",
        nickname = "import",
        produces = "application/json",
        httpMethod = "POST"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "stateModel",
            paramType = "body",
            required = true,
            value = "The JSON that contains the state model(s) that needs importing",
            dataType = "string",
            defaultValue = '''{
    stati: [],
    actionEventResults: [],
    actionEventResultLists: [],
    actions: [],
    events: [],
    stateModels: []
}''')
    ])
    def ingest() {
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_RESOURCE, RESOURCE_STATE_MODEL);
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_IMPORT);
        log.debug(ContextLogging.MESSAGE_ENTERING);

        // Define our result object
        def result = [ : ];
        result.errors = [ ];

        // Have we been supplied some json
        if (request.JSON) {
            try {
                // Start a transaction
                StateModel.withTransaction { transactionStatus ->

                    log.info("Importing state models");

                    // import the stati
                    if (checkIsJsonArray(request.JSON.stati, "stati", result.errors)) {
                        result.stati = stateModelService.importStati(request.JSON.stati);
                    }

                    // Import the action / event results
                    if (checkIsJsonArray(request.JSON.actionEventResults, "actionEventResult", result.errors)) {
                        result.actionEventResults = stateModelService.importActionEventResults(request.JSON.actionEventResults);
                    }

                    // Action / Event Result List
                    if (checkIsJsonArray(request.JSON.actionEventResultLists, "actionEventResultLists", result.errors)) {
                        result.actionEventResultLists = stateModelService.importActionEventResultLists(request.JSON.actionEventResultLists);
                    }

                    // Actions
                    if (checkIsJsonArray(request.JSON.actions, "actions", result.errors)) {
                        result.actions = stateModelService.importActionEvents(request.JSON.actions, true);
                    }

                    // Events
                    if (checkIsJsonArray(request.JSON.events, "events", result.errors)) {
                        result.events = stateModelService.importActionEvents(request.JSON.events, false);
                    }

                    // State models
                    if (checkIsJsonArray(request.JSON.stateModels, "stateModels", result.errors)) {
                        result.stateModels = stateModelService.importStateModels(request.JSON.stateModels);
                    }
                }
            } catch (Exception e) {
                result.errors.add("Exception thrown while trying to import state model: " + e.message);
            }
        } else {
            // No state model supplied
            result.errors.add("No state model supplied");
        }

        // log the result
        log.info("Results from importing state models:\n" + result.toString());

        // Just return the result as json
        render result as JSON;

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);
    }

    /**
     * Checks to see if we have a JSONArray in our hand, adding a message if we do not
     * @param potentialArray The object we need to test to see if it is an array
     * @param property The name of the property it has come from
     * @param errors The list of errors we will add to
     * @return True if it is considered to be an array (we include null in this) otherwise we return false
     */
    private boolean checkIsJsonArray(Object potentialArray, String property, List errors) {
        boolean result = true;
        if (potentialArray != null) {
            if (!(potentialArray instanceof JSONArray)) {
                errors.add("Property " + property + " is not a json array");
                result = false;
            }
        }
        return(result);
    }
}
