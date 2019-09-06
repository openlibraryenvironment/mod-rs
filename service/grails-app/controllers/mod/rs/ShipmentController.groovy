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
  
  def performAction() {
    if ( request.method=='POST' ) {
      log.debug("ShipmentController::performAction(${request.JSON})");
    }
  }

  /**
   * list the valid actions for this shipment
   */
  def validActions() {
    log.debug("ShipmentController::validActions() ${params}");
    def result = [:]

    if ( params.ShipmentId ) {
      def shipment = Shipment.get(params.ShipmentId)

      if (  shipment != null ) {
        result.currentState = shipment.state

        def possible_actions = StateTransition.executeQuery(POSSIBLE_ACTIONS_QUERY,[fromstate:shipment.state]);

        // Gather only the action id, name and description from the set of possible actions, and return that as a list of maps
        result.validActions=possible_actions.collect{ [ id: it.id, name:it.name, description:it.description ] }
      }
      else {
        result.message="Unable to locate request for ID ${params.shipmentId}";
      }
    }
    else {
      result.message="No ID provided in call to validActions";
    }
    respond result
  }
}
