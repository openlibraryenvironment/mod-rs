import com.k_int.okapi.OkapiTenantAwareController
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition

import grails.databinding.SimpleMapDataBindingSource
import grails.gorm.multitenancy.CurrentTenant
import grails.web.Controller
import groovy.util.logging.Slf4j

@Slf4j
@CurrentTenant
@Controller
class CustomPropertyDefinitionController extends OkapiTenantAwareController<CustomPropertyDefinition> {
  
  CustomPropertyDefinitionController() {
    super(CustomPropertyDefinition)
  }
  
  protected CustomPropertyDefinition createResource(Map parameters) {
    def res
    if (!parameters.type) {
      res = super.createResource(parameters)
    } else {
      res = resource.forType("${parameters.type}", parameters)
    }
    
    res
  }
  
  protected CustomPropertyDefinition createResource() {
    def instance
    def json = getObjectToBind()
    if (json && json.type) {
      instance = resource.forType("${json.type}")
    }
    
    if (!instance) {
      instance = super.createResource()
    }
    
    bindData instance, (json ? new SimpleMapDataBindingSource(json) : getObjectToBind()), ['exclude': ['type']]
    instance
  }
}
