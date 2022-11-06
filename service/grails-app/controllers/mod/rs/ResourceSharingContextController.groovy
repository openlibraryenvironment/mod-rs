package mod.rs

import grails.rest.*
import grails.converters.*
import org.olf.rs.ResourceSharingContext
import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j

class ResourceSharingContextController extends OkapiTenantAwareController<ResourceSharingContext> {
  
  static responseFormats = ['json', 'xml']
  
  ResourceSharingContextController() {
    super(Timer)
  }
  
}
