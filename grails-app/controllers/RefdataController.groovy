import com.k_int.okapi.OkapiTenantAwareController
import com.k_int.web.toolkit.refdata.RefdataCategory

import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j

@Slf4j
@CurrentTenant
class RefdataController extends OkapiTenantAwareController<RefdataCategory> {
  
  RefdataController() {
    super(RefdataCategory)
  }
}