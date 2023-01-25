package mod.rs

import org.olf.rs.HostLMSItemLoanPolicy;

import grails.gorm.multitenancy.CurrentTenant;
import io.swagger.annotations.Api;

@CurrentTenant
@Api(value = "/rs/hostLMSItemLoanPolicies", tags = ["Host LMS Item Loan Policies Controller"], description = "API for all things to do with Host LMS Item Loan Policies")
class HostLMSItemLoanPolicyController extends HasHiddenRecordController<HostLMSItemLoanPolicy> {

    static responseFormats = ['json', 'xml']

    HostLMSItemLoanPolicyController() {
        super(HostLMSItemLoanPolicy)
    }
}
