package mod.rs


import grails.rest.*
import grails.converters.*

import org.olf.rs.HostLMSLocation

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;

import grails.gorm.transactions.Transactional
import static org.springframework.http.HttpStatus.*

@CurrentTenant
class HostLMSLocationController extends OkapiTenantAwareController<HostLMSLocation> {
  
  static responseFormats = ['json', 'xml']
  
  HostLMSLocationController() {
    super(HostLMSLocation)
  }

  @Transactional
  def delete() {
    HostLMSLocation loc = queryForResource(params.id)
    
    // Not found.
    if (loc == null) {
      transactionStatus.setRollbackOnly()
      notFound()
      return
    }
    

    // Return the relevant status if not allowed to delete.
    Map canDelete = loc.canDelete();

    if (!canDelete.deleteValid) {
      transactionStatus.setRollbackOnly()

      Map response = [
        message: "Host LMS Location is attached to Patron Request(s) ${canDelete.prs.join(', ')}".toString(),
        linkedPatronRequests: canDelete.prs
      ]

      respond(response, status: METHOD_NOT_ALLOWED)
      return
    }
    
    // Finally delete the location if we get this far and respond.
    deleteResource loc
    render status: NO_CONTENT
  }
  
}
