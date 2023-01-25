package mod.rs

import org.olf.rs.HostLMSPatronProfile;

import grails.gorm.multitenancy.CurrentTenant;
import io.swagger.annotations.Api;

@CurrentTenant
@Api(value = "/rs/hostLMSPatronProfiles", tags = ["Host LMS Patron Profiles Controller"], description = "API for all things to do with Host LMS Patron Profiles")
class HostLMSPatronProfileController extends HasHiddenRecordController<HostLMSPatronProfile> {

    static responseFormats = ['json', 'xml']

    HostLMSPatronProfileController() {
        super(HostLMSPatronProfile)
    }
}
