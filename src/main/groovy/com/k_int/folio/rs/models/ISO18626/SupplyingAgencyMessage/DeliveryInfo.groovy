package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

import com.k_int.folio.rs.models.ISO18626.YesNo
import com.k_int.folio.rs.models.ISO18626.Types.Costs
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.DeliveredFormat
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.LoanCondition
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.SentVia;

class DeliveryInfo {

	/** Date when the item was sent, Format: YYYY-MM-DDThh:mm:ssZ */
	Date dateSent;

	/** Barcode or rfid tag id of the item being sent */
	String itemId;
	
	/** The service by which the item was sent */
	SentVia sentVia;

	/** Was the item sent to the patron */
	boolean sentToPatron;

	/** Conditions for the use of the item */
	LoanCondition loanCondition;

	/** The format of the delivered item */
	DeliveredFormat deliveredFormat;

	/** The total cost for the item (including delivery) */
	Costs deliveryCosts;

	public DeliveryInfo(Date dateSent = null, String itemId = null, SentVia sentVia = null, boolean sentToPatron = null,
		                LoanCondition loanCondition = null, DeliveredFormat deliveredFormat = null, Costs deliveryCosts = null) {
		this.dateSent = dateSent;
		this.itemId = itemId;
		this.sentVia = sentVia;
		this.sentToPatron = sentToPatron;
		this.loanCondition = loanCondition;
		this.deliveredFormat = deliveredFormat;
		this.deliveryCosts = deliveryCosts;
	}
}
