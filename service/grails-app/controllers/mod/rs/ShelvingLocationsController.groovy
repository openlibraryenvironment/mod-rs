package mod.rs

import org.olf.rs.HostLMSShelvingLocation;

import io.swagger.annotations.Api;

@Api(value = "/rs/shelvingLocations", tags = ["Shelving Locations Controller"], description = "API for all things to do with Shelving Locations")
class ShelvingLocationsController extends HasHiddenRecordController<HostLMSShelvingLocation> {

    ShelvingLocationsController() {
        super(HostLMSShelvingLocation)
    }
}
