package org.olf.rs.patronstore

import com.k_int.web.toolkit.settings.AppSetting;
import static groovyx.net.http.Method.GET;
import static groovyx.net.http.HttpBuilder.configure;
import groovyx.net.http.FromServer;
import org.olf.rs.patronstore.PatronStoreActions;

public class FolioPatronStoreService implements PatronStoreActions {
  
     
  private Map getFolioSettings() {
    AppSetting patron_store_base_url_setting = AppSetting.findByKey('patron_store_base_url');
    String patron_store_base_url = patron_store_base_url_setting?.value ?: patron_store_base_url_setting?.defValue;
    AppSetting patron_store_tenant_setting = AppSetting.findByKey('patron_store_tenant');
    String patron_store_tenant = patron_store_tenant_setting?.value ?: patron_store_tenant_setting?.defValue;
    AppSetting patron_store_user_setting = AppSetting.findByKey('patron_store_user');
    String patron_store_user = patron_store_user_setting?.value ?: patron_store_user_setting?.defValue;
    AppSetting patron_store_pass_setting = AppSetting.findByKey('patron_store_pass');
    String patron_store_pass = patron_store_pass_setting?.value ?: patron_store_pass_setting?.defValue;
    AppSetting patron_store_group_setting = AppSetting.findByKey('patron_store_group');
    String patron_store_group = patron_store_group_setting?.value ?: patron_store_group_setting?.defValue;
    

    return [ url: patron_store_base_url, tenant: patron_store_tenant, 
      user: patron_store_user, pass: patron_store_pass, group: patron_store_group ];    
    
  }
  
  public boolean createPatronStore(Map patronData) {
    def folioSettings = getFolioSettings();
    def result = false;
    log.debug("Creating patron store with data ${patronData}");
    if(folioSettings.url == null || folioSettings.tenant == null ||
      folioSettings.user == null || folioSettings.pass == null) {
      log.warn("Unable to connect to Folio Patron Store: Bad url/tenant/user/password");
    } else {
      String token = getOkapiToken(folioSettings.url, folioSettings.user, folioSettings.pass,
       folioSettings.tenant);
      if(!token) {
        log.warn("Unable to acquire token for Folio Patron Store");
      } else {
        def newUser = [:];
        newUser['externalSystemId'] = patronData['userid'];
        newUser['personal'] = [:];
        newUser['personal']['firstName'] = patronData['givenName'] ?: 'None';
        newUser['personal']['lastName'] = patronData['surname'] ?: 'None';
        newUser['personal']['email'] = patronData['email'] ?: 'null@null.null';
        newUser['patronGroup'] = folioSettings['group'];
        newUser['username'] = patronData['userid']; 
        newUser['barcode'] = patronData['userid'];
        newUser['active'] = true;
        def userRequest = configure {
          request.uri = folioSettings.url;
          request.uri.path = "/users";
          request.contentType = "application/json";
          request.headers['X-Okapi-Tenant'] = folioSettings.tenant;
          request.headers['X-Okapi-Token'] = token;          
        }.post() {
          request.body = newUser;
          response.success { FromServer fs, Object body ->
            result = true;
          }
          response.failure { FromServer fs ->
            result = false;
            log.error("Unable to create new FOLIO User at url ${fs.getUri().toString()} with JSON ${newUser}: ${fs.getStatusCode()} ${fs.getMessage()}");
          }
        }
      }
    }
    return result;
  }


  public Map lookupRawFolioUser(String systemPatronId) {
    def folioSettings = getFolioSettings();
    def resultMap = [:];
    log.debug("Looking up patron store for id ${systemPatronId}");
    if(folioSettings.url == null || folioSettings.tenant == null ||
      folioSettings.user == null || folioSettings.pass == null) {
      log.warn("Unable to connect to Folio Patron Store: Bad url/tenant/user/password");
    } else {
      String token = getOkapiToken(folioSettings.url, folioSettings.user, folioSettings.pass,
       folioSettings.tenant);
      if( !token ) {
        log.warn("Unable to acquire token for Folio Patron Store");
      } else {
        def userRequest = configure {
          request.uri = folioSettings.url;
          request.uri.path = "/users";
          request.uri.query = [ query : "externalSystemId==${systemPatronId}" ];
          request.contentType = "application/json";
          request.headers['X-Okapi-Tenant'] = folioSettings.tenant;
          request.headers['X-Okapi-Token'] = token;
        }.get() {
          response.success { FromServer fs, Object body -> 
            try {
              def users = body['users'];
              if(!users || users.size() < 1) {
                log.debug("No users found with externalSystemId of ${systemPatronId}");
              } else {
                resultMap = users[0];
              }
            } catch(Exception e) {
              log.error("Error reading returned JSON ${body}: ${e}");
            }
          }
          response.failure { FromServer fs ->
            log.error("Unable to read Patron with id ${systemPatronId} at url ${fs.getUri().toString()}: Status ${fs.getStatusCode()}: ${fs.getMessage()}");
          }
        }    
      }
    }
    return resultMap;
  }

