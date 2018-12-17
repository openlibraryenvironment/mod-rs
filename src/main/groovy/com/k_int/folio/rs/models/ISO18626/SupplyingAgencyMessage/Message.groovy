package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

public class Message {
	Header header;
	MessageInfo messageInfo;
	StatusInfo statusInfo;
	DeliveryInfo deliveryInfo;
	ReturnInfo returnInfo;
	
	public Message(Header header = null, MessageInfo messageInfo = null, StatusInfo statusInfo = null,
		           DeliveryInfo deliveryInfo = null, ReturnInfo returnInfo = null) {
		this.header = header;
		this.messageInfo = messageInfo;
		this.statusInfo = statusInfo;
		this.deliveryInfo = deliveryInfo;
		this.returnInfo = returnInfo;
	}
}
