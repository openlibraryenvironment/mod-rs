package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class LoanCondition extends ReferenceData {

	public LoanCondition(String code = null, boolean validated = false) {
		super(ReferenceTypes.LOAN_CONDITION, code, validated);
	}
}
