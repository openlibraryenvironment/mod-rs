package mod.rs


import grails.rest.*
import grails.converters.*

import org.olf.rs.HostLMSLocation

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;

class HostLMSLocationController extends OkapiTenantAwareController<HostLMSLocation> {
  
  static responseFormats = ['json', 'xml']
  
  HostLMSLocationController() {
    super(HostLMSLocation)
  }
  
}
