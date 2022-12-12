package mod.rs

import com.k_int.okapi.OkapiTenantAwareController
import com.k_int.web.toolkit.settings.AppSetting

import grails.gorm.transactions.Transactional

class SettingController extends OkapiTenantAwareController<AppSetting> {

    static responseFormats = ['json', 'xml'];

    SettingController() {
        super(AppSetting);
    }

    @Override
    @Transactional
    def index(Integer max) {
        // Use gorm criteria builder to always add your custom filter....
        Closure gormFilterClosure = {
            or {
                isNull('hidden')
                eq('hidden', false)
            }
        };

        // Are they explicitly filtering on the hidden field
        if (params.filters != null) {
            // they are, so see if hidden is being filtered on
            if (params.filters.toString().indexOf("hidden") > -1) {
                // They are explicitly filtering on it, so we do want to return hidden settings
                gormFilterClosure = null;
            }
        }

        // Now we can perform the lookup
        respond doTheLookup(gormFilterClosure);
    }
}