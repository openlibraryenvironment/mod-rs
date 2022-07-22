package org.olf.rs

import com.k_int.web.toolkit.settings.AppSetting
import groovy.xml.XmlUtil
import groovyx.net.http.FromServer

import static groovyx.net.http.HttpBuilder.configure

public class ModmetastorageSharedIndexService implements SharedIndexActions {

  final String LENDABLE_SI = 'LOANABLE';
  final String LENDABLE_RS = 'Will lend';

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

  // This method can't be private at the moment or it'll break the test that stubs it
  // Providing a method signature didn't seem to help https://issues.apache.org/jira/browse/GROOVY-7368
  Object fetchCluster(String id) {
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
          request.uri = shared_index_base_url+'/meta-storage/oai?verb=GetRecord&identifier='+(id.trim());
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
    if (cluster.getClass()?.name != 'groovy.util.slurpersupport.NodeChild') {
      log.error("Shared index record via OAI not in expected XML format.");
    } else {
      XmlUtil xmlUtil = new XmlUtil();
      result = [xmlUtil.serialize(cluster)];
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
    if (cluster.getClass()?.name != 'groovy.util.slurpersupport.NodeChild') {
      log.error("Shared index record via OAI not in expected XML format.");
      // Just sticking with the pattern established by the other implementation but perhaps we should actually throw at this point?
      return result;
    }

    def cfg = [
      'tag': '999',
      'ind1': '1',
      'ind2': '1',
      'localIdSub': 'l',
      'symbolSub': 's',
      'policySub': 'p',
    ];

    cluster?.GetRecord?.record?.metadata?.record?.datafield?.findAll { it.'@tag' == cfg.tag && it.'@ind1' == cfg.ind1 && it.'@ind2' == cfg.ind2 }.each { field ->
      String localId = field?.subfield.find { it.'@code' == cfg.localIdSub }.text();
      String sym = field?.subfield.find { it.'@code' == cfg.symbolSub }.text();
      String pol = field?.subfield.find { it.'@code' == cfg.policySub }.text() ?: LENDABLE_RS;
      if (pol == LENDABLE_SI) pol = LENDABLE_RS;

      if (sym && localId) {
        // Do we already have an entry in the result for the given symbol?
        def existing = result.find { it.symbol == sym };
        if ( existing == null) {
          log.debug("Adding holding for ${sym} - ${localId} with policy ${pol}");
          result.add([
                  symbol            : sym,
                  illPolicy         : pol,
                  instanceIdentifier: localId,
                  copyIdentifier    : null])
        } else {
          log.debug("Located existing entry in result for ${sym} - not adding another");
          if (existing?.illPolicy != LENDABLE_RS && pol == LENDABLE_RS) {
            log.debug("Updating existing entry for ${sym} - found lendable copy");
            existing.illPolicy = LENDABLE_RS;
          }
        }
      } else {
        log.error("Unexpected data back from shared index, symbol or localId missing");
      }
    }

    log.debug("Result: ${result}");
    return result;
  }
}

