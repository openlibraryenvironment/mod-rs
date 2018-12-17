package com.k_int.folio.rs.models.ISO18626

import com.k_int.folio.rs.models.ISO18626.Types.AgencyId;

class HeaderBase {
	/** The Id of the supplying agency */
	public AgencyId supplyingAgencyId;

	/** The Id of the requesting agency */
	public AgencyId requestingAgencyId;

	/** Date / time of the request validation, Format for sending and receiving is "YYYY-MM-DDThh:mm:ssZ" */
	public Date timestamp;

	/** The unique identifier of the request, from the requesters perspective */
	public String requestingAgencyRequestId;
	
	public HeaderBase() {
	}
}
