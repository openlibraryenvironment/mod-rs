package com.k_int.folio.rs.models.ISO18626.Request

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.k_int.folio.rs.models.ISO18626.Types.BibliographicItemId;
import com.k_int.folio.rs.models.ISO18626.Types.BibliographicRecordId;

public class BibliographicInfo {

	/** Suppliers unqiue bibliographic id */
	public String supplierUniqueRecordId;

	/** Title */
	public String title;

	/** Author */
	public author;

	/** Subtitle */
	public String subtitle;

	/** Series Title */
	public String seriesTitle;

	/** Edition */
	public String edition;

	/** Title of Component */
	public String titleOfComponent;
	
	/** Author of Component */
	public String authorOfComponent;

	/** Volume */
	public String volume;

	/** Issue */
	public String issue;
	
	/** Pages requested */
	public String pagesRequested;

	/** Estimated number of pages */
	public estimatedNumberOfPages;

	/** The ids that this item can be identified by */
	@JacksonXmlElementWrapper(useWrapping = false)
 	public List<BibliographicItemId> bibliographicItemId;

	/** Sponsor */
	public String sponsor;

	/** Where we obtain the bib information from */
	String informationSource;

	/** the The ids for the record */
	@JacksonXmlElementWrapper(useWrapping = false)
 	List<BibliographicRecordId> bibliographicRecordId;

	public BibliographicInfo() {
	} 	
}
