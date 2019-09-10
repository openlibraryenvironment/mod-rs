package org.olf

import com.k_int.okapi.OkapiTenantAwareController
import com.k_int.web.toolkit.refdata.GrailsDomainRefdataHelpers
import com.k_int.web.toolkit.refdata.RefdataCategory
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.utils.DomainUtils
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j

@Slf4j
@CurrentTenant
class RefdataController extends OkapiTenantAwareController<RefdataCategory> {
  
  RefdataController() {
    super(RefdataCategory)
  }
  
  def lookup (String domain, String property) {
    def c = DomainUtils.resolveDomainClass(domain)?.javaClass
    def cat = c ? GrailsDomainRefdataHelpers.getCategoryString(c, property) : null
    
    // Bail if no cat.
    if (!cat) {
      render status: 404
    } else {
      
      // SO: THis needs addressing in the superclasses. Shouldn't have to repeat this here.
      final int offset = params.int("offset") ?: 0
      final int perPage = Math.min(params.int('perPage') ?: params.int('max') ?: 10, 100)
      final int page = params.int("page") ?: (offset ? (offset / perPage) + 1 : 1)
      final List<String> filters = ["owner.desc==${cat}"]
      final List<String> match_in = params.list("match[]") ?: params.list("match")
      final List<String> sorts = params.list("sort[]") ?: params.list("sort")
      if (params.boolean('stats')) {
        respond simpleLookupService.lookupWithStats(RefdataValue, params.term, perPage, page, filters, match_in, sorts, null)
      } else {
        respond simpleLookupService.lookup(RefdataValue, params.term, perPage, page, filters, match_in, sorts)
      }
    }
  }
}