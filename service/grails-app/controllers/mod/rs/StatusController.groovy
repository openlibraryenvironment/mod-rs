package mod.rs

import org.olf.rs.statemodel.Status;

import io.swagger.annotations.Api;

@Api(value = "/rs/status", tags = ["Status Controller"], description = "API for all things to do with status")
class StatusController extends OkapiTenantAwareSwaggerGetController<Status> {

  StatusController() {
    super(Status, true)
  }
}
