package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class ReasonForMessage extends ReferenceData {

	public ReasonForMessage(String code = null, boolean validated = false) {
		super(ReferenceTypes.REASON_FOR_MESSAGE, code, validated);
	}
}
