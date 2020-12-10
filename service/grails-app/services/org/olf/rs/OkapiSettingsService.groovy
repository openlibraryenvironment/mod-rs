package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import com.k_int.okapi.OkapiClient
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import grails.core.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired

@Slf4j
public class OkapiSettingsService {

  @Autowired
  GrailsApplication grailsApplication

  // injected by spring
  @Autowired
  OkapiClient okapiClient

  Map setting_cache = [:]

  public String getSetting(String setting_code) {
    String result = setting_cache.get(setting_code)
    if ( result == null ) {
      result = getSettingInternal(setting_code)
      if ( result ) {
        setting_cache[setting_code] = result;
      }
    }
    return result;
  }

  // Use mod-configuration to retrieve the approproate setting
  private Map getSettingInternal(String setting) {

    Map result = null;
    try {
      result = okapiClient.getSync("/configurations/entries", [query:'code='+setting])
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

    log.debug("OkapiSettingsService::getSettingInternal(${setting}) result ${result}");

    return result;
  }

}
