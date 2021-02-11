package org.olf.rs;

import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.rs.SharedIndexActions;
import grails.core.GrailsApplication;

/**
 * Return the right SharedIndexActions for the tenant config
 *
 */
public class SharedIndexService {

  GrailsApplication grailsApplication

  public SharedIndexActions getSharedIndexActionsFor(String si) {
    log.debug("SharedIndexService::getSharedIndexActionsFor(${si})");
    SharedIndexActions result = grailsApplication.mainContext."${si}SharedIndexService"

    if ( result == null && si != 'none' ) {
      log.warn("Unable to locate SharedIndexActions for ${si}. Did you fail to configure the app_setting \"host_si_integration\". Current options are folio|none");
    }

    return result;
  }

  public SharedIndexActions getSharedIndexActions() {
    SharedIndexActions result = null;
    AppSetting host_si_intergation_setting = AppSetting.findByKey('host_si_integration');
    String v = host_si_intergation_setting?.value ?: 'folio';
    log.debug("Return host si integrations for : ${v} - query application context for bean named ${v}SharedIndexService");
    result = getSharedIndexActionsFor(v);
    return result;
  }

}
