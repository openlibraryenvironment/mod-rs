package com.k_int.folio.rs.models.ISO18626.Types.OpenCodes;

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

public class ElectronicAddressType extends ReferenceData {

	public ElectronicAddressType(String code = null, boolean validated = false) {
		super(ReferenceTypes.ELECTRONIC_ADDRESS_TYPE, code, validated);
	}
}
