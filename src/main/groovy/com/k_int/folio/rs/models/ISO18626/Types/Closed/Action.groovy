package com.k_int.folio.rs.models.ISO18626.Types.Closed

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class Action extends ReferenceData {

	public Action() {
		this(null);
	}

	public Action(String code) {
		super(ReferenceTypes.ACTION, code);
	}
}
