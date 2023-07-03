package org.olf.rs.hostlms;

import org.olf.rs.circ.client.NCIPClientWrapper
import org.olf.rs.circ.client.CirculationClient
import groovyx.net.http.HttpBuilder
import org.olf.rs.lms.ItemLocation
import org.olf.rs.logging.IHoldingLogDetails;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.settings.ISettings;

import groovy.xml.XmlUtil;



public class Wms2HostLMSService extends BaseHostLMSService {

  @Override
  public CirculationClient getCirculationClient(ISettings settings, String address) {
    String wms_api_key = settings.getSettingValue(SettingsData.SETTING_WMS_API_KEY);
    String wms_api_secret = settings.getSettingValue(SettingsData.SETTING_WMS_API_SECRET);
    String wms_lookup_patron_endpoint = settings.getSettingValue(SettingsData.SETTING_WMS_LOOKUP_PATRON_ENDPOINT);
    
    
    // TODO this wrapper contains the 'send' command we need and returns a Map rather than JSONObject, consider switching to that instead
    return new NCIPClientWrapper(address, [
      protocol: "WMS2",
      apiKey: wms_api_key,
      apiSecret: wms_api_secret,
      lookupPatronEndpoint: wms_lookup_patron_endpoint
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
        strategy: { pr, service, settings, holdingLogDetails ->
          return service.lookupViaConnector(
                  "rec.identifier=${pr.oclcNumber?.trim()}&startRecord=1&maximumRecords=3",
                  settings, holdingLogDetails
          ) }
      ],
      [
        name:'Adapter_By_Title_And_Identifier',
        precondition: { pr -> return ( pr.isbn != null && pr.title != null ) },
        strategy: { pr, service, settings, holdingLogDetails ->
          return service.lookupViaConnector(
                  "dc.title=${pr.title?.trim()} and bath.isbn=${pr.isbn?.trim()}&startRecord=1&maximumRecords=3",
                  settings, holdingLogDetails
          ) }
      ],
      [
        name:'Adapter_By_ISBN_Identifier',
        precondition: { pr -> return ( pr.isbn != null ) },
        strategy: { pr, service, settings, holdingLogDetails ->
          return service.lookupViaConnector(
                  "bath.isbn=${pr.isbn?.trim()}&startRecord=1&maximumRecords=3",
                  settings, holdingLogDetails
          ) }
      ],
      [
        name:'Adapter_By_Title',
        precondition: { pr -> return ( pr.title != null ) },
        strategy: { pr, service, settings, holdingLogDetails ->
          return service.lookupViaConnector(
                  "dc.title=${pr.title?.trim()}&startRecord=1&maximumRecords=3",
                  settings, holdingLogDetails
          ) }
      ],
    ]
  }

  List<ItemLocation> lookupViaConnector(String query, ISettings settings, IHoldingLogDetails holdingLogDetails) {

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
              'x-username': wms_connector_username?.value,
              'x-password': wms_connector_password?.value,
              'query': query

      ]
      holdingLogDetails.newSearch(request.uri?.toURI().toString());
      log.debug("Querying connector with URL ${request.uri?.toURI().toString()}");
      holdingLogDetails.numberOfRecords(z_response?.numberOfRecords?.toLong());
      holdingLogDetails.searchRequest(z_response?.echoedSearchRetrieveRequest);
    }
    log.debug("Got Z3950 response: ${XmlUtil.serialize(z_response)}");

    return extractAvailableItemsFrom(z_response, null, holdingLogDetails);
  }

  @Override
  protected List<ItemLocation> extractAvailableItemsFrom(z_response, String reason, IHoldingLogDetails holdingLogDetails) {
    List<ItemLocation> availability_summary = [];
    if ( z_response?.numberOfRecords?.size() > 0) {
      holdingLogDetails.newRecord();
      def withHoldings = z_response.records.record.findAll {
        it?.recordData?.opacRecord?.holdings?.holding?.size() > 0
      };
      if (withHoldings.size() < 1) {
        log.warn("Wms2HostLMSService failed to find an OPAC record with holdings");
        return null;
      }
      def opacRecord = withHoldings?.first()?.recordData?.opacRecord;
      opacRecord?.holdings?.holding?.each { hld ->
        holdingLogDetails.holdings(hld);
        log.debug("Wms2HostLMSService holdings record: ${hld}");
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
      log.debug("WMS2 connector lookup returned no matches. Unable to determine availability.")
    }

    return availability_summary;
  }



}