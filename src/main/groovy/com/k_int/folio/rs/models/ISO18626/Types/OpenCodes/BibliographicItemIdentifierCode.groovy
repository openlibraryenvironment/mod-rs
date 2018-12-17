package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class BibliographicItemIdentifierCode extends ReferenceData {

	public BibliographicItemIdentifierCode(String code = null, boolean validated = false) {
		super(ReferenceTypes.BIBLIOGRAPHIC_ITEM_IDENTIFIER_CODE, code, validated);
	}
}
