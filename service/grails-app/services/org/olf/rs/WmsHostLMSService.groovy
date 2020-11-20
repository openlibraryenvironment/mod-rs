package org.olf.rs;

import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.circ.client.CirculationClient

import com.k_int.web.toolkit.settings.AppSetting


/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class WmsHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(String address) {
    AppSetting wms_api_key = AppSetting.findByKey('wms_api_key')
    AppSetting wms_api_secret = AppSetting.findByKey('wms_api_secret')
    AppSetting wms_lookup_patron_endpoint = AppSetting.findByKey('wms_lookup_patron_endpoint')
    
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [
      protocol: "WMS",
      apiKey: wms_api_key.value,
      apiSecret: wms_api_secret.value,
      lookupPatronEndpoint: wms_lookup_patron_endpoint.value
      ]).circulationClient;
  }

}
