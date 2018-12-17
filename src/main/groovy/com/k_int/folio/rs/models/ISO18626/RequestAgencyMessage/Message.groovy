package com.k_int.folio.rs.models.ISO18626.RequestAgencyMessage

import com.k_int.folio.rs.models.ISO18626.ActiveSection

class Message {

	/** The header section */
	Header header;

	/** What the requester wants to do */
	ActiveSection activeSection;

	public Message(Header header, ActiveSection activeSection) {
		this.header = header;
		this.activeSection = activeSection;
	}
}
