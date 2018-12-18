package com.k_int.folio.rs.models.ISO18626.Request

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.k_int.folio.rs.models.ISO18626.Types.Address;

public class RequestingAgencyInfo {

	/** Name of the requesting agency */
	public String name;

	/** The contact name of the person responsible within the agency */
	public String contactName;

	/** The contact addresses for the requesting agency */
	@JacksonXmlElementWrapper(useWrapping = false)
	public List<Address> address;

	public RequestingAgencyInfo() {
	}
}
