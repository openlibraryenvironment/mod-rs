package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

import com.k_int.folio.rs.models.ISO18626.ConfirmationHeader
import com.k_int.folio.rs.models.ISO18626.Types.Closed.ReasonForMessage

public class MessageConfirmationHeader extends ConfirmationHeader {

	/** The header section */
	public ReasonForMessage reasonForMessage;

	public MessageConfirmationHeader() {
	}
}
