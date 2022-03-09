package org.olf.rs.referenceData;

import groovy.util.logging.Slf4j

@Slf4j
public class NamingAuthority {

	/**
	 * Ensures that the naming authority exists in the database
	 * @param symbol The symbol that represents this naming authority
	 * @return the found or created naming authority
	 */
	public org.olf.okapi.modules.directory.NamingAuthority ensureNamingAuthority(String symbol) {
		org.olf.okapi.modules.directory.NamingAuthority result = org.olf.okapi.modules.directory.NamingAuthority.findBySymbol(symbol);
		if (result == null) {
			result = new org.olf.okapi.modules.directory.NamingAuthority(symbol: symbol);
			result.save(flush:true, failOnError:true);
		}
		return result;
	}
	
	/**
	 * Loads the naming authorities into the database	
	 */
	public void load() {
		try {
			log.info("Adding naming authorities to the database");
		  
			ensureNamingAuthority("RESHARE");
			ensureNamingAuthority("ISIL");
			ensureNamingAuthority("OCLC");
			ensureNamingAuthority("EXL");
			ensureNamingAuthority("PALCI");
			ensureNamingAuthority("CARDINAL");
		  
		} catch ( Exception e ) {
			log.error("Exception thrown while loading naming authorities", e);
		}
	}
	
	public static void loadAll() {
		(new NamingAuthority()).load();
	}
}
