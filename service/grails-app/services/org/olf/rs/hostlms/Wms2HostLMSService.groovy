package org.olf.rs.hostlms;

import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.circ.client.CirculationClient
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.ISettings;


public class Wms2HostLMSService extends WmsHostLMSService {

  @Override
  public CirculationClient getCirculationClient(ISettings settings, String address) {
    String wms_api_key = settings.getSettingValue(SettingsData.SETTING_WMS_API_KEY);
    String wms_api_secret = settings.getSettingValue(SettingsData.SETTING_WMS_API_SECRET);
    String wms_lookup_patron_endpoint = settings.getSettingValue(SettingsData.SETTING_WMS_LOOKUP_PATRON_ENDPOINT);
    
    
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [
      protocol: "WMS2",
      apiKey: wms_api_key,
      apiSecret: wms_api_secret,
      lookupPatronEndpoint: wms_lookup_patron_endpoint
      ]).circulationClient;
  }
}