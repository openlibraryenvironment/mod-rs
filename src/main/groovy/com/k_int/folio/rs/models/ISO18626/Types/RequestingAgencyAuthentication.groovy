package com.k_int.folio.rs.models.ISO18626.Types


public class RequestingAgencyAuthentication {
	/** The account used by this agency */
	String accountId;

	/** The security code associated with this account */
	String securityCode;

	public RequestingAgencyAuthentication(String accountId = null, String securityCode = null) {
		this.accountId = accountId;
		this.securityCode = securityCode;
	}
}
