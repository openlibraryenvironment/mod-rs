package org.olf.rs

import com.k_int.web.toolkit.settings.AppSetting
import groovy.json.JsonOutput
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.SharedIndexActions

import javax.servlet.http.HttpServletRequest

import static groovyx.net.http.HttpBuilder.configure

public class ModsiSharedIndexService implements SharedIndexActions {

 /**
   * findAppropriateCopies - Accept a map of name:value pairs that describe an instance and see if we can locate
   * any appropriate copies in the shared index.
   * @param description A Map of properies that describe the item. Currently understood properties:
   *                         title - the title of the item
   * @return instance of SharedIndexAvailability which tells us where we can find the item.
   */
  public List<AvailabilityStatement> findAppropriateCopies(Map description) {

    List<AvailabilityStatement> result = []
    log.debug("mod-shared-index findAppropriateCopies(${description})");

    // Use the shared index to try and obtain a list of locations
    try {
      if ( description?.systemInstanceIdentifier != null ) {
        log.debug("Query mod-shared-index for holdings of system instance identifier: ${description?.systemInstanceIdentifier}");

        sharedIndexHoldings(description?.systemInstanceIdentifier).each { shared_index_availability ->
          log.debug("add shared index availability: ${shared_index_availability}");

          // We need to look through the identifiers to see if there is an identifier where identifierTypeObject.name == shared_index_availability.symbol
          // If so, that identifier is the instanceIdentifier in the shared index for this item - I know - it makes my brain hurt too
          

          result.add(new AvailabilityStatement(
                                               symbol:shared_index_availability.symbol, 
                                               instanceIdentifier:shared_index_availability.instanceIdentifier, 
                                               copyIdentifier:shared_index_availability.copyIdentifier,
                                               illPolicy:shared_index_availability.illPolicy));
        }
      }
      else {
        log.warn("No shared index identifier for record. Cannot use shared index");
      }
    }
    catch ( Exception e ) {
      log.error("Failure in mod-shared-index lookup", e);
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

  private Object fetchCluster(String id) {
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
          request.headers['X-Okapi-Token'] = token;
          request.uri = shared_index_base_url+'/shared-index/clusters/'+(id.trim());
        }.get() {
          response.success { FromServer fs, Object body ->
            log.debug("Success response from shared index");
            return body;
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
  }

  public List<String> fetchSharedIndexRecords(Map description) {
    String id = description?.systemInstanceIdentifier;
    if (!id) {
      log.debug("Unable to retrieve shared index record, systemInstanceIdentifier not specified and other fields not supported by this implementation");
      return null;
    }
    log.debug("fetchSharedIndexRecord(${id})");

    List<String> result = [];
    Object cluster = fetchCluster(id);
    Object instance = cluster?.records[0]?.inventoryPayload?.instance;
    if (!instance) {
      log.debug("Unable to retrieve shared index record, systemInstanceIdentifier not specified and other fields not supported by this implementation");
    } else {
      result = [JsonOutput.toJson(instance)];
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
        request.body = postBody
      }.post() {
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
        response.failure { resp, body ->
          log.error("RESP ERROR: ${resp.getStatusCode()}, ${resp.getMessage()}, ${body}, ${resp.getHeaders()}")
        }
      }
    }
    catch ( Exception e ) {
        log.error("problem trying to obtain auth token for shared index",e);
      }

    log.debug("Result of okapi login: ${result}");
    return result;
  }


  private List<Map> sharedIndexHoldings(String id) {
    log.debug("sharedIndexHoldings(${id})");
    List<Map> result = [];
    Object cluster = fetchCluster(id);
    Object payload = cluster?.records[0]?.inventoryPayload
    Object holdingsRecords = payload?.holdingsRecords;
    Object instance = payload?.instance;

    if (holdingsRecords && instance) {
      log.debug("Response for holdings on ${id}\n\n${holdingsRecords}\n\n");

      holdingsRecords?.each { hr ->
        log.debug("Process holdings record ${hr}");
        String location = hr.permanentLocationDeref;
        String[] split_location = location.split('/')
        if ( split_location.length == 4 ) {
          // If we successfully parsed the location as a 4 part string: TempleI/TempleC/Temple/Temple

          String local_symbol = convertSILocationToSymbol(split_location[0])
          if ( local_symbol != null ) {

            // Do we already have an entry in the result for the given location? If not, Add it
            if ( result.find { it.symbol==local_symbol } == null ) {
              // And we don't already have the location

              // Iterate through identifiers to try and find one with the same identifierTypeObject.name as our symbol
              // Very unsure about this, so wrapping for now
              def instance_identifier = null;
              try {
                instance_identifier = instances?.identifiers.find { it.identifierTypeDeref?.equalsIgnoreCase(local_symbol)} ?. value
              }
              catch ( Exception e ) {
                e.printStackTrace()
              }

              log.debug("adding ${local_symbol} - ${instance_identifier} with policy ${hr.illPolicyDerefs}");
              result.add([
                      symbol:local_symbol,
                      illPolicy:hr.illPolicyDeref,
                      instanceIdentifier:instance_identifier,
                      copyIdentifier:null ])
            }
            else {
              log.debug("Located existing entry in result for ${location} - not adding another");
            }
          }
          else {
            log.warn("Unable to resolve shared index symbol ${split_location}");
          }
        }
        else {
          log.warn("Location code does not split into 4: ${location}");
        }
      }
    }
    else {
      log.error("Unexpected data back from shared index");
    }

    log.debug("Result: ${result}");
    return result;
  }

  private String convertSILocationToSymbol(String si_location) {
    log.debug("convertSILocationToSymbol(${si_location})");
    // return 'RESHARE:'+si_location
    String result = null;
    // Try to resolve the symbol without a namespace
    List<Symbol> r = Symbol.findAllBySymbol(si_location.trim().toUpperCase())
    if ( r.size() == 1 ) {
      log.debug("Located unique symbol without namespace");
      Symbol s = r.get(0)
      result = "${s.authority.symbol}:${s.symbol}".toString()
    }
    else if ( r.size() > 1 ) {
      log.debug("Symbol is not unique over namespace");
      // The symbol was not uniqe over namespaces, try our priority list 
      ['RESHARE', 'ISIL'].each {
        log.debug("Trying to locate symbol ${si_location} in ns ${it}");
        if ( result == null ) {
          try {
            List<Symbol> r2 = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol=:a and s.symbol=s',[a:it, s:si_location.trim().toUpperCase()]);
            if ( r2.size() == 1 ) {
              Symbol s = r2.get(0)
              result = "${s.authority.symbol}:${s.symbol}".toString()
            }
          } catch(Exception e) {
            log.error("Error trying to locate symbol ${si_location} in ${it}: ${e.getMessage()}")            
          }
        }
      }
    }
    log.debug("convertSILocationToSymbol result: ${si_location}");
    return result
  }

}

