package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName="supplyingAgencyMessage")
public class Message {
	Header header;
	MessageInfo messageInfo;
	StatusInfo statusInfo;
	DeliveryInfo deliveryInfo;
	ReturnInfo returnInfo;
	
	public Message() {
	}

	public Message(Header header, MessageInfo messageInfo = null, StatusInfo statusInfo = null,
		           DeliveryInfo deliveryInfo = null, ReturnInfo returnInfo = null) {
		this.header = header;
		this.messageInfo = messageInfo;
		this.statusInfo = statusInfo;
		this.deliveryInfo = deliveryInfo;
		this.returnInfo = returnInfo;
	}
}
