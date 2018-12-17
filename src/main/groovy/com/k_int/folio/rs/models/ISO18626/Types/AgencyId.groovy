package com.k_int.folio.rs.models.ISO18626.Types

import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.AgencyIdType;

public class AgencyId {
	/** The type of agency */
	AgencyIdType agencyIdType;

	/** The value associated with the agency that represents the agency */
	String agencyIdValue;

	public AgencyId(AgencyIdType agencyIdType = null, String agencyIdValue = null) {
		this.agencyIdType = agencyIdType;
		this.agencyIdValue = agencyIdValue;
	}
}
