package org.olf.rs.reporting;

import org.olf.rs.files.FileDefinitionCreateResult;
import org.olf.rs.files.FileFetchResult;
import org.olf.rs.files.FileService;
import org.olf.rs.files.FileType;
import org.olf.rs.files.ReportCreateUpdateResult;
import org.springframework.web.multipart.MultipartFile

import groovy.util.logging.Slf4j

/**
 * Provides the necessary interfaces for reports
 * @author Chas
 *
 */
@Slf4j
public class ReportService {

    /** The service that handles files for us */
    FileService fileService;

    /** The service that interfaces into jasper reports */
    JasperReportService jasperReportService;

    public FileFetchResult generateReport(String tenant, String reportId, List identifiers = null, String fallbackReportResource = null) {
        FileFetchResult result = new FileFetchResult();

        // tThe database schema we are looking at
        String schema = tenant + "_mod_rs";

        if (reportId || fallbackReportResource) {
            // Get hold of the report if a report id is specified
            Report report = Report.lookup(reportId);

            try {
                // Execute the jasper report
                result.inputStream = jasperReportService.executeReport(report?.fileDefinition, schema, identifiers, fallbackReportResource);

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
                            // Now we havn't so we are creating a new record
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
}
