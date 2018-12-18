package com.k_int.folio.rs.models.ISO18626

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * This is just a placeholder class, as I gather there is one in a library
 * @author Chas
 *
 */
public class ReferenceData {

	/** I have assumed string, but could be anything else */
	@JsonIgnore
	String referenceType;

	/** The code that is unique for this reference type */
	@JacksonXmlText 
	String code;
	
	public ReferenceData(String referenceType, String code = null) {
		this.referenceType = referenceType;
		this.code = code;
	}
}
