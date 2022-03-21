package org.olf.rs;

import org.olf.rs.PatronRequest
import groovyx.net.http.HttpBuilder
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.statemodel.Status;
import com.k_int.web.toolkit.settings.AppSetting
import groovy.xml.StreamingMarkupBuilder
import static groovyx.net.http.HttpBuilder.configure
import groovyx.net.http.FromServer;
import com.k_int.web.toolkit.refdata.RefdataValue
import static groovyx.net.http.ContentTypes.XML
import org.olf.rs.lms.HostLMSActions;
import grails.core.GrailsApplication

/**
 * Return the right HostLMSActions for the tenant config
 *
 */
public class HostLMSService {

  GrailsApplication grailsApplication
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  StatisticsService statisticsService

  public HostLMSActions getHostLMSActionsFor(String lms) {
    log.debug("HostLMSService::getHostLMSActionsFor(${lms})");
    HostLMSActions result = grailsApplication.mainContext."${lms}HostLMSService"

    if ( result == null ) {
      log.warn("Unable to locate HostLMSActions for ${lms}. Did you fail to configure the app_setting \"host_lms_integration\". Current options are aleph|alma|FOLIO|Koha|Millennium|Sierra|Symphony|Voyager|wms|manual|default");
    }

    return result;
  }

  public HostLMSActions getHostLMSActions() {
    HostLMSActions result = null;
    AppSetting host_lms_intergation_setting = AppSetting.findByKey('host_lms_integration');
    String v = host_lms_intergation_setting?.value
    log.debug("Return host lms integrations for : ${v} - query application context for bean named ${v}HostLMSService");
    result = getHostLMSActionsFor(v);
    return result;
  }

  /* 
   *  Utility function to handle checking in volumes for a request
   */
  public Map checkInRequestVolumes(PatronRequest request) {
    
    boolean result = false;
    def resultMap = [:];
    resultMap.checkInList = [];
    resultMap.hostLMS = false;
    resultMap.errors = [];
    resultMap.complete = [:];
    /* 
    Since we don't throw errors for checking in already-checked-in items there's no
    reason not to just try and check in all of the volumes
    */
    //def volumesNotCheckedIn = request.volumes.findAll { rv -> rv.status.value == 'awaiting_lms_check_in'; }
    def volumesNotCheckedIn = request.volumes.findAll();
    def totalVolumes = volumesNotCheckedIn.size();
    HostLMSActions host_lms = getHostLMSActions();
    if (host_lms) {
      resultMap.hostLMS = true;
      for( def vol : volumesNotCheckedIn ) {
        String temporaryItemBarcode; //We want to match what we created for AcceptItem
        if(request.isRequester) {
          if (request.volumes?.size() > 1) {
            temporaryItemBarcode = "${request.hrid}-${vol.itemId}";
          } else {
            temporaryItemBarcode = request.hrid;
          }
        } else { //Use the actual barcode for supply-side requests
          temporaryItemBarcode = vol.itemId;
        }
        def checkInMap = [:];
        def check_in_result = host_lms.checkInItem(temporaryItemBarcode);
        checkInMap.result = check_in_result;
        checkInMap.volume = vol;
        String message;
        if(check_in_result?.result == true) {          
          if(check_in_result?.already_checked_in == true) {	
            message = "NCIP CheckinItem call succeeded for item: ${temporaryItemBarcode}. ${check_in_result.reason=='spoofed' ? '(No host LMS integration configured for check in item call)' : 'Host LMS integration: CheckinItem not performed because the item was already checked in.'}"
          } else {
            message = "NCIP CheckinItem call succeeded for item: ${temporaryItemBarcode}. ${check_in_result.reason=='spoofed' ? '(No host LMS integration configured for check in item call)' : 'Host LMS integration: CheckinItem call succeeded.'}"
          }          
          checkInMap.success = true;
          reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, message, null);
          def newVolStatus = check_in_result.reason=='spoofed' ? vol.lookupStatus('lms_check_in_(no_integration)') : vol.lookupStatus('completed')
          vol.status = newVolStatus
          vol.save(failOnError: true)
        } else {
          request.needsAttention=true;
          checkInMap.success = false;
          message = "Host LMS integration: NCIP CheckinItem call failed for item: ${temporaryItemBarcode}. Review configuration and try again or deconfigure host LMS integration in settings. "+check_in_result.problems?.toString();
          reshareApplicationEventHandlerService.auditEntry(
            request,
            request.state,
            request.state,
            "Host LMS integration: NCIP CheckinItem call failed for item: ${temporaryItemBarcode}. Review configuration and try again or deconfigure host LMS integration in settings. "+check_in_result.problems?.toString(),
            null);
        }
        checkInMap.message = message;
        checkInMap.state = request.state;
        resultMap.checkInList.add(checkInMap);
      }
    } else {
      def errorMap = [:];
      String message = 'Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings.';
      errorMap.message = message;
      errorMap.state = request.state;
      resultMap.errors.add(errorMap);
      reshareApplicationEventHandlerService.auditEntry(
        request,
        request.state,
        request.state,
        message,
        null);
      request.needsAttention=true;
    }
    //Make sure we don't have any hanging volumes
    volumesNotCheckedIn = request.volumes.findAll { rv -> rv.status.value == 'awaiting_lms_check_in'; }

    if(volumesNotCheckedIn.size() == 0) {
      statisticsService.decrementCounter('/activeLoans');
      request.needsAttention = false;
      request.activeLoan = false;

      if(totalVolumes > 0) {
        String message = "Complete request succeeded. Host LMS integration: CheckinItem call succeeded for all items.";
        resultMap.complete.message = message;
        resultMap.complete.state = request.state;
        reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, message, null);
      } else {
        log.debug("No items found to check in for request ${request.id}");
      }
      result = true;
    } else {
      def errorMap = [:];
      String message = "Host LMS integration: NCIP CheckinItem calls failed for some items."
      reshareApplicationEventHandlerService.auditEntry(request,
        request.state,
        request.state,
        message,
        null);
      errorMap.message = message;
      errorMap.state = request.state;
      resultMap.errors.add(errorMap);
      request.needsAttention = true;
    }
    resultMap.result = result;
    return resultMap;
    
  }

}
