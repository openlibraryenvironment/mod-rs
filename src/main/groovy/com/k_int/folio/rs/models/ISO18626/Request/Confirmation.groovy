package com.k_int.folio.rs.models.ISO18626.Request

import com.k_int.folio.rs.models.ISO18626.ConfirmationHeader;

public class Confirmation {

	/** The header section */
	ConfirmationHeader header;

	public RequestAgencyMessage(ConfirmationHeader header) {
		this.header = header;
	}
}
