package org.olf.rs.reporting;

import org.olf.rs.SettingsService;
import org.olf.rs.files.FileDefinitionCreateResult;
import org.olf.rs.files.FileFetchResult;
import org.olf.rs.files.FileService;
import org.olf.rs.files.FileType;
import org.olf.rs.files.ReportCreateUpdateResult;
import org.olf.rs.referenceData.SettingsData;
import org.springframework.web.multipart.MultipartFile;

import groovy.util.logging.Slf4j

/**
 * Provides the necessary interfaces for reports
 * @author Chas
 *
 */
@Slf4j
public class ReportService {

    static public final String pullSlipDefaultReport = "reports/patronRequests/PullSlip.jrxml";

    /** The service that handles files for us */
    FileService fileService;

    /** The service that interfaces into jasper reports */
    JasperReportService jasperReportService;

    /** The service that interogates the settings */
    SettingsService settingsService;

    /**
     * Generates the specified report
     * @param tenant The tenant the report is being against, this will be supplied as a parameter to the report
     * @param reportId The id of the report to be executed
     * @param identifiers The identifiers to be passed to the report
     * @param fallbackReportResource If a report id is not valid, then if this parameter is specified we will look in the resources with the supplies path to see if we can find the report there
     * @param imageId The id to use for the image
     * @return A FileFetchResult object that will either give an error or an InputStream containing the generated report
     */
    public FileFetchResult generateReport(String tenant, String reportId, List identifiers = null, String imageId = null, String fallbackReportResource = null) {
        FileFetchResult result = new FileFetchResult();

        // tThe database schema we are looking at
        String schema = tenant + "_mod_rs";

        if (reportId || fallbackReportResource) {
            // Get hold of the report if a report id is specified
            Report report = Report.lookup(reportId);

            try {
                // Get hold of the logo if we have one
                InputStream logoInputStream = getImage(imageId);

                log.debug("Generating report for identifiers ${identifiers}");

                // Execute the jasper report
                result.inputStream = jasperReportService.executeReport(report?.fileDefinition, schema, identifiers, logoInputStream, fallbackReportResource);

                // Get hold of the content type and filename for the generated report
                if (report) {
                    // Take the filename and content type from the uploaded report file
                    result.filename = report.filename;
                    result.contentType = report.contentType;
                } else {
                    // We default them
                    result.filename = "report.pdf";
                    result.contentType = "application/pdf";
                }
            } catch (Exception e) {
                log.error("Exception thrown generating report", e);
            }
        } else {
            result.error = "No report identifier supplied or a full back resource identifier";
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Creates or updates a report object
     * @param name The name of the report
     * @param description A brief description of the report
     * @param domain The domain the report belongs to
     * @param isSingleRecord Is the report just tahe a single identifier or can it cope with multiple (multiple also implies single)
     * @param contentType The content type of the the generated report
     * @param filename The filename that the generated report will be called
     * @param multipartFile The uploaded file
     * @param id If we are updating an existing report, this is the id of the report
     * @return A ReportCreateResult object that contains the report object or an error if it failed to create or update
     */
    public ReportCreateUpdateResult createUpdate(
        String name,
        String description,
        String domain,
        boolean isSingleRecord,
        String contentType,
        String filename,
        MultipartFile multipartFile,
        String id = null
    ) {
        ReportCreateUpdateResult result = new ReportCreateUpdateResult();

        // Cannot do anything without the parameters
        if (name && description && domain && contentType && filename && (multipartFile != null)) {
            Report.withTransaction { tstatus ->
                Report report = ((id == null) ? null : Report.lookup(id));

                // Do we have a report object for when it is an update
                if ((report == null) && (id != null)) {
                    // No we do not
                    result.error = "Unable to find report eith id: " + id;
                } else {
                    // First of all let us attempt to create the file
                    FileDefinitionCreateResult fileCreationResult = fileService.create(FileType.REPORT_DEFINITION, description, multipartFile);

                    // Did we manage to create it
                    if (fileCreationResult.fileDefinition == null) {
                        // Failed to create the file for some reason
                        result.error = fileCreationResult.error;
                    } else {
                        // File has been created, so do we have a report object
                        if (report == null) {
                            // No we havn't so we are creating a new record
                            report = new Report();
                        }

                        // Now just update all the fields on the report
                        report.name = name;
                        report.description = description;
                        report.isSingleRecord = isSingleRecord;
                        report.domain = domain;
                        report.fileDefinition = fileCreationResult.fileDefinition;
                        report.contentType = contentType;
                        report.filename = filename;

                        // Save any changes
                        report.save(flush:true, failOnError:true);

                        // we need to return the report
                        result.report = report;
                    }
                }
            }
        } else {
            result = new FileDefinitionCreateResult();
            result.error = "Not all parameters have been supplied or they are empty";
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Retrieves the report id that has been configured that generates the pull slip
     * @return The report id if one has been configured or null which means use the default
     */
    public String getPullSlipReportId() {
        return(settingsService.getSettingValue(SettingsData.SETTING_PULL_SLIP_REPORT_ID));
    }

    /**
     * Retrieves the maximum number of requests that can be contained on 1 pull slip
     * @return The maximum number of requests that can be printed on a pull slip
     */
    public int getMaxRequestsInPullSlip() {
        return(settingsService.getSettingAsInt(SettingsData.SETTING_PULL_SLIP_MAX_ITEMS, 500, false));
    }

    /**
     * Retrieves the maximum number of requests that can be contained on 1 pull slip for a manually generated batch
     * @return The maximum number of requests that can be printed on a pull slip for a manually generated batch
     */
    public int getMaxRequestsInPullSlipManual() {
        return(settingsService.getSettingAsInt(SettingsData.SETTING_PULL_SLIP_MAX_ITEMS_MANUAL, 500, false));
    }

    /**
     * Retrieves the id of the logo to use for the image on the picklist
     * @return the Id for the logo or null if no logo has not been specified
     */
    public String getPullSlipLogoId() {
        // Get hold of the id for the logo
        return(settingsService.getSettingValue(SettingsData.SETTING_PULL_SLIP_LOGO_ID));
    }

    /**
     * Retrieves the image for the supplied id
     * @return the InputStream for the image
     */
    public InputStream getImage(String imageId) {
        // The inputStream for the logs
        InputStream imageInputStream = null;

        // Do we have an id for the logo
        if (imageId != null) {
            imageInputStream = fileService.fetch(imageId).inputStream;
        }

        // Return the input stream to the caller
        return(imageInputStream);
    }
}
