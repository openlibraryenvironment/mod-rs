package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class BibliographicRecordIdentifierCode extends ReferenceData {

	public BibliographicRecordIdentifierCode() {
		this(null);
	}

	public BibliographicRecordIdentifierCode(String code) {
		super(ReferenceTypes.BIBLIOGRAPHIC_RECORD_IDENTIFIER_CODE, code);
	}
}
