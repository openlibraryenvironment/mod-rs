package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class RequestType extends ReferenceData {

	public RequestType(String code = null, boolean validated = false) {
		super(ReferenceTypes.REQUEST_TYPE, code, validated);
	}
}
