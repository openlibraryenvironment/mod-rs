package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class RequestSubType extends ReferenceData {

	public RequestSubType(String code = null, boolean validated = false) {
		super(ReferenceTypes.REQUEST_SUB_TYPE, code, validated);
	}
}
