package mod.rs

import com.k_int.okapi.OkapiTenantAwareController
import com.k_int.web.toolkit.settings.AppSetting

import grails.gorm.transactions.Transactional

class SettingController extends OkapiTenantAwareController<AppSetting> {

  static responseFormats = ['json', 'xml']

  SettingController() {
    super(AppSetting)
  }

  @Override
  @Transactional
  def index(Integer max) {
    // Use gorm criteria builder to always add your custom filter....
    respond doTheLookup() {
      or {
        isNull('hidden')
        eq('hidden', false)
      }
    }
  }
}