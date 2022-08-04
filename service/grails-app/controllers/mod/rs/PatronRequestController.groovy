package mod.rs;

import org.olf.rs.OpenUrlService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.Result;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.ActionService;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;

@Slf4j
@CurrentTenant
class PatronRequestController extends OkapiTenantAwareController<PatronRequest>  {

	ActionService actionService;
    OpenUrlService openUrlService;
	ReshareApplicationEventHandlerService reshareApplicationEventHandlerService

	PatronRequestController() {
		super(PatronRequest)
	}

    /**
     *  Controller action that takes a POST containing a json payload with the following parameters
     *   {
     *     action:"StartRota",
     *     actionParams:{}
     *   }
     */
	def performAction() {

		def result = [:]
		if ( request.method=='POST' ) {
			log.debug("PatronRequestController::performAction(${request.JSON})...");
			if ( params.patronRequestId ) {
				PatronRequest.withTransaction { tstatus ->
					PatronRequest patron_request = PatronRequest.lock(params.patronRequestId)

					if ( patron_request ) {
						log.debug("Apply action ${request.JSON.action} to ${patron_request}");

                        // Needs to fulfil the following criteria to be valid
                        // 1. Is a valid action for the current status of the request
                        // 2. Request has no network activity going on
						if (patron_request.isNetworkActivityIdle() &&
                            actionService.isValid(patron_request.isRequester, patron_request.state, request.JSON.action)) {
							// Perform the requested action
							ActionResultDetails resultDetails = actionService.performAction(request.JSON.action, patron_request, request.JSON.actionParams)
							response.status = (resultDetails.result == ActionResult.SUCCESS ? 200 : (resultDetails.result == ActionResult.INVALID_PARAMETERS ? 400 : 500));
							result = resultDetails.responseResult;
						} else {
							response.status = 400;
							result.message = 'A valid action was not supplied, isRequester: ' + patron_request.isRequester +
				            	   			 ' Current state: ' + patron_request.state.code +
                                             ', network status: ' + patron_request.networkStatus.toString() +
											 ' Action being performed: ' + request.JSON.action;
						    reshareApplicationEventHandlerService.auditEntry(patron_request, patron_request.state, patron_request.state, result.message, null);
							patron_request.save(flush:true, failOnError:true);
						}

					} else {
						response.status = 400;
						result.message='Unable to lock request with id: ' + params.patronRequestId;
					}
				}
			}
		}
		log.debug("PatronRequestController::performAction exiting");
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
  	def bulkAction() {
		  def result = [:]
		  render result as JSON;
	  }

    /**
     * list the valid actions for this request
     */
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

    def openURL() {
        // Maps an OpenURL onto a request, originally taken from here https://github.com/openlibraryenvironment/listener-openurl/blob/master/src/ReshareRequest.js
        Result result = openUrlService.mapToRequest(params) ;
        response.status = 200;

        render result as JSON
    }
}
