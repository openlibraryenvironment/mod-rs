package mod.rs

import org.olf.rs.statemodel.Status

import com.k_int.okapi.OkapiTenantAwareController

class StatusController extends OkapiTenantAwareController<Status> {
  
  StatusController() {
    super(Status, true)
  }
  
}
