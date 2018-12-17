package com.k_int.folio.rs.models.ISO18626.Types

import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.ElectronicAddressType;;

public class ElectronicAddress {
	/** The type od address */
	ElectronicAddressType electronicAddressType;

	/** The data associated with this elactronic address type */
	String electronicAddressData;

	public ElectronicAddress(ElectronicAddressType electronicAddressType = null, String electronicAddressData = null) {
		this.electronicAddressType = electronicAddressType;
		this.electronicAddressData = electronicAddressData;
	}
}
