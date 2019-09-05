package mod.rs

import org.olf.rs.PatronRequest

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;

@Slf4j
@CurrentTenant
class PatronRequestController extends OkapiTenantAwareController<PatronRequest>  {

  public static final String POSSIBLE_ACTIONS_QUERY='select distinct st.action from StateTransition as st where st.fromStatus = :fromstate'

  PatronRequestController() {
    super(PatronRequest)
  }

  /**
   *  Controller action that takes a POST containing a json payload with the following parameters
   *   {
   *     patronRequestIdList:[uuid-123,uuid-456,uuid-788],
   *     action:"StartRota",
   *     actionParams:{}
   *   }
   */
  def performAction() {
    if ( request.method=='POST' ) {
      log.debug("PatronRequestController::performAction(${request.JSON})");
    }
  }

  /**
   * list the valid actions for this request
   */
  def validActions() {
    log.debug("PatronRequestController::validActions() ${params}");
    def result = [:]

    if ( params.patronRequestId ) {
      def patron_request = PatronRequest.get(params.patronRequestId)

      if (  patron_request != null ) {
        result.currentState = patron_request.state

        def possible_actions = StateTransition.executeQuery(POSSIBLE_ACTIONS_QUERY,[fromstate:patron_request.state]);

        // Gather only the action id, name and description from the set of possible actions, and return that as a list of maps
        result.validActions=possible_actions.collect{ [ id: it.id, name:it.name, description:it.description ] }
      }
      else {
        result.message="Unable to locate request for ID ${params.patronRequestId}";
      }
    }
    else {
      result.message="No ID provided in call to validActions";
    }

    respond result
  }
}

