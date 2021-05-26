package org.olf.rs.patronstore


public interface PatronStoreActions {
  
  /* 
   * Create a backend store in whatever system we are using to hold patrons
   * to represent a single patron record
   */
  public abstract boolean createPatronStore(Map patronData);
  
  /*
   * Retrieve a map with information from the backend store for a given
   * external system identifier (if it exists)
   */
  public abstract Map lookupPatronStore(String systemPatronId);

  public abstract Map lookupOrCreatePatronStore(String systemPatronId, Map patronData); 

  public abstract boolean updateOrCreatePatronStore(String systemPatronId, Map patronData);
  
  
	
}

