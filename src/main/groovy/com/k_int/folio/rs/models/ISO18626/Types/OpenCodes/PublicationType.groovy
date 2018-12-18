package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class PublicationType extends ReferenceData {

	public PublicationType() {
		this(null);
	}

	public PublicationType(String code) {
		super(ReferenceTypes.PUBLICATION_TYPE, code);
	}
}
