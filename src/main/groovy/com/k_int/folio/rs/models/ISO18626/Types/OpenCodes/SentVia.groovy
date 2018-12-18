package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class SentVia extends ReferenceData {

	public SentVia() {
		this(null);
	}

	public SentVia(String code) {
		super(ReferenceTypes.SENT_VIA, code);
	}
}
