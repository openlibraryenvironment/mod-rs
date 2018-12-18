package com.k_int.folio.rs.models.ISO18626.Request

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement
public class Message {

	/** The header for the message */
	public Header header;

	/** Details about the item being requested */
	public BibliographicInfo bibliographicInfo;

	//* Details about the publication */
	public PublicationInfo publicationInfo;

	/** Details about the service */
	public ServiceInfo serviceInfo;

	/** The Suppliers */
	@JacksonXmlElementWrapper(useWrapping = false)
 	public List<SupplierInfo> supplierInfo;

	/** The delivery information */
	@JacksonXmlElementWrapper(useWrapping = false)
	public List<RequestedDeliveryInfo> requestedDeliveryInfo;

	/** Requesting agency information */
	public RequestingAgencyInfo requestingAgencyInfo;

	/** Patron information */
	public PatronInfo patronInfo;

	/** Billing information*/
	public BillingInfo billingInfo;

	public Message() {
	}

	public Message(Header header = null, BibliographicInfo bibliographicInfo = null, PublicationInfo publicationInfo = null,
		           ServiceInfo serviceInfo = null, List<SupplierInfo> supplierInfo = null, List<RequestedDeliveryInfo> requestedDeliveryInfo = null,
				   RequestingAgencyInfo requestingAgencyInfo = null, PatronInfo patronInfo = null, BillingInfo billingInfo = null) {
		this.header = header;
		this.bibliographicInfo = bibliographicInfo;
		this.publicationInfo = publicationInfo;
		this.serviceInfo = serviceInfo;
		this.supplierInfo = supplierInfo;
		this.requestedDeliveryInfo = requestedDeliveryInfo;
		this.requestingAgencyInfo = requestingAgencyInfo;
		this.patronInfo = patronInfo;
		this.billingInfo = billingInfo;
	}
}
