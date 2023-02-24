package org.olf.rs.hostlms;

import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.circ.client.CirculationClient

import com.k_int.web.toolkit.settings.AppSetting

public class NcsuHostLMSService extends SymphonyHostLMSService {

  @Override
  public CirculationClient getCirculationClient(String address) {
    AppSetting password = AppSetting.findByKey('fromAgencyAuthentication');
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address,
     [
      fromAgencyAuthentication: password?.value,
      protocol: "NCIP2"
     ]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
  }

}