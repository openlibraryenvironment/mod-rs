package org.olf.rs.files

import org.olf.rs.reporting.Report;

/**
 * Holds the outcome from attempting to create a file
 */
public class ReportCreateUpdateResult {
	/** Report record that was created */
	public Report report;

	/** The error that occurred if any */
	public String error;
}
