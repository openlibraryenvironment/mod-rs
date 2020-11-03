package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import com.k_int.okapi.OkapiClient
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

/**
 * 
 */
@Slf4j
public class FolioEmailServiceImpl implements EmailService {

  def grailsApplication
  def reshareActionService
  static boolean running = false;

  // injected by spring
  OkapiClient okapiClient

  public Map sendEmail(Map email_params) {
    log.debug("FolioEmailServiceImpl::sendNotification(${email_params})");

    Map email_cfg = getEmailConfig();

    if ( email_cfg != null ) {
      try {
        log.debug("Send email");
        if (okapiClient?.withTenant().providesInterface("email", "^1.0")) {
          log.debug(" -> Got email");

          // post(URL, JsonPayload, Params)
          def email_result = okapiClient.post("/email", email_params, [:]);
        }
      }
      catch ( Exception e ) {
        log.error("Problem talking to mod-config",e);
        log.debug("okapiClient: ${okapiClient} ${okapiClient?.inspect()}");
      }
    }
  
    return [ status: 'OK' ]
  }

  // Use mod-configuration to retrieve the approproate setting
  private String getSetting(String setting) {
    String result = null;
    try {
      def setting_result = okapiClient.getSync("/configurations/entries", [query:'code='+setting])
      log.debug("Got setting result ${setting_result}");
    }   
    catch ( Exception e ) {
      e.printStackTrace()
    }

    return result;
  }

  private Map getEmailConfig() {
    if ( email_config == null ) {
      try {
        if (okapiClient?.withTenant().providesInterface("configuration", "^2.0")) {
          email_config = [
            EMAIL_SMTP_HOST:getSetting('EMAIL_SMTP_HOST'),
            EMAIL_SMTP_PORT:getSetting('EMAIL_SMTP_PORT'),
            EMAIL_SMTP_LOGIN_OPTION:getSetting('EMAIL_SMTP_LOGIN_OPTION'),
            EMAIL_TRUST_ALL:getSetting('EMAIL_TRUST_ALL'),
            EMAIL_SMTP_SSL:getSetting('EMAIL_SMTP_SSL'),
            EMAIL_START_TLS_OPTIONS:getSetting('EMAIL_START_TLS_OPTIONS'),
            EMAIL_USERNAME:getSetting('EMAIL_USERNAME'),
            EMAIL_PASSWORD:getSetting('EMAIL_PASSWORD'),
            EMAIL_FROM:getSetting('EMAIL_FROM')
          ]
          
          log.debug("getEmailConfig : ${email_config}");
        }
      }
      catch ( Exception e ) {
        log.error("Problem talking to mod-config",e);
        log.debug("okapiClient: ${okapiClient} ${okapiClient?.inspect()}");
      }
    }
    return email_config;
  }
}
