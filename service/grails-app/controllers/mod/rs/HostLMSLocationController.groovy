package mod.rs

import org.olf.rs.HostLMSLocation;

import grails.gorm.multitenancy.CurrentTenant;

@CurrentTenant
class HostLMSLocationController extends HasHiddenRecordController<HostLMSLocation> {

    static responseFormats = ['json', 'xml']

    HostLMSLocationController() {
        super(HostLMSLocation)
    }
}
