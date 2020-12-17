package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import com.k_int.okapi.OkapiClient
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import grails.core.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired
import com.k_int.okapi.OkapiTenantResolver

@Slf4j
public class OkapiSettingsService {

  @Autowired
  GrailsApplication grailsApplication

  // injected by spring
  @Autowired
  OkapiClient okapiClient

  Map<String, Map> setting_cache = [:]

  public Map getSetting(String setting_code) {
    String tenantId = OkapiTenantResolver.schemaNameToTenantId( Tenants.currentId().toString() )
    String unique_setting = tenantId+':'+setting_code;
    Map result = setting_cache.get(unique_setting)
    if ( result == null ) {
      result = getSettingInternal(setting_code)
      if ( result ) {
        setting_cache[unique_setting] = result;
      }
    }
    return result;
  }

  // Use mod-configuration to retrieve the approproate setting
  private Map getSettingInternal(String setting) {

    String lookup_prop = 'code';
    if ( setting=='localSettings' ) {
      lookup_prop='configName'
    }

    Map result = null;
    try {
      def call_result = okapiClient.getSync("/configurations/entries", [query:lookup_prop+'='+setting])
      if ( ( call_result != null ) &&
           ( call_result instanceof Map ) &&
           ( call_result.configs != null ) &&
           ( call_result.configs.size() == 1 ) ) {
        def cfg_result_record = call_result.configs[0]
        result = [
          id: cfg_result_record.id,
          value: cfg_result_record.value,
          code: cfg_result_record.code,
          description: cfg_result_record.description,
          name: cfg_result_record.name
        ]
      }
      else {
        log.info("Config lookup for ${setting} did not return expected record: ${call_result}");
      }
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

    log.debug("OkapiSettingsService::getSettingInternal(${setting}) result ${result}");

    return result;
  }

}
