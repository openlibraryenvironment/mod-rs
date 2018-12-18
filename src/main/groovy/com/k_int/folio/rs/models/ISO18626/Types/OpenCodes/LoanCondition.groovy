package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class LoanCondition extends ReferenceData {

	public LoanCondition() {
		this(null);
	}

	public LoanCondition(String code) {
		super(ReferenceTypes.LOAN_CONDITION, code);
	}
}
