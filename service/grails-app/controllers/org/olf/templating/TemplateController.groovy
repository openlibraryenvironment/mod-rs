package org.olf.templating

import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import com.k_int.okapi.OkapiTenantAwareController
import grails.converters.JSON
import org.olf.templating.TemplateContainer

import org.olf.rs.BackgroundTaskService

@Slf4j
@CurrentTenant
class TemplateController extends OkapiTenantAwareController<TemplateContainer>  {

  TemplateController() {
    super(TemplateContainer)
  }

    // Before deletion, check this template container isn't in use as a value from any template setting
    def delete() {
      if (!TemplatingService.usedInAppSettings(params.id)) {
        super.delete()
      } else {
        response.sendError(400, "That template is in use on one or more AppSettings")
      }
    }

}