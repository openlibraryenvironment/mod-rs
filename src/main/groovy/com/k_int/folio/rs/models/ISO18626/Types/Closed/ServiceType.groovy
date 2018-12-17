package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class ServiceType extends ReferenceData {

	public ServiceType(String code = null, boolean validated = false) {
		super(ReferenceTypes.SERVICE_TYPE, code, validated);
	}
}
