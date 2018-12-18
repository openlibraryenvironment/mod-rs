package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class PatronType extends ReferenceData {

	public PatronType() {
		this(null);
	}

	public PatronType(String code) {
		super(ReferenceTypes.PATRON_TYPE, code);
	}
}
