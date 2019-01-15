package mod.rs

import org.olf.rs.PatronRequest

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j

@Slf4j
@CurrentTenant
class PatronRequestController extends OkapiTenantAwareController<PatronRequest>  {

  PatronRequestController() {
    super(PatronRequest)
  }
}

