package org.olf.rs.logging

import org.olf.rs.PatronRequest;
import org.olf.rs.ProtocolAudit;
import org.olf.rs.ProtocolMethod;
import org.olf.rs.ProtocolType;
import org.olf.rs.ReferenceDataService;
import org.olf.rs.SettingsService;
import org.olf.rs.referenceData.RefdataValueData;
import org.olf.rs.referenceData.SettingsData;

/**
 * Provides the necessary methods for interfacing with the ProtocolAudit table
 * @author Chas
 *
 */
public class ProtocolAuditService {

    private static String refDataYes = null;

    ReferenceDataService referenceDataService;
    SettingsService settingsService;

    /**
     * Given the protocol type determines the class for recording  mechanism for
     * @param protocolType
     * @return
     */
    public IHoldingLogDetails getHoldingLogDetails(ProtocolType protocolType) {
        String settingKey = null;

        // We first need to lookup the settings to see if we are recording information for this protocol using IHoldingsLogDetails
        switch (protocolType) {
            case protocolType.Z3950_REQUESTER:
                settingKey = SettingsData.SETTING_LOGGING_Z3950_REQUESTER;
                break;

            case protocolType.Z3950_RESPONDER:
                settingKey = SettingsData.SETTING_LOGGING_Z3950_RESPONDER;
                break;

            default:
                break;
        }

        // Now we can allocate an appropriate object
        return(((settingKey != null) && settingsService.hasSettingValue(settingKey, getRefDataYes())) ?
                new HoldingLogDetails(protocolType, ProtocolMethod.GET) : // Logging is enabled
                new DoNothingHoldingLogDetails()                          // Logging is not enabled
        );
    }

    /**
     * Associates the logging details with request
     * @param patronRequest The request that the log details need to be associated with
     * @param holdingLogDetails The logging details
     */
    public void save(PatronRequest patronRequest, IHoldingLogDetails holdingLogDetails) {
        // Do we have anything to save
        String logDetails = holdingLogDetails.toString();
        if (logDetails != null) {
            // We have some details to save
            ProtocolAudit protocolAudit = new ProtocolAudit();

            protocolAudit.protocolType = holdingLogDetails.getProtocolType();
            protocolAudit.protocolMethod = holdingLogDetails.getProtocolMethod();
            protocolAudit.url = holdingLogDetails.getURL();
            protocolAudit.responseBody = logDetails;
            protocolAudit.duration = holdingLogDetails.duration();
            patronRequest.addToProtocolAudit(protocolAudit);
        }
    }

    /**
     * Looks up the value for the value of yes for the Yes / No Category
     * @return The value for for Yes
     */
    private String getRefDataYes() {
        if (refDataYes == null) {
            refDataYes = referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_YES).value;
        }
        return(refDataYes);
    }
}
