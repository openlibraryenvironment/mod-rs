package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class Status extends ReferenceData {

	public Status() {
		this(null);
	}

	public Status(String code = null) {
		super(ReferenceTypes.STATUS, code);
	}
}
