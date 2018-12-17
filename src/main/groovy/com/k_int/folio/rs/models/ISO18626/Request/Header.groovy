package com.k_int.folio.rs.models.ISO18626.Request

import com.k_int.folio.rs.models.ISO18626.RequestingAgencyHeader;

public class Header extends RequestingAgencyHeader {

	/** Multiple Item request Id */
	String multipleItemRequestId;

	public Header(String multipleItemRequestId = null) {
		this.multipleItemRequestId = multipleItemRequestId;
	}
}
