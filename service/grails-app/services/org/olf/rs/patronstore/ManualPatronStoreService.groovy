package org.olf.rs.patronstore

import org.olf.rs.patronstore.PatronStoreActions

public class ManualPatronStoreService implements PatronStoreActions {
  public boolean createPatronStore(Map patronData) {
    return true;
  }

  public Map lookupPatronStore(String systemPatronId) {
    return [:];
  }

  public Map lookupOrCreatePatronStore(String systemPatronId, Map patronData) {
    return patronData;
  }

  public boolean updateOrCreatePatronStore(String systemPatronId, Map patronData) {
    return true;
  }
}