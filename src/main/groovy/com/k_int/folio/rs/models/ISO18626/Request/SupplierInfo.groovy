package com.k_int.folio.rs.models.ISO18626.Request

import com.k_int.folio.rs.models.ISO18626.Types.AgencyId;
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.BibliographicRecordIdentifierCode;

public class SupplierInfo {

	/** The order of this supplier */
	public int sortOrder;
	
	/** the supplier identifier */
	public AgencyId supplierCode;

	/** The description for the supplier */
	public String supplierDescription;

	/** The item as known by the supplier */
	public BibliographicRecordIdentifierCode bibliographicRecordId;

	/** The call number (shelfmar) kas known by the supplier */
	public String callNumber;

	/** The holdings for this supplier */
	public String summaryHoldings;

	/** Any availability at the time of the request */
	public String availabilityNote; 

	public SupplierInfo() {
	}
}
