package org.olf.rs;

import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.logging.IHoldingLogDetails;
import org.olf.rs.logging.INcipLogDetails;
import org.olf.rs.logging.ProtocolAuditService;

import com.k_int.web.toolkit.settings.AppSetting;

import grails.core.GrailsApplication
import org.olf.rs.settings.ISettings;

/**
 * Return the right HostLMSActions for the tenant config
 *
 */
public class HostLMSService {

    private static final Map resultHostLMSNotConfigured = [
        result : false,
        problems : "Host LMS integration not configured: Choose Host LMS in settings or deconfigure host LMS integration in settings."
    ];

  GrailsApplication grailsApplication;
  ProtocolAuditService protocolAuditService;
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
  SettingsService settingsService;
  StatisticsService statisticsService;

  public HostLMSActions getHostLMSActionsFor(String lms) {
    log.debug("HostLMSService::getHostLMSActionsFor(${lms})");
    HostLMSActions result = grailsApplication.mainContext."${lms}HostLMSService"

    if ( result == null ) {
      log.warn("Unable to locate HostLMSActions for ${lms}. Did you fail to configure the app_setting \"host_lms_integration\". Current options are aleph|alma|FOLIO|Koha|Millennium|Sierra|Symphony|Voyager|wms|manual|default|Polaris|Evergreen");
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
        def checkInMap = [:];
        INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails();
        def check_in_result = host_lms.checkInItem(settingsService, vol.temporaryItemBarcode, ncipLogDetails);
        protocolAuditService.save(request, ncipLogDetails);
        checkInMap.result = check_in_result;
        checkInMap.volume = vol;
        String message;
        if(check_in_result?.result == true) {
          if(check_in_result?.already_checked_in == true) {
            message = "NCIP CheckinItem call succeeded for item: ${vol.temporaryItemBarcode}. ${check_in_result.reason=='spoofed' ? '(No host LMS integration configured for check in item call)' : 'Host LMS integration: CheckinItem not performed because the item was already checked in.'}"
          } else {
            message = "NCIP CheckinItem call succeeded for item: ${vol.temporaryItemBarcode}. ${check_in_result.reason=='spoofed' ? '(No host LMS integration configured for check in item call)' : 'Host LMS integration: CheckinItem call succeeded.'}"
          }
          checkInMap.success = true;
          reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, message, null);
          def newVolStatus = check_in_result.reason=='spoofed' ? vol.lookupStatus('lms_check_in_(no_integration)') : vol.lookupStatus('completed')
          vol.status = newVolStatus
          vol.save(failOnError: true)
        } else {
          request.needsAttention=true;
          checkInMap.success = false;
          message = "Host LMS integration: NCIP CheckinItem call failed for item: ${vol.temporaryItemBarcode}. Review configuration and try again or deconfigure host LMS integration in settings. "+check_in_result.problems?.toString();
          reshareApplicationEventHandlerService.auditEntry(
            request,
            request.state,
            request.state,
            "Host LMS integration: NCIP CheckinItem call failed for item: ${vol.temporaryItemBarcode}. Review configuration and try again or deconfigure host LMS integration in settings. "+check_in_result.problems?.toString(),
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

    /**
     * looks up the patron using NCIP and records the messages are stored in the protocol audit table if enabled
     * @param request the request associated with the patron lookup
     * @param patronIdentifier the id of the patron to be looked up
     * @return a map containing the result of the lookup
     */
    public Map lookupPatron(PatronRequest request, String patronIdentifier) {
        Map patronDetails;
        HostLMSActions hostLMSActions = getHostLMSActions();
        if (hostLMSActions) {
            INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails();
            patronDetails = hostLMSActions.lookupPatron(settingsService, patronIdentifier, ncipLogDetails);
            protocolAuditService.save(request, ncipLogDetails);
        } else {
            patronDetails = resultHostLMSNotConfigured;
        }
        return(patronDetails);
    }

    /**
     * looks to see if the requested item is available using Z3950 and records the messages are stored in the protocol audit table if enabled
     * @param request the request this lookup is for
     * @param protocolType The protocol type it is for
     * @return The location holding this item where it is available or null if it has not been found or it is not available
     */
    public ItemLocation determineBestLocation(PatronRequest request, ProtocolType protocolType) {
        ItemLocation location = null;
        HostLMSActions hostLMSActions = getHostLMSActions();
        if (hostLMSActions) {
            IHoldingLogDetails holdingLogDetails = protocolAuditService.getHoldingLogDetails(protocolType);
            location = hostLMSActions.determineBestLocation(settingsService, request, holdingLogDetails);
            protocolAuditService.save(request, holdingLogDetails);
        }
        return(location);
    }

    /**
     * Checks out the specified item using NCIP and records the messages are stored in the protocol audit table if enabled
     * @param request the request this item is associated with
     * @param itemId the id of the item to be checked out
     * @param institutionalPatronIdValue the patron that the item should be checked out to
     * @return A Map containg the result of the checkout
     */
    public Map checkoutItem(PatronRequest request, String itemId, String institutionalPatronIdValue) {
        Map checkoutResult;
        HostLMSActions hostLMSActions = getHostLMSActions();
        if (hostLMSActions) {
            INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails();
            checkoutResult = hostLMSActions.checkoutItem(
                settingsService,
                request.hrid,
                itemId,
                institutionalPatronIdValue,
                ncipLogDetails
            );
            protocolAuditService.save(request, ncipLogDetails);
        } else {
            checkoutResult = resultHostLMSNotConfigured;
            request.needsAttention = true;
        }
        return(checkoutResult);
    }

    /**
     * Creates a temporary item in the local lms using NCIP and records the messages are stored in the protocol audit table if enabled
     * @param request the request triggering the accept item message
     * @param temporaryItemBarcode the id of the temporary item to be created
     * @param callNumber volume callNumber
     * @param requestedAction the action to be performed (no idea what actions can be performed)
     * @return a map containing the outcome of the accept item call
     */
    public Map acceptItem(PatronRequest request, String temporaryItemBarcode, String callNumber, String requestedAction) {
        Map acceptResult;
        HostLMSActions hostLMSActions = getHostLMSActions();
        if (hostLMSActions) {
        INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails();
            acceptResult = getHostLMSActions().acceptItem(
                settingsService,
                temporaryItemBarcode,
                request.hrid,
                request.patronIdentifier, // user_idA
                request.author, // author,
                request.title, // title,
                request.isbn, // isbn,
                callNumber,
                request.resolvedPickupLocation?.lmsLocationCode, // pickup_location,
                requestedAction, // requested_action
                ncipLogDetails
            );
            protocolAuditService.save(request, ncipLogDetails);
        } else {
            acceptResult = resultHostLMSNotConfigured;
            request.needsAttention = true;
        }
        return(acceptResult);
    }

    public Map requestItem(PatronRequest request, String pickupLocation,  String itemLocation, String bibliographicId,
            String patronId) {
        Map requestItemResult;
        HostLMSActions hostLMSActions = getHostLMSActions();
        if (hostLMSActions) {
            INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails();
            requestItemResult = getHostLMSActions().requestItem(
                settingsService,
                request.hrid,
                bibliographicId,
                patronId,
                pickupLocation,
                itemLocation,
                ncipLogDetails)
            protocolAuditService.save(request, ncipLogDetails);
        } else {
            requestItemResult = resultHostLMSNotConfigured;
            request.needsAttention = true;
        }
        return requestItemResult;
    }

    public Map cancelRequestItem(PatronRequest request, String requestId, String userId) {
        Map cancelRequestItemResult;
        HostLMSActions hostLMSActions = getHostLMSActions();
        if (hostLMSActions) {
            INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails();
            cancelRequestItemResult = getHostLMSActions().cancelRequestItem(
                    settingsService,
                    requestId,
                    userId,
                    ncipLogDetails);
            protocolAuditService.save(request, ncipLogDetails);
        } else {
            cancelRequestItemResult = resultHostLMSNotConfigured;
            request.needsAttention = true;
        }
        return cancelRequestItemResult;
    }

    Map deleteItem(PatronRequest request, String itemId) {
        Map deleteItemResult
        HostLMSActions hostLMSActions = getHostLMSActions()
        if (hostLMSActions) {
            INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails()
            deleteItemResult = getHostLMSActions().deleteItem(
                    settingsService,
                    itemId,
                    ncipLogDetails)
            protocolAuditService.save(request, ncipLogDetails)
        } else {
            deleteItemResult = resultHostLMSNotConfigured
            request.needsAttention = true
        }
        return deleteItemResult
    }

    public boolean isManualCancelRequestItem() {
        HostLMSActions hostLMSActions = getHostLMSActions();
        if (hostLMSActions) {
            return hostLMSActions.isManualCancelRequestItem();
        }
        return false;
    }

    Map createUserFiscalTransaction (PatronRequest request, String userId, String itemId) {
        Map createUserFiscalTransactionItemResult
        HostLMSActions hostLMSActions = getHostLMSActions()
        if (hostLMSActions) {
            INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails()
            createUserFiscalTransactionItemResult = getHostLMSActions().createUserFiscalTransaction(
                    settingsService,
                    userId,
                    itemId,
                    ncipLogDetails)
            protocolAuditService.save(request, ncipLogDetails)
        } else {
            createUserFiscalTransactionItemResult = resultHostLMSNotConfigured
            request.needsAttention = true
        }
        return createUserFiscalTransactionItemResult
    }
}
