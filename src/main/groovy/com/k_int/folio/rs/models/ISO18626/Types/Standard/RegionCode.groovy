package com.k_int.folio.rs.models.ISO18626.Types.Standard

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

/**
 * Standard: ISO 3166-2
 * List: http://www.unece.org/cefact/locode/subdivisions.html
 * 
 * @author Chas
 *
 */
public class RegionCode extends ReferenceData {

	public RegionCode(String code = null, boolean validated = false) {
		super(ReferenceTypes.REGION_CODE, code, validated);
	}
}
