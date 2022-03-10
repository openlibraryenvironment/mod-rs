package mod.rs

import org.olf.rs.HostLMSShelvingLocation;
import com.k_int.okapi.OkapiTenantAwareController

class ShelvingLocationsController extends OkapiTenantAwareController<HostLMSShelvingLocation> {
  
  ShelvingLocationsController() {
    super(HostLMSShelvingLocation)
  }
  
}
