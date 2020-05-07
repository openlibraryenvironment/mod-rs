package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import java.util.concurrent.ThreadLocalRandom;
import com.k_int.web.toolkit.settings.AppSetting
import static groovyx.net.http.HttpBuilder.configure
import groovy.json.JsonOutput;
import groovyx.net.http.FromServer;


/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class SharedIndexService {

 /**
   * findAppropriateCopies - Accept a map of name:value pairs that describe an instance and see if we can locate
   * any appropriate copies in the shared index.
   * @param description A Map of properies that describe the item. Currently understood properties:
   *                         title - the title of the item
   * @return instance of SharedIndexAvailability which tells us where we can find the item.
   */
  public List<AvailabilityStatement> findAppropriateCopies(Map description) {

    List<AvailabilityStatement> result = []
    log.debug("findAppropriateCopies(${description})");

    // Use the shared index to try and obtain a list of locations
    try {
      log.debug("Try graphql")
      if ( description?.systemInstanceIdentifier != null ) {
        sharedIndexHoldings(description?.systemInstanceIdentifier).each { shared_index_availability ->
          result.add(new AvailabilityStatement(
                                               symbol:shared_index_availability.symbol, 
                                               instanceIdentifier:null, 
                                               copyIdentifier:null,
                                               illPolicy:shared_index_availability.illPolicy));
        }
      }
      else {
        log.warn("No shared index identifier for record. Cannot use shared index");
      }
    }
    catch ( Exception e ) {
      log.error("Graphql failed",e);
    }

    // See if we have an app setting for lender of last resort
    AppSetting last_resort_lenders_setting = AppSetting.findByKey('last_resort_lenders');
    String last_resort_lenders = last_resort_lenders_setting?.value ?: last_resort_lenders_setting?.defValue;
    if ( last_resort_lenders && ( last_resort_lenders.length() > 0 ) ) {
      String[] additionals = last_resort_lenders.split(',');
      additionals.each { al ->
        if ( ( al != null ) && ( al.trim().length() > 0 ) ) {
          result.add(new AvailabilityStatement(symbol:al.trim(), instanceIdentifier:null, copyIdentifier:null));
        }
      }
    }

    return result;
  }

  /**
   * ToDo: This method should expose critical errors like 404 not found so that we can cleanly
   * log the problem in the activity log
   */
  public String fetchSharedIndexRecord(String id) {
    log.debug("fetchSharedIndexRecord(${id})");

    String result = null;
    AppSetting shared_index_base_url_setting = AppSetting.findByKey('shared_index_base_url');
    AppSetting shared_index_user_setting = AppSetting.findByKey('shared_index_user');
    AppSetting shared_index_pass_setting = AppSetting.findByKey('shared_index_pass');
    AppSetting shared_index_tenant_setting = AppSetting.findByKey('shared_index_tenant');

    String shared_index_base_url = shared_index_base_url_setting?.value ?: shared_index_base_url_setting?.defValue;
    String shared_index_user = shared_index_user_setting?.value ?: shared_index_user_setting?.defValue;
    String shared_index_pass = shared_index_pass_setting?.value ?: shared_index_pass_setting?.defValue;
    String shared_index_tenant =  shared_index_tenant_setting?.value ?: shared_index_tenant_setting?.defValue ?: 'diku'

    if ( ( shared_index_base_url != null ) &&
         ( shared_index_user != null ) &&
         ( shared_index_pass != null ) && 
         ( id != null ) &&
         ( id.length() > 0 ) ) {
      log.debug("Attempt to retrieve shared index record ${id} from ${shared_index_base_url} ${shared_index_user}/${shared_index_pass}");
      String token = getOkapiToken(shared_index_base_url, shared_index_user, shared_index_pass, shared_index_tenant);
      if ( token ) {
        def r1 = configure {
           request.headers['X-Okapi-Tenant'] = shared_index_tenant;
           request.headers['X-Okapi-Token'] = token
          request.uri = shared_index_base_url+'/inventory/instances/'+(id.trim());
        }.get() {
          response.success { FromServer fs, Object body ->
            log.debug("Success respoinse from shared index");
            result = JsonOutput.toJson(body);
          }
          response.failure { FromServer fs ->
            log.debug("Failure response from shared index ${fs.getStatusCode()} when attempting to GET ${id}");
          }
        }
      }
      else {
        log.warn("Unable to login to remote shared index");
      }
    }
    else {
      log.debug("Unable to contact shared index - no url/user/pass");
    }

    return result;
  }


  private String getOkapiToken(String baseUrl, String user, String pass, String tenant) {
    String result = null;
    def postBody = [username: user, password: pass]
    log.debug("getOkapiToken(${baseUrl},${postBody},..,${tenant})");
    try {
      def r1 = configure {
        request.headers['X-Okapi-Tenant'] = tenant
        request.headers['accept'] = 'application/json'
        request.contentType = 'application/json'
        request.uri = baseUrl+'/authn/login'
        request.uri.query = [expandPermissions:true,fullPermissions:true]
        request.body = postBody
      }.get() {
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


  // https://github.com/folio-org/mod-graphql/blob/master/doc/example-queries.md#using-curl-from-the-command-line

  private List<Map> sharedIndexHoldings(String id) {

    List<Map> result = []

  // "query": "query($id: String!) { instance_storage_instances_SINGLE(instanceId: $id) { id title holdingsRecord2 { holdingsInstance { id callNumber holdingsStatements } } } }",
    String query='''{
  "query": "query($id: String!) { instance_storage_instances_SINGLE(instanceId: $id) { id title holdingsRecords2 { id callNumber illPolicy { name }  permanentLocation { name code } holdingsStatements { note statement } bareHoldingsItems { id barcode enumeration } }  identifiers { value identifierTypeObject { name } } } }",
  "variables":{ "id":"'''+id+'''" } }'''

    log.debug("Sending graphql to get holdings for (${id}) \n${query}\n");

    AppSetting shared_index_base_url_setting = AppSetting.findByKey('shared_index_base_url');
    AppSetting shared_index_user_setting = AppSetting.findByKey('shared_index_user');
    AppSetting shared_index_pass_setting = AppSetting.findByKey('shared_index_pass');
    AppSetting shared_index_tenant_setting = AppSetting.findByKey('shared_index_tenant');

    String shared_index_base_url = shared_index_base_url_setting?.value ?: shared_index_base_url_setting?.defValue;
    String shared_index_user = shared_index_user_setting?.value ?: shared_index_user_setting?.defValue;
    String shared_index_pass = shared_index_pass_setting?.value ?: shared_index_pass_setting?.defValue;
    String shared_index_tenant =  shared_index_tenant_setting?.value ?: shared_index_tenant_setting?.defValue ?: 'diku'

    if ( ( shared_index_base_url != null ) &&
         ( shared_index_user != null ) &&
         ( shared_index_pass != null ) ) {
      long start_time = System.currentTimeMillis();
      try {
        String token = getOkapiToken(shared_index_base_url, shared_index_user, shared_index_pass, shared_index_tenant);
        if ( token ) {
          log.debug("Attempt to retrieve shared index record ${id}");
          def r1 = configure {
            request.headers['X-Okapi-Tenant'] = shared_index_tenant;
            request.headers['X-Okapi-Token'] = token
            request.uri = shared_index_base_url+'/graphql'
            request.contentType = 'application/json'
            request.body = query
          }.get() {
            response.success { FromServer fs, Object r1 ->
              if ( ( r1 != null ) &&
                   ( r1.data != null ) ) {
                // We got a response from the GraphQL service - example {"data":
                // {"instance_storage_instances_SINGLE":
                //     {"id":"5be100af-1b0a-43fe-bcd6-09a67fb9c779","title":"A history of the twentieth century in 100 maps",
                //      "holdingsRecords2":[
                //         {"id":"d045fd86-fdcf-455f-8f42-e7bbaaf5ddd6","callNumber":" GA793.7.A1 ","permanentLocationId":"87038e41-0990-49ea-abd9-1ad00a786e45","holdingsStatements":[]}
                //      ]}}}
    
                log.debug("Response for holdings on ${id}\n\n${r1.data}\n\n");
    
                r1.data.instance_storage_instances_SINGLE?.holdingsRecords2?.each { hr ->
                  log.debug("Process holdings record ${hr}");
                  String location = hr.permanentLocation.code
                  String[] split_location = location.split('/')
                  if ( split_location.length == 4 ) {
                    // If we successfully parsed the location as a 4 part string: TempleI/TempleC/Temple/Temple
                    
                    if ( result.find { it.symbol==('RESHARE:'+split_location[0]) } == null ) {
                      // And we don't already have the location
                      result.add([symbol:'RESHARE:'+split_location[0], illPolicy:hr.illPolicy?.name])
                    }
                  }
                }
              }
              else {
                log.error("Unexpected data back from shared index");
              }
            }

            response.failure { FromServer fs, Object r1 ->
              log.warn("** Failure response from shared index ${fs.getStatusCode()} when attempting to send Graphql to ${shared_index_base_url}/graphql - query: ${query}");
              log.warn("** Response object: ${r1}");
            }

          }
        }
        else {
          log.warn("Unable to login to remote shared index");
        }
      }
      catch ( Exception e ) {
        log.error("Problem attempting get in shared index",e);
      }
      finally {
        log.debug("Shared index known item lookup returned. ${System.currentTimeMillis() - start_time} elapsed");
      }
    }
    else {
      def missingSettings = [];
      if( shared_index_base_url == null ) {
        missingSettings += "url";
      }
      if( shared_index_user == null ) {
        missingSettings += "user";
      }
      if( shared_index_pass == null ) {
        missingSettings += "password";
      }
      log.debug("Unable to contact shared index - Missing settings: ${missingSettings}");
    }

    log.debug("Result: ${result}");
    return result;
  }
}

