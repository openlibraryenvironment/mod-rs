package mod.rs

import grails.rest.*
import grails.converters.*

import org.olf.rs.NoticePolicy
import org.olf.rs.NoticePolicyNotice

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;

class NoticePolicyController extends OkapiTenantAwareController<NoticePolicy> {

  static responseFormats = ['json', 'xml']

  NoticePolicyController() {
    super(NoticePolicy)
  }

}
