package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import com.k_int.okapi.OkapiClient
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import grails.core.GrailsApplication

/**
 * 
 */
@Slf4j
public class FolioEmailServiceImpl implements EmailService {

  @Autowired
  GrailsApplication grailsApplication

  static boolean running = false;
  static Map email_config = null;

  // injected by spring
  @Autowired
  OkapiClient okapiClient

  public Map sendEmail(Map email_params) {
    boolean successfulSend = false;
    String error = "";

    try {
      log.debug("Send email (okapiClient=${okapiClient})")

      if ( okapiClient ) {
        // if (okapiClient.withTenant().providesInterface("email", "^1.0")) {
          log.debug(" -> Got email - calling post ${email_params}");
  
          // post(URL, JsonPayload, Params)
          def email_result = okapiClient.post("/email", email_params)
          successfulSend = true;
  
          log.debug("Email result: ${email_result}");
          return email_result as Map; //implicit returns are for suckers

      }
      else {
        error = "okapiClient not properly injected - unable to send email"
        log.warn(error);
      }
    }
    catch ( Exception e ) {
      error = "Problem talking to mod-email: ${e.getLocalizedMessage()}";
      log.error(error, e);
      log.debug("okapiClient: ${okapiClient} ${okapiClient?.inspect()}");
    }

    if (!successfulSend) {
      throw new Exception(error);
    }

  }

  public Map legacySendEmail(Map email_params) {
    log.debug("FolioEmailServiceImpl::sendNotification(${email_params})");

    Map email_cfg = getEmailConfig();

    if ( email_cfg != null ) {
      try {
        log.debug("Send email");
        if (okapiClient?.withTenant().providesInterface("email", "^1.0")) {
          log.debug(" -> Got email");

          // post(URL, JsonPayload, Params)
          def email_result = okapiClient.post("/email", email_params, [:]) {
          }
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
