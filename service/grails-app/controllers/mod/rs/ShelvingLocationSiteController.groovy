package mod.rs;

import org.olf.rs.ShelvingLocationSite;

import io.swagger.annotations.Api;

@Api(value = "/rs/shelvingLocationSites", tags = ["Shelving Location Sites Controller"], description = "API for all things to do with shelving location sites")
class ShelvingLocationSiteController extends OkapiTenantAwareSwaggerController<ShelvingLocationSite> {

  ShelvingLocationSiteController() {
    super(ShelvingLocationSite);
  }

}
