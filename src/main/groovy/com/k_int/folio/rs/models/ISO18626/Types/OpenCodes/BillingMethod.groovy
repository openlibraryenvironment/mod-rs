package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class BillingMethod extends ReferenceData {

	public BillingMethod(String code = null, boolean validated = false) {
		super(ReferenceTypes.BILLING_METHOD, code, validated);
	}
}
