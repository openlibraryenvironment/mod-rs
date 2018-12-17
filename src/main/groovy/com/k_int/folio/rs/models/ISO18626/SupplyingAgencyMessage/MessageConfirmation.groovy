package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

public class MessageConfirmation {

	/** The header section */
	MessageConfirmationHeader header;

	public RequestAgencyMessage(MessageConfirmationHeader header) {
		this.header = header;
	}
}
