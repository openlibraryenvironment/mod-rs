package com.k_int.folio.rs.models.ISO18626.Request

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.k_int.folio.rs.models.ISO18626.YesNo;
import com.k_int.folio.rs.models.ISO18626.Types.Closed.RequestSubType;
import com.k_int.folio.rs.models.ISO18626.Types.Closed.RequestType;
import com.k_int.folio.rs.models.ISO18626.Types.Closed.ServiceType
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.CopyrightCompliance;
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.PreferredFormat;
import com.k_int.folio.rs.models.ISO18626.Types.OpenCodes.ServiceLevel;

public class ServiceInfo {

	/** The type of request */
	public RequestType requestType;

	/** The subtype of a request */
	@JacksonXmlElementWrapper(useWrapping = false)
	public List<RequestSubType> requestSubType;

	/** The previous request id, if there was one */
	public String requestingAgencyPreviousRequestId;

	/** The service type that is acceptable to fulfill the request */
	public ServiceType serviceType;

	/** the service level that needs to be fulfilled */
	public ServiceLevel serviceLevel;

	/** The preferred medium that we want it delivered in */
	public PreferredFormat preferredFormat;

	/** The date that we need it by, Format: YYYY-MM-DDThh:Mmm:ssZ */
	public Date needBeforeDate;

	/** The copyright we conform to */
	public CopyrightCompliance copyrightCompliance;

	/** Are we interested in any edition of the item */
	public YesNo anyEdition;

	/** Start date for a booking, Format: YYYY-MM-DDThh:Mmm:ssZ */
	public Date startDate;

	/** End date for a booking, Format: YYYY-MM-DDThh:Mmm:ssZ */
	public Date endDate;

	/** Any additional information */
	public String note;

	public ServiceInfo() {
	}
}
