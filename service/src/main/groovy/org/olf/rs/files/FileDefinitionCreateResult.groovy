package org.olf.rs.files

/**
 * Holds the outcome from attempting to create a file
 */
public class FileDefinitionCreateResult {
	/** file definition record that was created */
	public FileDefinition fileDefinition;

	/** The error that occurred if any */
	public String error;
}
