package org.olf.rs.reporting;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.olf.rs.OkapiSettingsService;
import org.olf.rs.reports.Report;

import groovy.json.JsonSlurper;
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Provides an interface into jasper reports for generating reports
 * @author Chas
 *
 */
@Slf4j
public class JasperReportService {

    // The parameter names passed into the reports
    public static final String PARAMETER_DATE_FORMAT      = "dateFormat";
    public static final String PARAMETER_DATE_TIME_FORMAT = "dateTimeFormat";
    public static final String PARAMETER_ID               = "id";
    public static final String PARAMETER_SCHEMA           = "schema";
    public static final String PARAMETER_TIME_FORMAT      = "timeFormat";
    public static final String PARAMETER_TIME_ZONE        = "timeZone";

    private static Map DefaultLocaleSettings = [
        (PARAMETER_DATE_FORMAT) : "M/d/yy",
        (PARAMETER_DATE_TIME_FORMAT) : "d/M/yy h:mm a",
        (PARAMETER_TIME_FORMAT) : "h:mm a",
        (PARAMETER_TIME_ZONE) : "Europe/London"
    ];

    DataSource dataSource;
    OkapiSettingsService okapiSettingsService;

    // Very simple form of caching, potentially use EHCache
    private static final Map cachedFormats = [ : ];
    private static final Map cachedReports = [ : ];

    /**
     * Generates a report using Jasper Reports
     * @param reportId The id of the report that is to be run
     * @param schema The database schema that the report will be run against
     * @param outputStream Where the report should be output to
     * @param idForReport If the report requires an id, this is the id it will be supplied
     */
    public void executeReport(String reportId, String schema, OutputStream outputStream, String idForReport = null) {
        Connection connection = dataSource.getConnection();
        try{
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(PARAMETER_ID, idForReport);
            parameters.put(PARAMETER_SCHEMA, schema);
            AddDateTimeParameters(schema, parameters);

            // Compile the report
            JasperReport jasperReport = getReport(reportId);

            // Execute the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            // Now output the report
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        } catch(Exception e) {
            log.error("Exception thrown while generating a report", e);
        } finally {
            // Not forgetting to close the connection
            connection.close();
        }
    }

    /**
     * Returns a compiled report for the given id
     * @param id The id of the report
     * @return The report or null if it does not exist or compile
     */
    private JasperReport getReport(String id) {
        // Lookup the cache to see if it has been previously compiled
        JasperReport report = cachedReports[id];

        // Did we find it
        if (report == null) {
            // No we did not
            try {
                // look in the database to get the report
                Report dbReport = Report.lookup(id);

                if (dbReport == null) {
                    log.error("Unable to find report: " + id);
                } else {
                    // Compile the report
                    InputStream reportInputStream = new ByteArrayInputStream(dbReport.reportDefinition.getBytes());
                    report = JasperCompileManager.compileReport(reportInputStream);
                    reportInputStream.close();

                    // Did it compile
                    if (report == null) {
                        // Need to check, not sure if it returns null or throws an exception when the report has an error
                        log.error("Failed to compile report: " + id);
                    } else {
                        // Add it to the cache
                        cachedReports[id] = report;
                    }
                }
            } catch (Exception e) {
                log.error("Exception thrown while compiling report " + id, e);
            }
        }

        // Return the compiled report to the caller
        return(report);
    }

    /**
     * Adds the locale parameters to be supplied to the report
     * @param schema The schema the report is to be run against
     * @param parameters The parameters that are to be supplied to the report that the locale parameters will be added to
     */
    private void AddDateTimeParameters(String schema, Map<String, Object> parameters) {
        Map schemaLocaleSettings = cachedFormats[schema];

        // Do we need to lookup the locale settings
        if (schemaLocaleSettings == null) {
            JsonSlurper jsonSlurper = new JsonSlurper();
            Map localeSettings = okapiSettingsService.getSetting('localeSettings');
            if (localeSettings == null) {
                // Use the defaults, but do not add it to the cache
                schemaLocaleSettings = DefaultLocaleSettings;
            } else {
                // We managed to find the locale settings
                def tenantLocale = jsonSlurper.parseText(localeSettings.value);
                Locale locale = Locale.forLanguageTag(tenantLocale?.locale);
                String datePattern =  patternFromDateFormat(DateFormat.getDateInstance(DateFormat.SHORT, locale));
                String timePattern =  patternFromDateFormat(DateFormat.getTimeInstance(DateFormat.SHORT, locale));
                String timeZone = tenantLocale?.timezone;


                schemaLocaleSettings = [
                    (PARAMETER_DATE_FORMAT) : datePattern,
                    (PARAMETER_TIME_FORMAT) : timePattern,
                    (PARAMETER_DATE_TIME_FORMAT) : (datePattern + " " + timePattern),
                    (PARAMETER_TIME_ZONE) : timeZone
                ];

                // Now add it to the cache
                cachedFormats[schema] = schemaLocaleSettings;
            }
        }

        // Now we can set the parameters
        parameters.putAll(schemaLocaleSettings);
    }

    /**
     * Obtains the pattern for displaying a date / time from the dateFormat object
     * @param dateFormat The DateFormat object that contains the pattern we are interested in
     * @return The pattern from the DateFormat object or null if we failed to obtain one
     */
    private String patternFromDateFormat(DateFormat dateFormat) {
        String pattern = null;

        // Ensure we have been passed a SimpleDateFormat
        if (dateFormat instanceof SimpleDateFormat) {
            // That is good, now we can use toPattern
            pattern = ((SimpleDateFormat)dateFormat).toPattern();
        }
        return(pattern);
    }
}
