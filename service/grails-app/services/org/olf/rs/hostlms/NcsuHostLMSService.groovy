package org.olf.rs.hostlms;

import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.ISettings;

public class NcsuHostLMSService extends SymphonyHostLMSService {

  @Override
  public CirculationClient getCirculationClient(ISettings settings, String address) {
    String password = settings.getSettingValue(SettingsData.SETTING_NCIP_FROM_AGENCY_AUTHENTICATION);
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address,
     [
      fromAgencyAuthentication: password,
      protocol: "NCIP2"
     ]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
  }
}
