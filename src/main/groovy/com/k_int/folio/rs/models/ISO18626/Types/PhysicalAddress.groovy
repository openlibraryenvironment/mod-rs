package com.k_int.folio.rs.models.ISO18626.Types

import com.k_int.folio.rs.models.ISO18626.Types.Standard.CountryCode
import com.k_int.folio.rs.models.ISO18626.Types.Standard.RegionCode

public class PhysicalAddress {
	/** first line of the address */
	String line1;

	/** Second line of the address */
	String line2;

	/** locality of the library or agency or the contacts name */
	String locality;

	/** Postcode */
	String postalCode;

	/** Region within the country */
	RegionCode region;

	/** The country code of the address */
	CountryCode country;

	public PhysicalAddress(String line1 = null, String line2 = null, String locality = null, String postalCode = null, region = null, country = null) {
		this.line1 = line1;
		this.line2 = line2;
		this.locality = locality;
		this.postalCode = postalCode;
		this.region = region;
		this.country = country;
	}
}
