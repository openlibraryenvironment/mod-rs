package org.olf.rs

public class ManualPatronStoreService implements BasePatronStoreActions {
  public boolean createPatronStore(Map patronData) {
    return true;
  }

  public Map lookupPatronStore(String systemPatronId) {
    return [:];
  }

  public Map lookupOrCreatePatronStore(String systemPatronId, Map patronData) {
    return patronData;
  }
}