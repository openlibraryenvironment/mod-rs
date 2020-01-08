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


/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class HostLMSService {

  def defaultHostLMSService

  public HostLMSActions getHostLMSActions() {

    HostLMSActions result = null;

    AppSetting host_lms_intergation_setting = AppSetting.findByKey('host_lms_integration');
    String v = host_lms_intergation_setting?.value

    log.debug("Return host lms integrations for : ${v}");

    switch ( v ) {
      default:
        result = defaultHostLMSService
        break;
    }
    
    return result;
  }

}
