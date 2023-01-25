package mod.rs

import org.olf.rs.HostLMSLocation;

import grails.gorm.multitenancy.CurrentTenant;
import io.swagger.annotations.Api;

@CurrentTenant
@Api(value = "/rs/hostLMSLocations", tags = ["Host LMS Location Controller"], description = "API for all things to do with Host LMS Locations")
class HostLMSLocationController extends HasHiddenRecordController<HostLMSLocation> {

    static responseFormats = ['json', 'xml']

    HostLMSLocationController() {
        super(HostLMSLocation)
    }
}
