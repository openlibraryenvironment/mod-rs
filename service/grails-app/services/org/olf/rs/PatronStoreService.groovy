package org.olf.rs;

import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.rs.patronstore.PatronStoreActions;
import grails.core.GrailsApplication;

/**
 * Return the right PatronServiceActions for the tenant config
 *
 */
public class PatronStoreService {

  GrailsApplication grailsApplication
  String app_setting = 'patron_store';

  public PatronStoreActions getPatronStoreActionsFor(String ps) {
    log.debug("PatronStoreService::getSharedIndexActionsFor(${ps})");
   
    PatronStoreActions result = null;
    String bean_name;

    if('FOLIO' == ps?.toUpperCase()) {
      bean_name = 'folioPatronStoreService';
    } else {
      bean_name = 'manualPatronStoreService';
    }

    try {
      result = grailsApplication.mainContext.getBean(bean_name);
    } catch(Exception e) {
      log.error("Unable to retrieve bean ${bean_name} from grails application context: ${e}");
    }

    if ( result == null && ps != 'none' ) {
      log.warn("Unable to locate PatronStoreActions for ${ps}. Did you fail to configure the app_setting \"${app_setting}\". Current options are folio|none");
    }

    return result;
  }

  public PatronStoreActions getPatronStoreActions() {
    PatronStoreActions result = null;
    AppSetting patron_store_setting = AppSetting.findByKey(app_setting);
    String v = patron_store_setting?.value;
    log.debug("Return host patron store integrations for : ${v} - query application context for bean named ${v}PatronStoreService");
    result = getPatronStoreActionsFor(v);
    return result;
  }

}
