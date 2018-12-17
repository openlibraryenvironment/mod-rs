package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class ServiceLevel extends ReferenceData {

	public ServiceLevel(String code = null, boolean validated = false) {
		super(ReferenceTypes.SERVICE_LEVEL, code, validated);
	}
}
