package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

import com.k_int.folio.rs.models.ISO18626.Types.Closed.Status

public class StatusInfo {

	/** The status of the request in the supplying library */
	Status status;

	/** The expected delivery date, Format: YYYY-MM-DDThh:mm:ssZ */
	Date expectedDeliveryDate;

	/** The date the item is to be returned, Format: YYYY-MM-DDThh:mm:ssZ */
	Date dueDate;

	/** The date of the last change in the status, Format: YYYY-MM-DDThh:mm:ssZ */
	Date lastChange;

	public StatusInfo() {
	}
}
