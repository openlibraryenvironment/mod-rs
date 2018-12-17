package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class ReasonUnfilled extends ReferenceData {

	public ReasonUnfilled(String code = null, boolean validated = false) {
		super(ReferenceTypes.REASON_UNFILLED, code, validated);
	}
}
