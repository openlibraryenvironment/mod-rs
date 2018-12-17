package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class Status extends ReferenceData {

	public Status(String code = null, boolean validated = false) {
		super(ReferenceTypes.STATUS, code, validated);
	}
}
