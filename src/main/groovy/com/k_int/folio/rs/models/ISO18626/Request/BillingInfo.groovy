package com.k_int.folio.rs.models.ISO18626.Request

import com.k_int.folio.rs.models.ISO18626.Types.Address;
import com.k_int.folio.rs.models.ISO18626.Types.Costs;
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.BillingMethod;
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.PaymentMethod;

class BillingInfo {

	/** The preferred payment method */
	PaymentMethod paymentMethod;

	/** The preferred billing method */
	BillingMethod billingMethod;

	/** The maximum costs that we want to pay */
	Costs maximumCosts;

	/** The person are department that should be billed */
	String billingName;

	/** The address they want it sending to */
	Address address;
}
