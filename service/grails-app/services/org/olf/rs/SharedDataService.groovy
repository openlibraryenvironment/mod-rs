package org.olf.rs


public class SharedDataService {

  private static final SHARED_SCHEMA_NAME='__shared_ill_mappings';

  public void registerSymbolForTenant(String symbol, String tenant_id) {
  }

  public String getTenantForSymol(String symbol) {
    return null;
  }

  // public void ensureSharedConfig() {
    // This is for DEVELOPMENT ONLY - Need to find a sysadmin way to do this
  //   registerTenantSymbolMapping('RESHARE:DIKUA', 'diku');
  //   registerTenantSymbolMapping('RESHARE:DIKUB', 'diku');
  //   registerTenantSymbolMapping('RESHARE:DIKUC', 'diku');
  //   registerTenantSymbolMapping('RESHARE:ACMAIN', 'diku');
  // }

  // public void registerTenantSymbolMapping(String symbol, String tenant) {
  //   Tenants.withId(SHARED_SCHEMA_NAME) {
  //     TenantSymbolMapping.withNewTransaction {
  //       TenantSymbolMapping.findBySymbol(symbol) ?: new TenantSymbolMapping(
  //                                                               symbol:symbol,
  //                                                               tenant:tenant).save(flush:true, failOnError:true);
  //     }
  //   }
  // }

}

