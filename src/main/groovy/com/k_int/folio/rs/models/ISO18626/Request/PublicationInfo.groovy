package com.k_int.folio.rs.models.ISO18626.Request

import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.PublicationType;

public class PublicationInfo {

	/** Name of the publisher */
	public String publisher;

	/** The publication type */
	public PublicationType publicationType;

	/** The date of publication */
	public String publicationDate;

	/** Where it was published */
	public String placeOfPublication;

	public PublicationInfo() {
	}
}