  public Map lookupPatronStore(String systemPatronId) {
    def user = lookupRawFolioUser(systemPatronId);
    def resultMap = [:];
    if(user) {
      try {
        resultMap['userid'] = user['externalSystemId'];
        resultMap['givenName'] = user['personal']['firstName'];
        resultMap['surname'] = user['personal']['lastName'];
        resultMap['email'] = user['personal']['email'];
      } catch(Exception e) {
        log.error("Error assigning values from user JSON ${user}: ${e}");
      }
    }
    return resultMap;
  }

  public boolean updatePatronStore(String systemPatronId, Map patronData) {
    def resultMap = lookupRawFolioUser(systemPatronId);

    if(resultMap.size() == 0) {
      log.debug("Cannot update patron store, none found for id ${systemPatronId}");
      return false;
    }

    def folioSettings = getFolioSettings();
    String token = getOkapiToken(folioSettings.url, folioSettings.user, folioSettings.pass,
       folioSettings.tenant);

    if(!token) {
      log.warn("Unable to acquire token for Folio Patron Store");
      return false;
    }

    boolean result = false;
    

    log.debug("Updating patron store with identifier ${systemPatronId}");
    

    String folioId = resultMap['id'];

    if(resultMap['personal'] == null) {
      resultMap['personal'] = [:];
    }
    resultMap['personal']['firstName'] = patronData['givenName'] ?:
      (resultMap['personal']['firstName'] ?: 'None');

    resultMap['personal']['lastName'] = patronData['surname'] ?:
      (resultMap['personal']['lastName'] ?: 'None');

    resultMap['personal']['email'] = patronData['email'] ?:
      (resultMap['personal']['email'] ?: 'null@null.null');
    
    if(resultMap['patronGroup'] == null) {
      resultMap['patronGroup'] = folioSettings['group'];
    }

    if(resultMap['username'] == null) {
      resultMap['username'] = patronData['userid'];
    }
    
    if(resultMap['barcode'] == null) {
      resultMap['barcode'] = patronData['userid'];
    }

    if(resultMap['active'] == null) {
      resultMap['active'] = true;
    }

    def updateRequest = configure {
      request.uri = folioSettings.url;
      request.uri.path = "/users/${folioId}";
      request.contentType = "application/json";
      request.headers['X-Okapi-Tenant'] = folioSettings.tenant;
      request.headers['X-Okapi-Token'] = token;
    }.put() {
      request.body = resultMap;
      response.success { FromServer fs, Object body ->
        result = true;
      }
      response.failure { FromServer fs ->
        result = false;
        log.error("Unable to update FOLIO user at url ${fs.getUri().toString()} with JSON ${resultMap}: ${fs.getStatusCode()} ${fs.getMessage()}");
      }
    }
    return result;
  }

  public Map lookupOrCreatePatronStore(String systemPatronId, Map patronData) {
    def resultMap = lookupPatronStore(systemPatronId);
    if(resultMap.size() != 0) {
      log.debug("Existing patron for id ${systemPatronId}: ${resultMap}");
      return resultMap;
    } else {
      log.debug("Creating new patron store for id ${systemPatronId} with data ${patronData}");
      def createResult = createPatronStore(patronData);
      if(!createResult) {
        log.error("Unable to create new Folio Patron Record")
        return [:];
      }
      return patronData;
    }
  }

  public boolean updateOrCreatePatronStore(String systemPatronId, Map patronData) {
    boolean success = updatePatronStore(systemPatronId, patronData);
    if(!success) {
      log.debug("Unable to update patron store with identifier ${systemPatronId}");
      success = createPatronStore(patronData);
    }
    return success;
  }
  
  
  /*
   * This is duplicated verbatim from FolioSharedIndexService. Ideally it would better
   * to have a shared method that both services use.
   */  
  private String getOkapiToken(String baseUrl, String user, String pass, String tenant) {
    String result = null;
    def postBody = [username: user, password: pass]
    log.debug("getOkapiToken(${baseUrl},${postBody},..,${tenant})");
    try {
      def r1 = configure {
        request.headers['X-Okapi-Tenant'] = tenant
        request.headers['accept'] = 'application/json'
        request.contentType = 'application/json'
        request.uri = baseUrl;
        request.uri.path = '/authn/login'
        request.uri.query = [expandPermissions:true,fullPermissions:true]
        request.body = postBody
      }.post() {
        log.debug("Posting to uri ${request.uri?.toString()}");
        response.success { resp ->
          if ( resp == null ) {
            log.error("Response null from http post");
          }
          else {
            log.debug("Try to extract token - ${resp} ${resp?.headers}");
            def tok_header = resp.headers?.find { h-> h.key == 'x-okapi-token' }
            if ( tok_header ) {
              result = tok_header.value;
            }
            else {
              log.warn("Unable to locate okapi token header amongst ${r1?.headers}");
            }
          }
        
        }
        response.failure { resp -> 
          log.error("RESP ERROR: ${resp.getStatusCode()}, ${resp.getMessage()}, ${resp.getHeaders()}")
        }
      }
    }
    catch ( Exception e ) {
        log.error("problem trying to obtain auth token for shared index",e);
    }

    log.debug("Result of okapi login: ${result}");
    return result;
  }  
	
}