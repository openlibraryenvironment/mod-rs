package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class PaymentMethod extends ReferenceData {

	public PaymentMethod(String code = null, boolean validated = false) {
		super(ReferenceTypes.PAYMENT_METHOD, code, validated);
	}
}
