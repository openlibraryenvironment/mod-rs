package org.olf

import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import com.k_int.okapi.OkapiTenantAwareController
import grails.converters.JSON
import org.olf.rs.TemplateContainer

@Slf4j
@CurrentTenant
class TemplateController extends OkapiTenantAwareController<TemplateContainer>  {

  TemplateController() {
    super(TemplateContainer)
  }

}