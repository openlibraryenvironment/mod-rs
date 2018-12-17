package com.k_int.folio.rs.models.ISO18626.Types

/**
 * Only one of the address elements will be filled out, so no need to say which one should be used
 */
public class Address {

	/** The electronic address if specified */
	ElectronicAddress electronicAddress;

	/** The physical address if specified */
	PhysicalAddress physicalAddress;

	public Address(ElectronicAddress electronicAddress = null, PhysicalAddress physicalAddress = null) {
		this.electronicAddress = electronicAddress;
		this.physicalAddress = physicalAddress;
	} 
}
