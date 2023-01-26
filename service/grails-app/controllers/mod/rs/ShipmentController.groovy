package mod.rs

import org.olf.rs.Shipment;

import io.swagger.annotations.Api;

@Api(value = "/rs/shipments", tags = ["Shipments Controller"], description = "API for all things to do with shipments")
class ShipmentController extends OkapiTenantAwareSwaggerController<Shipment> {

  static responseFormats = ['json', 'xml']

  ShipmentController() {
    super(Shipment)
  }
}
