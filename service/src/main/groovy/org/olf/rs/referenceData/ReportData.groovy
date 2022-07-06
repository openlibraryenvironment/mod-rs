package org.olf.rs.referenceData

import java.nio.charset.StandardCharsets;

import org.olf.rs.reports.Report;

import groovy.util.logging.Slf4j

@Slf4j
public class ReportData {

    private static final String DOMAIN_PATRON_REQUEST = 'PatronRequest';

    private static final String ID_PATRON_REQUEST_PULL_SLIP_1 = DOMAIN_PATRON_REQUEST + '.PullSlip.1';

	public void load() {
		log.info("Adding report data to the database");

        // Pull Slip Report
		Report.ensure(
            ID_PATRON_REQUEST_PULL_SLIP_1,
            'Pull Sllp',
            'Generates where the item is pulled off the shelf and where it needs to be returned',
            true,
            DOMAIN_PATRON_REQUEST,
            'PullSlip.jrxml',
            loadReportFromResource('reports/patronRequests/PullSlip.jrxml')
        );
	}

    private String loadReportFromResource(String resourcePath) {
        String reportDefinition = null;
        try {
            URL resource = this.class.classLoader.getResource(resourcePath);
            if (resource == null) {
                log.error('Unable to find resource: ' + resourcePath);
            } else {
                InputStream stream = resource.openStream();
                reportDefinition = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                stream.close();
            }
        } catch (Exception e) {
            log.errorEnabled("Exception thrown while loading report definition for " + resourcePath, e);
        }

        return(reportDefinition);
    }

	public static void loadAll() {
		(new ReportData()).load();
	}
}
