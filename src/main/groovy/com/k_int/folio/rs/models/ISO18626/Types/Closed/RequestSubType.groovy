package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class RequestSubType extends ReferenceData {

	public RequestSubType() {
		this(null);
	}

	public RequestSubType(String code) {
		super(ReferenceTypes.REQUEST_SUB_TYPE, code);
	}
}
