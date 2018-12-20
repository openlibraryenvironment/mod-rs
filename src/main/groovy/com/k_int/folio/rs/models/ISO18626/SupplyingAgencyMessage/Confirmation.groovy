package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.k_int.folio.rs.models.ISO18626.ConfirmationHeader;
import com.k_int.folio.rs.models.ISO18626.ErrorData;
import com.k_int.folio.rs.models.ISO18626.Types.Closed.ReasonForMessage;

@JacksonXmlRootElement(localName="supplyingAgencyMessageConfirmation")
public class Confirmation {

	/** The header section */
	ConfirmationHeader confirmationHeader;

	/** The reason the message was sent */
	ReasonForMessage reasonForMessage;
	
	/** The Error data if any */
	ErrorData errorData;
	
	public Confirmation() {
	}

	public Confirmation(ConfirmationHeader confirmationHeader, ReasonForMessage reasonForMessage, ErrorData errorData) {
		this.confirmationHeader = confirmationHeader;
		this.reasonForMessage = reasonForMessage;
		this.errorData = errorData;
	}
}
