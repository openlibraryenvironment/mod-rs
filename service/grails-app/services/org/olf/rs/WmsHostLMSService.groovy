package org.olf.rs;

import org.olf.rs.PatronRequest
import groovyx.net.http.HttpBuilder
import org.olf.rs.lms.ItemLocation

import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.circ.client.CirculationClient

import com.k_int.web.toolkit.settings.AppSetting


/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class WmsHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(String address) {
    AppSetting wms_api_key = AppSetting.findByKey('wms_api_key')
    AppSetting wms_api_secret = AppSetting.findByKey('wms_api_secret')
    AppSetting wms_lookup_patron_endpoint = AppSetting.findByKey('wms_lookup_patron_endpoint')
    
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [
      protocol: "WMS",
      apiKey: wms_api_key.value,
      apiSecret: wms_api_secret.value,
      lookupPatronEndpoint: wms_lookup_patron_endpoint.value
      ]).circulationClient;
  }

  def lookup_strategies = [
    [ 
      name:'Adapter_By_OCLC_Number',
      precondition: { pr -> return ( pr.oclcNumber != null ) },
      strategy: { pr, service -> return service.lookupViaAdpater("rec.identifier=${pr.oclcNumber?.trim()}&startRecord=1&maximumRecords=1") }
    ],
    [ 
      name:'Adapter_By_Title_And_Identifier',
      precondition: { pr -> return ( pr.isbn != null && pr.title != null ) },
      strategy: { pr, service -> return service.lookupViaAdpater("dc.title=${pr.title?.trim()} and bath.isbn=${pr.isbn?.trim()}&startRecord=1&maximumRecords=1") }
    ],
    [
      name:'Adapter_By_Identifier',
      precondition: { pr -> return ( pr.isbn != null ) },
      strategy: { pr, service -> return service.lookupViaAdpater("bath.isbn=${pr.isbn?.trim()}&startRecord=1&maximumRecords=1") }
    ],
    [
      name:'Adapter_By_Title',
      precondition: { pr -> return ( pr.title != null ) },
      strategy: { pr, service -> return service.lookupViaAdpater("dc.title=${pr.title?.trim()}&startRecord=1&maximumRecords=1") }
    ],
  ]

  ItemLocation determineBestLocation(PatronRequest pr) {

    log.debug("determineBestLocation(${pr})");

    ItemLocation location = null;
    Iterator i = lookup_strategies.iterator();
    
    while ( ( location==null ) && ( i.hasNext() ) ) {
      def next_strategy = i.next();
      log.debug("Next lookup strategy: ${next_strategy.name}");
      if ( next_strategy.precondition(pr) == true ) {
        log.debug("Strategy ${next_strategy.name} passed precondition");
        try {
          location = next_strategy.strategy(pr, this);
        }
        catch ( Exception e ) {
          log.error("Problem attempting strategy ${next_strategy.name}",e);
        }
        finally {
          log.debug("Completed strategy ${next_strategy.name}");
        }
     
      }
      else {
        log.debug("Strategy did not pass precondition");
      }
    }
    
    log.debug("determineBestLocation returns ${location}");
    return location;
  }

  ItemLocation lookupViaAdpater(String query) {
    ItemLocation result = null;

    //Override this method on BaseHost to use RTAC connector provided by IndexData
    String z3950Connector = "http://mkc.indexdata.com:9008/STU"

    log.debug "RESOLVED SYMBOL: ${pr.resolvedSupplier.toString()}"

    def z_response = HttpBuilder.configure {
      request.uri = z3950Connector
    }.get {
        request.uri.query = [
                              'version':'1.1',
                              'operation': 'searchRetrieve',
                              'x-username': 'reshare',
                              'x-password': 'reshare0618',
                              'query': query
                            ]
    }
    log.debug("Got Z3950 response: ${z_response}")

    if ( z_response?.numberOfRecords == 1 ) {
      // SEE http://mkc.indexdata.com:9008/STU?version=1.1&operation=searchRetrieve&x-username=reshare&x-password=reshare0618&query=rec.identifier=42354631&startRecord=1&maximumRecords=1

      // Got exactly 1 record
      Map availability_summary = [:]
      z_response?.records?.record?.recordData?.opacRecord?.holdings?.holding?.each { hld ->
        log.debug("HOLDING ${hld}");
        log.debug("HOLDING AVAILABLE NOW ${hld.circulations?.circulation?.availableNow}");
        log.debug("HOLDING AVAILABLE NOW VALUE ${hld.circulations?.circulation?.availableNow?.@value}");
        if ( hld.circulations?.circulation?.availableNow?.@value=='1' ) {
          log.debug("Available now");
          ItemLocation il = new ItemLocation( location: hld.localLocation, shelvingLocation:hld.shelvingLocation, callNumber:hld.callNumber )

          if ( result == null ) 
            result = il;

          availability_summary[hld.localLocation] = il;
        }
      }

      log.debug("At end, availability summary: ${availability_summary}");
    }
    log.debug "LOGDEBUG RESULT: ${result}"
    return result
  }
}
