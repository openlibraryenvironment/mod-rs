package org.olf.rs.reporting;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.olf.rs.OkapiSettingsService;
import org.olf.rs.files.FileDefinition;
import org.olf.rs.files.FileFetchResult;
import org.olf.rs.files.FileService;

import groovy.util.logging.Slf4j;
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
    public static final String PARAMETER_IDS              = "ids";
    public static final String PARAMETER_IMAGE            = "image";
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
    FileService fileService;
    OkapiSettingsService okapiSettingsService;

    // Very simple form of caching, potentially use EHCache
    private static final Map cachedFormats = [ : ];
    private static final Map cachedReports = [ : ];

    /**
     * loads a report from the supplied report path
     * @param resourcePath The resource path to load the file from
     * @return A fileFetchResult that contains the input stream of the resource or an error if one occurred
     */
    public FileFetchResult loadReportFromResource(String resourcePath) {
        FileFetchResult result = new FileFetchResult();
        try {
            // Obtain the URL to the resource
            URL resource = this.class.classLoader.getResource(resourcePath);
            if (resource == null) {
                result.error = "Unable to find resource: " + resourcePath;
            } else {
                // Now actually read the resource into a string
                result.inputStream = resource.openStream();
            }
        } catch (Exception e) {
            String message = "Exception thrown while loading report definition for " + resourcePath;
            result.error = message + ", exeption: " + e.getMessage();
            log.errorEnabled(message, e);

        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Generates a report using Jasper Reports
     * @param fileDefinition The file definition that defines the report to be run
     * @param schema The database schema that the report will be run against
     * @param outputStream Where the report should be output to
     * @param idsForReport The ids to pass into the report
     * @param imageInputStream The input stream for the image
     * @param fallbackReportResource If we do not have a report using the file definition, the is where we obtain it from the resources
     * @return The input stream for the generated report or null if the report failed
     */
    public InputStream executeReport(
        FileDefinition fileDefinition,
        String schema,
        List idsForReport = null,
        InputStream imageInputStream = null,
        String fallbackReportResource = null
    ) {
        InputStream result = null;

        // If you are having issues with fonts, take a look at
        // https://community.jaspersoft.com/wiki/custom-font-font-extension
        Connection connection = dataSource.getConnection();
        JasperPrint jasperPrint;
        try {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(PARAMETER_IDS, idsForReport);
            parameters.put(PARAMETER_SCHEMA, schema);
            parameters.put(PARAMETER_IMAGE, imageInputStream);
            AddDateTimeParameters(schema, parameters);

            // Compile the report
            JasperReport jasperReport = getReport(fileDefinition, fallbackReportResource);

            // Execute the report
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
        } catch(Exception e) {
            log.error("Exception thrown while generating a report", e);
            return result;
        } finally {
            // Not forgetting to close the connection
            connection.close();

            // If we have a logo input stream the close it
            if (imageInputStream != null) {
                imageInputStream.close();
            }
        }

        try {
            // Now output the report
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            result = new ByteArrayInputStream(outputStream.toByteArray());
            outputStream.close();
        } catch(Exception e) {
            log.error("Exception thrown while generating a report PDF", e);
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Returns a compiled report for the given id
     * @param fileDefinition The file definition that defines the report to be run
     * @param fallbackReportResource If we do not have a report using the file definition, the is where we obtain it from the resources
     * @return The report or null if it does not exist or compile
     */
    private JasperReport getReport(FileDefinition fileDefinition, String fallbackReportResource) {
        String cacheKey = ((fileDefinition == null) ? fallbackReportResource : fileDefinition.id);

        // Lookup the cache to see if it has been previously compiled
        JasperReport report = cachedReports[cacheKey];

        // Did we find it
        if (report == null) {
            FileFetchResult fetchResult;

            // No we did not
            try {
                // look to see if we can get hold of the report
                if (fileDefinition == null) {
                    // we need to obtain it from the resources
                    fetchResult = loadReportFromResource(fallbackReportResource);
                } else {
                    // It is stored in the database or the file system
                    fetchResult = fileService.fetch(fileDefinition);
                }

                // Did we find the input stream
                if (fetchResult.inputStream == null) {
                    log.error("Unable to find report with file id: " + cacheKey + ", Error: " + fetchResult.error);
                } else {
                    // Compile the report
                    report = JasperCompileManager.compileReport(fetchResult.inputStream);

                    // Did it compile
                    if (report == null) {
                        // Need to check, not sure if it returns null or throws an exception when the report has an error
                        log.error("Failed to compile report with file if: " + cacheKey);
                    } else {
                        // Add it to the cache
                        cachedReports[cacheKey] = report;
                    }
                }
            } catch (Exception e) {
                log.error("Exception thrown while compiling report with file id" + cacheKey, e);
            } finally {
                // If we have an input stream ensure it is closed
                if (fetchResult?.inputStream != null) {
                    // We do so lets close it
                    fetchResult.inputStream.close();
                }
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
            Map localeSettings = okapiSettingsService.getLocaleSettings();
            if (localeSettings == null) {
                // Use the defaults, but do not add it to the cache
                log.warn("Failed to retrieve locale settings, falling back on default locale for pull slip");
                schemaLocaleSettings = DefaultLocaleSettings;
            } else {
                // We managed to find the locale settings
                Locale locale = Locale.forLanguageTag(localeSettings.locale);
                String datePattern =  patternFromDateFormat(DateFormat.getDateInstance(DateFormat.SHORT, locale));
                String timePattern =  patternFromDateFormat(DateFormat.getTimeInstance(DateFormat.SHORT, locale));
                String timeZone = localeSettings.timezone;


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
