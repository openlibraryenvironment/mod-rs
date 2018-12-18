package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class AgencyIdType extends ReferenceData {

	public AgencyIdType() {
		this(null);
	}

	public AgencyIdType(String code) {
		super(ReferenceTypes.AGENCY_ID_TYPE, code);
	}
}
