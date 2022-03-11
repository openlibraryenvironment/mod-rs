package mod.rs

import grails.rest.*
import grails.converters.*
import org.olf.rs.HostLMSPatronProfile
import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import grails.gorm.transactions.Transactional
import static org.springframework.http.HttpStatus.*

@CurrentTenant
class HostLMSPatronProfileController extends OkapiTenantAwareController<HostLMSPatronProfile> {
  
  static responseFormats = ['json', 'xml']
  
  HostLMSPatronProfileController() {
    super(HostLMSPatronProfile)
  }

}
