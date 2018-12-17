package com.k_int.folio.rs.models.ISO18626.Types

import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.BibliographicItemIdentifierCode

public class BibliographicItemId {

	BibliographicItemIdentifierCode bibliographicItemIdentifierCode;
	String bibliographicItemIdentifier;
	
	public BibliographicItemId(BibliographicItemIdentifierCode bibliographicItemIdentifierCode = null, String bibliographicItemIdentifier = null) {
		this.bibliographicItemIdentifierCode = bibliographicItemIdentifierCode;
		this.bibliographicItemIdentifier = bibliographicItemIdentifier;
	}
}
