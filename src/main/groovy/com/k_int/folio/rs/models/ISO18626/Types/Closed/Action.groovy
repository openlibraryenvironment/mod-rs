package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class Action extends ReferenceData {

	public Action(String code = null, boolean validated = false) {
		super(ReferenceTypes.ACTION, code, validated);
	}
}
