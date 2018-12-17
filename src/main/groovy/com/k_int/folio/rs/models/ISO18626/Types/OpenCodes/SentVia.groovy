package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class SentVia extends ReferenceData {

	public SentVia(String code = null, boolean validated = false) {
		super(ReferenceTypes.SENT_VIA, code, validated);
	}
}
