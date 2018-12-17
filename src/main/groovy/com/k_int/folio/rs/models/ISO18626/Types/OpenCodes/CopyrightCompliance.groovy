package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes;

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class CopyrightCompliance extends ReferenceData {

	public CopyrightCompliance(String code = null, boolean validated = false) {
		super(ReferenceTypes.BIBLIOGRAPHIC_COPYRIGHT_COMPLIANCE, code, validated);
	}
}
