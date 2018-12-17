package com.k_int.folio.rs.models.ISO18626.Types

import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.BibliographicRecordIdentifierCode

public class BibliographicRecordId {
	/** The type of record identifier */
	BibliographicRecordIdentifierCode bibliographicRecordIdentifierCode;

	/** The value associated with the type of record identifier */
	String bibliographicRecordIdentifier;

	public BibliographicRecordId(BibliographicRecordIdentifierCode bibliographicRecordIdentifierCode = null, String bibliographicRecordIdentifier = null) {
		this.bibliographicRecordIdentifierCode = bibliographicRecordIdentifierCode;
		this.bibliographicRecordIdentifier = bibliographicRecordIdentifier;
	}
}
