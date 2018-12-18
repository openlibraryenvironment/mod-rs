package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class RequestType extends ReferenceData {

	public RequestType() {
		this(null);
	}

	public RequestType(String code) {
		super(ReferenceTypes.REQUEST_TYPE, code);
	}
}
