package mod.rs


import grails.rest.*
import grails.converters.*

import AppSetting from com.k_int.web.toolkit.settings

import com.k_int.okapi.OkapiTenantAwareController
import grails.gorm.multitenancy.CurrentTenant
import groovy.util.logging.Slf4j
import org.olf.rs.workflow.*;

class SettingController extends OkapiTenantAwareController<Setting> {
  
  static responseFormats = ['json', 'xml']
  
  SettingController() {
    super(AppSetting)
  }
  
}
