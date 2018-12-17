package com.k_int.folio.rs.models.ISO18626.Request

import com.k_int.folio.rs.models.ISO18626.YesNo
import com.k_int.folio.rs.models.ISO18626.Types.Address
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.PatronType;

public class PatronInfo {

	/** The patron identifier */
	public String patronId;

	/** The patrons surname */
	public String surname;

	/** First name of the patron */
	public String givenName;

	/** The patron type */
	public PatronType patronType;

	/** Whether the item should be sent directly to the patron or not */
	public YesNo sendToPatron;

	/** The addresses the patron can be contacted at */
	public List<Address> address;

	public PatronInfo() {
	}
}
