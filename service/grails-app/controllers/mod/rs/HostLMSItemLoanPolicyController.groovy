package mod.rs

import grails.gorm.multitenancy.CurrentTenant
import org.olf.rs.HostLMSItemLoanPolicy
import org.olf.rs.HostLMSPatronProfile

@CurrentTenant
class HostLMSItemLoanPolicyController extends HasHiddenRecordController<HostLMSItemLoanPolicy> {

    static responseFormats = ['json', 'xml']

    HostLMSItemLoanPolicyController() {
        super(HostLMSItemLoanPolicy)
    }
}
