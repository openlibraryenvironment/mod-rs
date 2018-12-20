package com.k_int.folio.rs.models.ISO18626.SupplyingAgencyMessage

import com.k_int.folio.rs.models.ISO18626.YesNo;
import com.k_int.folio.rs.models.ISO18626.Types.Costs;
import com.k_int.folio.rs.models.ISO18626.Types.Closed.ReasonForMessage;
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.ReasonRetry;
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.ReasonUnfilled;

public class MessageInfo {
	/** The reason for sending this message */
	public ReasonForMessage reasonForMessage;

	/** If the action is a renew response or cancel response, then this is the answer */
	public YesNo answerYesNo;

	/** A note to go along with the message */
	public String note;

	/** The reason why the request cannot be fulfilled */
	public ReasonUnfilled reasonUnfilled;

	/** The reason for a retry */
	public ReasonRetry reasonRetry;

	/** The actual cost, if the max cost is less than the actual cost */
	public Costs offeredCosts;

	/** Do not send send a retry before this date, Format: YYYY-MM-DDThh:mm:ssZ */	
	public Date retryAfter;

	/** Do not send send a retry after this date, Format: YYYY-MM-DDThh:mm:ssZ */	
	public Date retryBefore;

	public MessageInfo() {
	}

	public MessageInfo(ReasonForMessage reasonForMessage, YesNo answerYesNo, String note,
		               ReasonUnfilled reasonUnfilled, ReasonRetry reasonRetry, Costs offeredCosts,
					   Date retryAfter, Date retryBefore) {
		this.reasonForMessage = reasonForMessage;
		this.answerYesNo = answerYesNo;
		this.note = note;
		this.reasonUnfilled = reasonUnfilled;
		this.reasonRetry = reasonRetry;
		this.offeredCosts = offeredCosts;
		this.retryAfter = retryAfter;
		this.retryBefore = retryBefore;
	}
}
