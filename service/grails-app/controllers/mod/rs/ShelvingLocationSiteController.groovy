package mod.rs;

import com.k_int.okapi.OkapiTenantAwareController;
import org.olf.rs.ShelvingLocationSite;

class ShelvingLocationSiteController extends OkapiTenantAwareController<ShelvingLocationSite> {

  ShelvingLocationSiteController() {
    super(ShelvingLocationSite);
  }

}
