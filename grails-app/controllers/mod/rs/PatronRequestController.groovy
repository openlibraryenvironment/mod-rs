package mod.rs

import org.olf.rs.PatronRequest

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j

@Slf4j
@CurrentTenant
class PatronRequestController extends OkapiTenantAwareController<PatronRequest>  {

  /** Inject reShareMessage service - 
   * see https://github.com/openlibraryenvironment/mod-rs/blob/master/grails-app/services/org/olf/rs/workflow/ReShareMessageService.groovy
   */
  def reShareMessageService

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

      // use reShareMessageService to action each identified request
    }
  }
}

