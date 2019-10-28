package mod.rs

import org.olf.rs.PatronRequest

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;
import grails.converters.JSON
import org.olf.rs.statemodel.StateTransition

@Slf4j
@CurrentTenant
class PatronRequestController extends OkapiTenantAwareController<PatronRequest>  {

  PatronRequestController() {
    super(PatronRequest)
  }

  /**
   *  Controller action that takes a POST containing a json payload with the following parameters
   *   {
   *     target:{
   *       singleRequest:'1233-4534-3345-2232',
   *       requestList:[uuid-123,uuid-456,uuid-788], // NOT IMPLEMENTED YET
   *       query:"title=%"  // NOT IMPLEMENTED YET
   *     }
   *     action:"StartRota",
   *     actionParams:{}
   *   }
   */
  def performAction() {

    def result = [:]

    if ( request.method=='POST' ) {
      log.debug("PatronRequestController::performAction(${request.JSON})");
      if ( request.JSON.target.singleRequest ) {
        log.debug("Apply action ${request.JSON.action} to ${request.JSON.target.singleRequest}");
      }
      else if ( request.JSON.target.requestList ) {
        request.JSON.target.requestList.each { prid ->
          log.debug("Apply action ${request.JSON.action} to ${prid}");
        }
      }
    }
    render result as JSON;
  }

  /**
   * list the valid actions for this request
   */
  def validActions() {
    log.debug("PatronRequestController::validActions() ${params}");
    def result = [:];

    if ( params.patronRequestId ) {
      def patron_request = PatronRequest.get(params.patronRequestId)

      if (  patron_request != null ) {
        result.actions=patron_request.getValidActions();
      }
      else {
        result.actions=[];
        result.message="Unable to locate request for ID ${params.patronRequestId}";
      }
    }
    else {
      result.actions=[];
      result.message="No ID provided in call to validActions";
    }

    render result as JSON
  }
}

