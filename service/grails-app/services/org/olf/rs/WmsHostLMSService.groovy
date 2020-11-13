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
    AppSetting ncip_api_key = AppSetting.findByKey('ncip_api_key')
    AppSetting ncip_api_secret = AppSetting.findByKey('ncip_api_secret')
    AppSetting wms_lookup_patron_endpoint = AppSetting.findByKey('wms_lookup_patron_endpoint')
    
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [
      protocol: "WMS",
      apiKey: ncip_api_key.value,
      apiSecret: ncip_api_secret.value,
      lookupPatronEndpoint: wms_lookup_patron_endpoint.value
      ]).circulationClient;
  }

  ItemLocation determineBestLocation(PatronRequest pr) {
    ItemLocation result = null;

    //Override this method on BaseHost to use RTAC connector provided by ID
    String z3950Connector = "http://mkc.indexdata.com:9008/STU"
    def reqItem1 = 710733
    def reqItem2 = 42354631

    log.debug "RESOLVED SYMBOL: ${pr.resolvedSupplier.toString()}"

    def z_response = HttpBuilder.configure {
        request.uri = z3950Connector
      }.get {
          request.uri.query = [
                                'version':'1.1',
                                'operation': 'searchRetrieve',
                                'x-username': 'reshare',
                                'x-password': 'reshare0618',
                                'query': "rec.identifier=${reqItem1}&startRecord=1&maximumRecords=1"
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
