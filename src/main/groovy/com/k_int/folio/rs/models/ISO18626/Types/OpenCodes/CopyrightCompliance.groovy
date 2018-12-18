package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes;

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class CopyrightCompliance extends ReferenceData {

	public CopyrightCompliance() {
		this(null);
	}

	public CopyrightCompliance(String code) {
		super(ReferenceTypes.COPYRIGHT_COMPLIANCE, code);
	}
}
