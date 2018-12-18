package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class Format extends ReferenceData {

	public Format() {
		this(null);
	}

	public Format(String code) {
		super(ReferenceTypes.FORMAT, code);
	}
}
