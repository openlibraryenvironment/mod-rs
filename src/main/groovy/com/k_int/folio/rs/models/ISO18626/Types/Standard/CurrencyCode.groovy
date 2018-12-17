package com.k_int.folio.rs.models.ISO18626.Types.Standard

import com.k_int.folio.rs.models.ISO18626.ReferenceData;
import com.k_int.folio.rs.models.ISO18626.Types.ReferenceTypes;

/**
 * Standard: ISO 4217
 * List: https://www.currency-iso.org/en/home/tables/table-a1.html
 * 
 * @author Chas
 *
 */
public class CurrencyCode extends ReferenceData {

	public CurrencyCode(String code = null, boolean validated = false) {
		super(ReferenceTypes.CURRENCY_CODE, code, validated);
	}
}
