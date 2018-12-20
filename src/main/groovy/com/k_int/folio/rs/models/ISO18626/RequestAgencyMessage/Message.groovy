package com.k_int.folio.rs.models.ISO18626.RequestAgencyMessage

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.k_int.folio.rs.models.ISO18626.Types.Closed.Action

@JacksonXmlRootElement(localName="requestingAgencyMessage")
class Message {

	/** The header section */
	Header header;

	/** The action to be performed */
	Action action;

	/** Notes that may be relevant for this action */
	String note;

	public Message() {
	}

	public Message(Header header, Action action, String note) {
		this.header = header;
		this.action = action;
		this.note = note;
	}
}
