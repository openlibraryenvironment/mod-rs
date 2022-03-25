package mod.rs

import org.olf.rs.HostLMSPatronProfile;

import grails.gorm.multitenancy.CurrentTenant;

@CurrentTenant
class HostLMSPatronProfileController extends HasHiddenRecordController<HostLMSPatronProfile> {

    static responseFormats = ['json', 'xml']

    HostLMSPatronProfileController() {
        super(HostLMSPatronProfile)
    }
}
