package com.k_int.folio.rs.models.ISO18626.Request

import com.k_int.folio.rs.models.ISO18626.Types.Address;

public class RequestedDeliveryInfo {

	/** The priority of this address */
	int sortOrder;

	/** The address where they want it to be delivered */
	Address address;

	public RequestedDeliveryInfo(int sortOrder = 1, Address address = null) {
		this.sortOrder = sortOrder;
		this.address = address;
	}
}
