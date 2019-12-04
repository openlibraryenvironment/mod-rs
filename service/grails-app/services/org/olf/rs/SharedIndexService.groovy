package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import java.util.concurrent.ThreadLocalRandom;
import com.k_int.web.toolkit.settings.AppSetting
import static groovyx.net.http.HttpBuilder.configure
import groovy.json.JsonOutput;



/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class SharedIndexService {

  public static def mockData = [
    [ symbol: 'RESHARE:DIKUA' ],
    [ symbol: 'RESHARE:KNOWINT01' ],
    [ symbol: 'RESHARE:TESTINST01' ],
    [ symbol: 'RESHARE:TESTINST02' ],
    [ symbol: 'RESHARE:TESTINST03' ],
    [ symbol: 'RESHARE:TESTINST04' ],
    [ symbol: 'RESHARE:TESTINST05' ],
    [ symbol: 'RESHARE:TESTINST06' ],
    [ symbol: 'RESHARE:TESTINST07' ],
    [ symbol: 'RESHARE:TESTINST09' ],
    [ symbol: 'RESHARE:TESTINST10' ]
  ]



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
        sharedIndexHoldings(description?.systemInstanceIdentifier).each { ls ->
          result.add(new AvailabilityStatement(symbol:ls, instanceIdentifier:null, copyIdentifier:null));
        }
      }
      else {
        log.warn("No shared index identifier for record. Cannot use shared index");
      }
    }
    catch ( Exception e ) {
      log.error("Graphql failed",e);
    }

    return result;
  }

  public List<AvailabilityStatement> createRandomRota(Map description) {

    List<AvailabilityStatement> result = new ArrayList<AvailabilityStatement>()

    try {
      log.debug("Try graphql")
      sharedIndexHoldings('491fe34f-ea1b-4338-ad20-30b8065a7b46');
    }
    catch ( Exception e ) {
      log.error("Graphql failed",e);
    }

    log.debug("findAppropriateCopies(${description}) - tenant is ${Tenants.currentId()}");

    List<String> all_libs = mockData.collect { it.symbol };
    int num_responders = ThreadLocalRandom.current().nextInt(0, 5 + 1);

    List<String> lendingStrings = new ArrayList<String>();
    for ( int i=0; i<num_responders; i++ ) {
      lendingStrings.add(all_libs.remove(ThreadLocalRandom.current().nextInt(0,all_libs.size())));
    }

    log.debug("Decded these are the lenders: Num lenders: ${num_responders} ${lendingStrings}");

    lendingStrings.each { ls ->
      String instance_id = null;
      String copy_id = null; // java.util.UUID.randomUUID().toString();
      result.add(new AvailabilityStatement(symbol:ls,instanceIdentifier:instance_id,copyIdentifier:copy_id));
    }
    // result.add(new AvailabilityStatement(symbol:'RESHARE:LOCALSYMBOL',instanceIdentifier:'MOCK_INSTANCE_ID_00001',copyIdentifier:'MOCK_COPY_ID_00001'));

    // Return an empty list
    return result;
  }

  public String fetchSharedIndexRecord(String id) {
    log.debug("fetchSharedIndexRecord(${id})");

    String result = null;
    AppSetting shared_index_base_url_setting = AppSetting.findByKey('shared_index_base_url');
    AppSetting shared_index_user_setting = AppSetting.findByKey('shared_index_user');
    AppSetting shared_index_pass_setting = AppSetting.findByKey('shared_index_pass');

    String shared_index_base_url = shared_index_base_url_setting?.value ?: shared_index_base_url_setting?.defValue;
    String shared_index_user = shared_index_user_setting?.value ?: shared_index_user_setting?.defValue;
    String shared_index_pass = shared_index_pass_setting?.value ?: shared_index_pass_setting?.defValue;
    String shared_index_tenant = 'diku'

    if ( ( shared_index_base_url != null ) &&
         ( shared_index_user != null ) &&
         ( shared_index_pass != null ) ) {
      log.debug("Attempt to retrieve shared index record ${id}");
      String token = getOkapiToken(shared_index_base_url, shared_index_user, shared_index_pass, shared_index_tenant);
      if ( token ) {
        def r1 = configure {
           request.headers['X-Okapi-Tenant'] = shared_index_tenant;
           request.headers['X-Okapi-Token'] = token
          request.uri = shared_index_base_url+'/inventory/instances/491fe34f-ea1b-4338-ad20-30b8065a7b46'
        }.get()
        if ( r1 ) {
          result = JsonOutput.toJson(r1);
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

  private List<String> sharedIndexHoldings(String id) {

    List<String> result = []

  // "query": "query($id: String!) { instance_storage_instances_SINGLE(instanceId: $id) { id title holdingsRecord2 { holdingsInstance { id callNumber holdingsStatements } } } }",
    String query='''{
  "query": "query($id: String!) { instance_storage_instances_SINGLE(instanceId: $id) { id title holdingsRecords2 { id callNumber permanentLocation { name code } holdingsStatements { note statement } bareHoldingsItems { id barcode enumeration } } } }",
  "variables":{
    "id":"'''+id+'''" } }'''

    log.debug("Sending graphql to get holdings for (${id}) \n${query}\n");

    AppSetting shared_index_base_url_setting = AppSetting.findByKey('shared_index_base_url');
    AppSetting shared_index_user_setting = AppSetting.findByKey('shared_index_user');
    AppSetting shared_index_pass_setting = AppSetting.findByKey('shared_index_pass');

    String shared_index_base_url = shared_index_base_url_setting?.value ?: shared_index_base_url_setting?.defValue;
    String shared_index_user = shared_index_user_setting?.value ?: shared_index_user_setting?.defValue;
    String shared_index_pass = shared_index_pass_setting?.value ?: shared_index_pass_setting?.defValue;
    String shared_index_tenant = 'diku'

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
          }.get()

          if ( ( r1 != null ) &&
               ( r1.data != null ) ) {
            // We got a response from the GraphQL service - example {"data":
            // {"instance_storage_instances_SINGLE":
            //     {"id":"5be100af-1b0a-43fe-bcd6-09a67fb9c779","title":"A history of the twentieth century in 100 maps",
            //      "holdingsRecords2":[
            //         {"id":"d045fd86-fdcf-455f-8f42-e7bbaaf5ddd6","callNumber":" GA793.7.A1 ","permanentLocationId":"87038e41-0990-49ea-abd9-1ad00a786e45","holdingsStatements":[]}
            //      ]}}}
  
            log.debug("Response for holdings on ${id}\n\n${r1.data}\n\n");
  
            r1.data.instance_storage_instances_SINGLE.holdingsRecords2.each { hr ->
              log.debug("Process holdings record ${hr}");
              String location = hr.permanentLocation.code
              String[] split_location = location.split('/')
              if ( split_location.length == 4 ) {
                // If we successfully parsed the location as a 4 part string: TempleI/TempleC/Temple/Temple
                if ( ! result.contains('RESHARE:'+split_location[0]) ) {
                  // And we don't already have the location
                  result.add('RESHARE:'+split_location[0])
                }
              }
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
      log.debug("Unable to contact shared index - no url/user/pass");
    }

    log.debug("Result: ${result}");
    return result;
  }
}

