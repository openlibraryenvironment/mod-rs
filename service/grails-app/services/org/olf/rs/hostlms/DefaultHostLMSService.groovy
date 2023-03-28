package org.olf.rs.hostlms;

import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper;
import org.olf.rs.settings.ISettings;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class DefaultHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [protocol: "NCIP2"]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
  }

}
