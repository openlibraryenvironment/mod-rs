package com.k_int.folio.rs.models.ISO18626.Types

import com.k_int.folio.rs.models.ISO18626.Types.Standard.CurrencyCode;

public class Costs {
	/** The currency this cost is represented by */
	public CurrencyCode currencyCode;

	/** The cost in terms of the currency */
	public String monetaryValue;

	public Costs() {
		this(null, null);
	}

	public Costs(CurrencyCode currencyCode, String monetaryValue) {
		this.currencyCode = currencyCode;
		this.monetaryValue = monetaryValue;
	}
}
