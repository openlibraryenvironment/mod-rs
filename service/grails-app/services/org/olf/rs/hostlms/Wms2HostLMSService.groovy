package org.olf.rs.hostlms;

import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.circ.client.CirculationClient
import org.olf.rs.lms.ItemLocation

import com.k_int.web.toolkit.settings.AppSetting


public class Wms2HostLMSService extends BaseHostLMSService {

  @Override
  public CirculationClient getCirculationClient(String address) {
    AppSetting wms_api_key = AppSetting.findByKey('wms_api_key')
    AppSetting wms_api_secret = AppSetting.findByKey('wms_api_secret')
    AppSetting wms_lookup_patron_endpoint = AppSetting.findByKey('wms_lookup_patron_endpoint')
    
    
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [
      protocol: "WMS2",
      apiKey: wms_api_key?.value,
      apiSecret: wms_api_secret?.value,
      lookupPatronEndpoint: wms_lookup_patron_endpoint?.value
      ]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
  }

  @Override
  List getLookupStrategies() {
    [
      [
        name:'Adapter_By_OCLC_Number',
        precondition: { pr -> return ( pr.oclcNumber != null ) },
        strategy: { pr, service -> return service.lookupViaConnector("rec.identifier=${pr.oclcNumber?.trim()}&startRecord=1&maximumRecords=3") }
      ],
      [
        name:'Adapter_By_Title_And_Identifier',
        precondition: { pr -> return ( pr.isbn != null && pr.title != null ) },
        strategy: { pr, service -> return service.lookupViaConnector("dc.title=${pr.title?.trim()} and bath.isbn=${pr.isbn?.trim()}&startRecord=1&maximumRecords=3") }
      ],
      [
        name:'Adapter_By_ISBN_Identifier',
        precondition: { pr -> return ( pr.isbn != null ) },
        strategy: { pr, service -> return service.lookupViaConnector("bath.isbn=${pr.isbn?.trim()}&startRecord=1&maximumRecords=3") }
      ],
      [
        name:'Adapter_By_Title',
        precondition: { pr -> return ( pr.title != null ) },
        strategy: { pr, service -> return service.lookupViaConnector("dc.title=${pr.title?.trim()}&startRecord=1&maximumRecords=3") }
      ],
    ]
  }

  List<ItemLocation> lookupViaConnector(String query) {

    //Override this method on BaseHost to use RTAC connector provided by IndexData
    AppSetting wms_connector_address = AppSetting.findByKey('wms_connector_address')
    AppSetting wms_connector_username = AppSetting.findByKey('wms_connector_username')
    AppSetting wms_connector_password = AppSetting.findByKey('wms_connector_password')
    AppSetting wms_api_key = AppSetting.findByKey('wms_api_key')
    AppSetting wms_api_secret = AppSetting.findByKey('wms_api_secret')
    AppSetting wms_registry_id = AppSetting.findByKey('wms_registry_id')


    //API key and API secret get embedded in the URL
    String z3950Connector = "${wms_connector_address?.value},user=${wms_api_key?.value}&password=${wms_api_secret?.value}&x-registryId=${wms_registry_id?.value}"

    def z_response = HttpBuilder.configure {
      request.uri = z3950Connector
    }.get {
      request.uri.query = [
              'version':'2.0',
              'operation': 'searchRetrieve',
              'x-username': wms_connector_username?.value,
              'x-password': wms_connector_password?.value,
              'query': query

      ]
      log.debug("Querying connector with URL ${request.uri?.toURI().toString()}");
    }
    log.debug("Got Z3950 response: ${z_response}")

    return extractAvailableItemsFrom(z_response);
  }

  @Override
  protected List<ItemLocation> extractAvailableItemsFrom(z_response, String reason=null) {
    List<ItemLocation> availability_summary = [];
    if ( z_response?.numberOfRecords?.size() > 0) {
      def withHoldings = z_response.records.record.findAll {
        it?.recordData?.opacRecord?.holdings?.holding?.size() > 0
      };
      if (withHoldings.size() < 1) {
        log.warn("WmsHostLMSService failed to find an OPAC record with holdings");
        return null;
      }
      def opacRecord = withHoldings?.first()?.recordData?.opacRecord;
      opacRecord?.holdings?.holding?.each { hld ->
        log.debug("WmsHostLMSService holdings record: ${hld}");
        hld?.circulations?.circulation?.each { circ ->
          def loc = hld?.localLocation?.text()?.trim();
          if (loc && circ?.availableNow?.@value=='1') {
            log.debug("Holding available now");
            ItemLocation il = new ItemLocation(
                    location: loc,
                    shelvingLocation:hld.shelvingLocation?.text()?.trim(),
                    callNumber:hld.callNumber.text()?.trim()
            );
            availability_summary << il;
          }
        }
      }
      log.debug("At end, availability summary: ${availability_summary}");
    } else {
      log.debug("WMS connector lookup returned no matches. Unable to determine availability.")
    }

    return availability_summary;
  }
}