package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes;

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class ElectronicAddressType extends ReferenceData {

	public ElectronicAddressType() {
		this(null);
	}

	public ElectronicAddressType(String code) {
		super(ReferenceTypes.ELECTRONIC_ADDRESS_TYPE, code);
	}
}
