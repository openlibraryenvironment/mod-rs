package com.k_int.folio.rs.models.ISO18626

/**
 * This is just a placeholder class, as I gather there is one in a library
 * @author Chas
 *
 */
public class ReferenceData {

	/** I have assumed string, but could be anything else */
	String referenceType;

	/** The code that is unqiue for this reference type */	
	String code;
	
	/** The display string for this code */
	String displayValue;
	
	/** A description of this value */
	String description;

	/** Have we verified this is a valid piece of reference data */
	boolean verified = false;
	
	public ReferenceData(String referenceType, String code = null, boolean verified = false) {
		this.referenceType = referenceType;
		this.code = code;
		this.verified = verified;
	}
}
