package mod.rs

import org.olf.rs.NoticePolicy;

import io.swagger.annotations.Api;

@Api(value = "/rs/noticePolicies", tags = ["Notice Policies Controller"], description = "API for all things to do with notice policies")
class NoticePolicyController extends OkapiTenantAwareSwaggerController<NoticePolicy> {

  static responseFormats = ['json', 'xml']

  NoticePolicyController() {
    super(NoticePolicy)
  }

}
