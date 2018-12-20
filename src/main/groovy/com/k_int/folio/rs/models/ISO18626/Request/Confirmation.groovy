package com.k_int.folio.rs.models.ISO18626.Request

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.k_int.folio.rs.models.ISO18626.ConfirmationHeader;
import com.k_int.folio.rs.models.ISO18626.ErrorData;

@JacksonXmlRootElement(localName="requestConfirmation")
public class Confirmation {

	/** The header section */
	ConfirmationHeader confirmationHeader;

	/** The Error data if any */
	ErrorData errorData;
	
	public Confirmation() {
	}

	public Confirmation(ConfirmationHeader confirmationHeader, ErrorData errorData) {
		this.confirmationHeader = confirmationHeader;
		this.errorData = errorData;
	}
}
