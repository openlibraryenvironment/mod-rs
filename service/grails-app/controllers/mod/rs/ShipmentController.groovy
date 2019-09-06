package mod.rs


import grails.rest.*
import grails.converters.*

import org.olf.rs.Shipment
import org.olf.rs.ShipmentItem

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;

class ShipmentController extends OkapiTenantAwareController<Shipment> {
  
	static responseFormats = ['json', 'xml']
  
  ShipmentController() {
    super(Shipment)
  }
  
}
