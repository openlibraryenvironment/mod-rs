package com.k_int.folio.rs.models.ISO18626.Request

public class Message {

	/** The header for the message */
	Header header;

	/** Details about the item being requested */
	BibliographicInfo bibliographicInfo;

	//* Details about the publication */
	PublicationInfo publicationInfo;

	/** Details about the service */
	ServiceInfo serviceInfo;

	/** The Suppliers */
	List<SupplierInfo> supplierInfo;

	/** The delivery information */
	List<RequestedDeliveryInfo> requestedDeliveryInfo;

	/** Requesting agency information */
	RequestingAgencyInfo requestingAgencyInfo;

	/** Patron information */
	PatronInfo patronInfo;

	/** Billing information*/
	BillingInfo billingInfo;

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
