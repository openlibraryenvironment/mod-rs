package mod.rs


import grails.rest.*
import grails.converters.*

import org.olf.rs.Timer

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;

class TimerController extends OkapiTenantAwareController<Timer> {
  
  static responseFormats = ['json', 'xml']
  
  TimerController() {
    super(Timer)
  }
  
}
