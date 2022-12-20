package org.olf.rs.files

/**
 * Holds the outcome from trying to fetch a file
 */
public class FileFetchResult {
	/** The stream that contains the uploaded file */
	public InputStream inputStream;

    /** The content type of the file */
    public String contentType;

    /** The filename for the report */
    public String filename;

	/** The error that occurred if any */
	public String error;
}
