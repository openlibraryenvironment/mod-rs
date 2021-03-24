package org.olf.rs;

import org.olf.rs.PatronRequest
import groovyx.net.http.HttpBuilder
import org.olf.rs.ItemLocation;
import org.olf.rs.statemodel.Status;
import com.k_int.web.toolkit.settings.AppSetting
import groovy.xml.StreamingMarkupBuilder
import static groovyx.net.http.HttpBuilder.configure
import groovyx.net.http.FromServer;
import com.k_int.web.toolkit.refdata.RefdataValue
import static groovyx.net.http.ContentTypes.XML
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.lms.HostLMSActions;
import grails.core.GrailsApplication

/**
 * Return the right HostLMSActions for the tenant config
 *
 */
public class HostLMSService {

  GrailsApplication grailsApplication

  public HostLMSActions getHostLMSActionsFor(String lms) {
    log.debug("HostLMSService::getHostLMSActionsFor(${lms})");
    HostLMSActions result = grailsApplication.mainContext."${lms}HostLMSService"

    if ( result == null ) {
      log.warn("Unable to locate HostLMSActions for ${lms}. Did you fail to configure the app_setting \"host_lms_integration\". Current options are aleph|alma|FOLIO|Sierra|Sirsi|wms|manual|default");
    }

    return result;
  }

  public HostLMSActions getHostLMSActions() {
    HostLMSActions result = null;
    AppSetting host_lms_intergation_setting = AppSetting.findByKey('host_lms_integration');
    String v = host_lms_intergation_setting?.value
    log.debug("Return host lms integrations for : ${v} - query application context for bean named ${v}HostLMSService");
    result = getHostLMSActionsFor(v);
    return result;
  }

}
