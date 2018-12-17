package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

import com.k_int.folio.rs.models.ISO18626.Types.AgencyId;
import com.k_int.folio.rs.models.ISO18626.Types.PhysicalAddress;

public class ReturnInfo {

	/** The library to which the item should be returned */
	AgencyId returnAgencyId;

	/** The address to which the item has to be returned */
	PhysicalAddress physicalAddress;

	public ReturnInfo(AgencyId returnAgencyId = null, PhysicalAddress physicalAddress = null) {
		this.returnAgencyId = returnAgencyId;
		this.physicalAddress = physicalAddress;
	}
}
