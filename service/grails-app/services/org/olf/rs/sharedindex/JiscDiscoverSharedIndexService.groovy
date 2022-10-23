package org.olf.rs.sharedindex;

import grails.gorm.multitenancy.Tenants
import java.util.concurrent.ThreadLocalRandom;
import org.olf.okapi.modules.directory.Symbol;
import com.k_int.web.toolkit.settings.AppSetting
import javax.servlet.http.HttpServletRequest
import groovyx.net.http.HttpBuilder
import static groovyx.net.http.HttpBuilder.configure
import groovy.json.JsonOutput;
import groovyx.net.http.FromServer;
import org.olf.rs.SharedIndexActions;


/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class JiscDiscoverSharedIndexService implements SharedIndexActions {

 /**
   * See: https://discover.libraryhub.jisc.ac.uk/sru-api?operation=searchRetrieve&version=1.1&query=rec.id%3d%2231751908%22&maximumRecords=1
   *
   * findAppropriateCopies - Accept a map of name:value pairs that describe an instance and see if we can locate
   * any appropriate copies in the shared index.
   * @param description A Map of properies that describe the item. Currently understood properties:
   *                         systemInstanceIdentifier - Instance identifier for the title we want copies of
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
        log.debug("Query shared index for holdings of system instance identifier: ${description?.systemInstanceIdentifier}");

        /*
        sharedIndexHoldings(description?.systemInstanceIdentifier).each { shared_index_availability ->
          log.debug("add shared index availability: ${shared_index_availability}");

          // We need to look through the identifiers to see if there is an identifiier where identifierTypeObject.name == shared_index_availability.symbol
          // If so, that identifier is the instanceIdentifier in the shared index for this item - I know - it makes my brain hurt too
          

          result.add(new AvailabilityStatement(
                                               symbol:shared_index_availability.symbol, 
                                               instanceIdentifier:shared_index_availability.instanceIdentifier, 
                                               copyIdentifier:shared_index_availability.copyIdentifier,
                                               illPolicy:shared_index_availability.illPolicy));
        }
        */
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
  public List<String> fetchSharedIndexRecords(Map description) {
    String id = description?.systemInstanceIdentifier;
    if (!id) {
      log.debug("Unable to retrieve shared index record, systemInstanceIdentifier not specified and other fields not supported by this implementation");
      return null;
    }

    log.debug("fetchSharedIndexRecord(${id})");

    List<String> result = [];

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
            log.debug("Success response from shared index");
            result = [JsonOutput.toJson(body)];
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

}

