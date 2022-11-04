package mod.rs;

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
@Api(value = "/rs", tags = ["State Model Controller"], description = "StateModel Api")
class StateModelController extends OkapiTenantAwareController<StateModel>  {

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
        nickname = "stateModel/export",
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
                result.stateModels.add(stateModelService.exportStateModel(stateModel));
            }
		} else {
            // We need to return all the state models
            StateModel.findAll().each { stateModel ->
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
    }
}
