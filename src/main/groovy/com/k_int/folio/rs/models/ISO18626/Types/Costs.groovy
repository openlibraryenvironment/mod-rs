package com.k_int.folio.rs.models.ISO18626.Types

import com.k_int.folio.rs.models.ISO18626.Types.Standard.CurrencyCode;

public class Costs {
	/** The currency this cost is represented by */
	CurrencyCode currencyCode;

	/** The cost in terms of the currency */
	String monetaryValue;

	public Costs(CurrencyCode currencyCode = null, String monetaryValue = null) {
		this.currencyCode = currencyCode;
		this.monetaryValue = monetaryValue;
	}
}
