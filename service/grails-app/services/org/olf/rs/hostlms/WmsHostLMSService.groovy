package org.olf.rs.hostlms;

import org.olf.rs.circ.client.CirculationClient;
import org.olf.rs.circ.client.NCIPClientWrapper;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.ISettings;

import groovyx.net.http.HttpBuilder;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class WmsHostLMSService extends BaseHostLMSService {

  public CirculationClient getCirculationClient(ISettings settings, String address) {
    String wms_api_key = settings.getSettingValue(SettingsData.SETTING_WMS_API_KEY);
    String wms_api_secret = settings.getSettingValue(SettingsData.SETTING_WMS_API_SECRET);
    String wms_lookup_patron_endpoint = settings.getSettingValue(SettingsData.SETTING_WMS_LOOKUP_PATRON_ENDPOINT);

    // This wrapper creates the circulationClient we need
    return new NCIPClientWrapper(address, [
      protocol: "WMS",
      apiKey: wms_api_key,
      apiSecret: wms_api_secret,
      lookupPatronEndpoint: wms_lookup_patron_endpoint
      ]).circulationClient;
  }

  @Override
  public boolean isNCIP2() {
    return true;
  }

  List getLookupStrategies() {
    [
      [
        name:'Adapter_By_OCLC_Number',
        precondition: { pr -> return ( pr.oclcNumber != null ) },
        strategy: { pr, service, settings -> return service.lookupViaConnector("rec.identifier=${pr.oclcNumber?.trim()}&startRecord=1&maximumRecords=3", settings) }
      ],
      [
        name:'Adapter_By_Title_And_Identifier',
        precondition: { pr -> return ( pr.isbn != null && pr.title != null ) },
        strategy: { pr, service, settings -> return service.lookupViaConnector("dc.title=${pr.title?.trim()} and bath.isbn=${pr.isbn?.trim()}&startRecord=1&maximumRecords=3", settings) }
      ],
      [
        name:'Adapter_By_ISBN_Identifier',
        precondition: { pr -> return ( pr.isbn != null ) },
        strategy: { pr, service, settings -> return service.lookupViaConnector("bath.isbn=${pr.isbn?.trim()}&startRecord=1&maximumRecords=3", settings) }
      ],
      [
        name:'Adapter_By_Title',
        precondition: { pr -> return ( pr.title != null ) },
        strategy: { pr, service, settings -> return service.lookupViaConnector("dc.title=${pr.title?.trim()}&startRecord=1&maximumRecords=3", settings) }
      ]
    ]
  }

  List<ItemLocation> lookupViaConnector(String query, ISettings settings) {

    List<ItemLocation> result = [];

    //Override this method on BaseHost to use RTAC connector provided by IndexData
    String wms_connector_address = settings.getSettingValue(SettingsData.SETTING_WMS_CONNECTOR_ADDRESS);
    String wms_connector_username = settings.getSettingValue(SettingsData.SETTING_WMS_CONNECTOR_USERNAME);
    String wms_connector_password = settings.getSettingValue(SettingsData.SETTING_WMS_CONNECTOR_PASSWORD);
    String wms_api_key = settings.getSettingValue(SettingsData.SETTING_WMS_API_KEY);
    String wms_api_secret = settings.getSettingValue(SettingsData.SETTING_WMS_API_SECRET);
    String wms_registry_id = settings.getSettingValue(SettingsData.SETTING_WMS_REGISTRY_ID);

    //API key and API secret get embedded in the URL
    String z3950Connector = "${wms_connector_address},user=${wms_api_key}&password=${wms_api_secret}&x-registryId=${wms_registry_id}"

    def z_response = HttpBuilder.configure {
      request.uri = z3950Connector
    }.get {
        request.uri.query = [
                              'version':'2.0',
                              'operation': 'searchRetrieve',
                              'x-username': wms_connector_username,
                              'x-password': wms_connector_password,
                              'query': query

                            ]
        log.debug("Querying connector with URL ${request.uri?.toURI().toString()}");
    }
    log.debug("Got Z3950 response: ${z_response}")

    if ( z_response?.numberOfRecords?.size() > 0) {
      List<ItemLocation> availability_summary = [];
      result = z_response?.records?.record?.recordData?.opacRecord?.holdings?.holding?.findResults { hld ->
        if ( hld.circulations?.circulation?.availableNow?.@value=='1' ) {
          log.debug("Holding available now");
          ItemLocation il = new ItemLocation( location: hld.localLocation, shelvingLocation:hld.shelvingLocation, callNumber:hld.callNumber )
          availability_summary << il;
          if( availability_summary?.size() > 0) { result = availability_summary; }
        } else {
          log.debug("Holding unavailable");
          return null;
        }
      }
      log.debug("At end, availability summary: ${availability_summary}");
    } else {
      log.debug("WMS connector lookup (${query}) returned no matches. Unable to determine availability.")
    }

    return result
  }
}
