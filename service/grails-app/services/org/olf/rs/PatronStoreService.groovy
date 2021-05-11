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

  public PatronStoreActions getPatronStoreActionsFor(String ps) {
    log.debug("PatronStoreService::getSharedIndexActionsFor(${ps})");
   
    PatronStoreActions result = null;

    if('FOLIO' == ps) {
      result = grailsApplication.mainContext.FolioPatronStoreService;
    } else {
      result = grailsApplication.mainContext.ManualPatronStoreService;
    }

    if ( result == null && ps != 'none' ) {
      log.warn("Unable to locate PatronStoreActions for ${ps}. Did you fail to configure the app_setting \"shared_index_integration\". Current options are folio|none");
    }

    return result;
  }

  public PatronStoreActions getPatronStoreActions() {
    PatronStoreActions result = null;
    AppSetting patron_store_setting = AppSetting.findByKey('patron_store');
    String v = patron_store_setting?.value;
    log.debug("Return host si integrations for : ${v} - query application context for bean named ${v}PatronStoreService");
    result = getPatronStoreActionsFor(v);
    return result;
  }

}