package org.olf.rs.logging

import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest;
import org.olf.rs.ProtocolAudit;
import org.olf.rs.ProtocolMethod;
import org.olf.rs.ProtocolType;
import org.olf.rs.ReferenceDataService;
import org.olf.rs.SettingsService;
import org.olf.rs.referenceData.RefdataValueData;
import org.olf.rs.referenceData.SettingsData;

import groovyx.net.http.URIBuilder
import org.springframework.dao.OptimisticLockingFailureException

/**
 * Provides the necessary methods for interfacing with the ProtocolAudit table
 * @author Chas
 *
 */
public class ProtocolAuditService {

    private static final String OBSCURED = "xxx";
    private static final List queryKeysToObscure = [
        "apikey",
        "user",
        "password"
    ];
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
     * Gets hold of the appropriate NCIP logging object depending on whether logging is enabled or not
     * @return An INcipLogDetails object
     */
    public INcipLogDetails getNcipLogDetails() {
        // Allocate an appropriate object depending on whether auditing is enabled
        return(settingsService.hasSettingValue(SettingsData.SETTING_LOGGING_NCIP, getRefDataYes()) ?
                new NcipLogDetails() :        // Logging is enabled
                new DoNothingNcipLogDetails() // Logging is not enabled
        );
    }

    /**
     * Allocates an object that implements the IIso18626LogDetails interface depending on whether auditing is enabled or not
     * @return An IIso18626LogDetails object
     */
    public IIso18626LogDetails getIso18626LogDetails() {
        // Allocate an appropriate object depending on whether auditing is enabled
        return(settingsService.hasSettingValue(SettingsData.SETTING_LOGGING_ISO18626, getRefDataYes()) ?
                new Iso18626LogDetails() :        // Logging is enabled
                new DoNothingIso18626LogDetails() // Logging is not enabled
        );
    }

    void save(String patronRequestId, IBaseAuditDetails baseAuditDetails) {
        PatronRequest request = PatronRequest.get(patronRequestId)
        if (request) {
            try {
                save(request, baseAuditDetails)
            } catch (OptimisticLockingFailureException olfe) {
                log.warn("Optimistic Locking Failure: ${olfe.getLocalizedMessage()}");
            }
        }
    }

    @Subscriber("ProtocolAuditService.saveSubscriber")
    void saveSubscriber(Serializable tenant, String patronRequestId, IBaseAuditDetails baseAuditDetails){
        Tenants.withId(tenant) {
            save(patronRequestId, baseAuditDetails)
        }
    }

    /**
     * Associates the audit details with request
     * @param patronRequest The request that the audit details need to be associated with
     * @param baseAuditDetails The audit details
     */
    public void save(PatronRequest patronRequest, IBaseAuditDetails baseAuditDetails) {
        // Have we been supplied a request
        if (patronRequest != null) {
            // Do we have anything to save
            String responseBody = baseAuditDetails.getResponseBody();
            if (responseBody != null) {
                // We have some details to save
                ProtocolAudit protocolAudit = new ProtocolAudit();

                // Populate the protocol audit
                protocolAudit.protocolType = baseAuditDetails.getProtocolType();
                protocolAudit.protocolMethod = baseAuditDetails.getProtocolMethod();
                protocolAudit.url = removePrivateDataFromURI(baseAuditDetails.getURL());
                protocolAudit.requestBody = baseAuditDetails.getRequestBody();
                protocolAudit.responseStatus = baseAuditDetails.getResponseStatus()?.take(30); // truncate to column size
                protocolAudit.responseBody = responseBody;
                protocolAudit.duration = baseAuditDetails.duration();
                protocolAudit.patronRequest = patronRequest
                protocolAudit.save(flush: true, failOnError: true);
                patronRequest.addToProtocolAudit(protocolAudit);
            }
        }
    }

    /**
     * Obfuscates certain query parameters so that usernames / passwords / apikeys are not recorded
     * @param uri The uri that may need query parameters obfuscating
     * @return The uri with parameters obfuscated
     */
    private String removePrivateDataFromURI(String uri) {
        // We need to manipulate the query string to remove any passwords, apikeys or secrets
        URIBuilder uriBuilder = new URIBuilder(uri);
        Map queryParameters = uriBuilder.getQuery();
        if (queryParameters) {
            queryKeysToObscure.each { String parameter ->
                if (queryParameters[parameter]) {
                    // it is set, so reset to xxx
                    queryParameters.put(parameter, OBSCURED);
                }
            }

            // Now replace the query parameters
            uriBuilder.setQuery(queryParameters);
        }

        if (uriBuilder.getPath() != null) {
            uriBuilder.setPath(uriBuilder.getPath().toLowerCase().replaceAll("ncip/ey.*", "ncip"))
        }

        // Return the actual url that we accessed
        return(uriBuilder.toString());
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
