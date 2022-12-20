package org.olf.rs.reporting

import org.olf.rs.PredefinedId;
import org.olf.rs.files.FileDefinition;

import grails.gorm.MultiTenant;

/**
 * Holds the details for a report
 */

class Report implements MultiTenant<Report> {

    private static final String NAMESPACE_REPORT = 'report';

    /** The id for the report */
    String id;

    /** The date the report was created - maintained by the framework */
    Date dateCreated;

    /** The date the report was last updated - maintained by the framework */
    Date lastUpdated;

    /** The name to be presented to the users for this report */
    String name;

    /** The description for this report */
    String description;

    /** Is this a single or multiple record report */
    boolean isSingleRecord;

    /** The domain the report belongs to */
    String domain;

    /** The actual report */
    FileDefinition fileDefinition;

    /** The content type produced by the report */
    String contentType;

    /** The name to be given to the generated report */
    String filename;

    static constraints = {
           dateCreated (nullable: true)
           lastUpdated (nullable: true)
                  name (nullable: false, blank: false)
           description (nullable: false, blank: false)
                domain (nullable: false, blank: false)
        fileDefinition (nullable: false, blank: false)
           contentType (nullable: false, blank: false)
              filename (nullable: false, blank: false)
    }

    static mapping = {
                    id column : 'r_id', generator: 'uuid2', length :36
               version column : 'r_version'
           dateCreated column : 'r_date_created'
           lastUpdated column : 'r_last_updated'
                  name column : 'r_name', length: 64
           description column : 'r_description', length : 2000
        isSingleRecord column : 'r_is_single_record'
                domain column : 'r_domain', length : 64
        fileDefinition column : 'r_file_definition'
           contentType column : 'r_content_type', length : 64, defaultValue : "application/pdf"
              filename column : 'r_filename', length : 64, defaultValue : "report.pdf"
    }

    /**
     * Lookup the report by its id
     * @param reportId The id id of the request you are interested in
     * @return The report corresponding to this id or null if it does not exist
     */
    public static Report lookup(String reportId) {
        Report report = null;
        if (reportId != null) {
            report = get(reportId);
        }
        return(report);
    }

    /**
     * Look for a report given the predefined id
     * @param predefinedId The predefined id for which we want the report for
     * @return The report that corresponds to the predefined id or null if it does not exist
     */
    public static Report lookupPredefinedId(String predefinedId) {
        Report report = null;

        // Have we been passed a predefined id
        if (predefinedId != null) {
            // Lookup to see if the predefined id is mapped to a report id
            String reportId = PredefinedId.lookupReferenceId(NAMESPACE_REPORT, predefinedId);

            // Did we find a report id
            if (reportId != null) {
                // We managed to find a reportId, so get hold of it
                report = lookup(reportId);
            }
        }
        return(report);
    }
}
