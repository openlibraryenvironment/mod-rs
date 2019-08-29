package org.olf.rs

import org.olf.rs.shared.TenantSymbolMapping
import grails.gorm.multitenancy.Tenants

public class SharedDataService {

  
  private static final SHARED_SCHEMA_NAME = '__shared_ill_mappings';

  public void registerSymbolForTenant(String symbol, String tenant_id) {
    Tenants.withId(SHARED_SCHEMA_NAME) {
      TenantSymbolMapping.withNewTransaction {
        TenantSymbolMapping.findBySymbol(symbol) ?: new TenantSymbolMapping(
          symbol:symbol,
          tenant:tenant).save(flush:true, failOnError:true);
      }
    }
  }

  public String getTenantForSymbol(String symbol) {
    String result = null
    
    
    Tenants.withId(SHARED_SCHEMA_NAME) {
      result = TenantSymbolMapping.findBySymbol(symbol)
      log.debug("GetTenant returns: " + result)
    }
      return "GetTenant returns: " + result;
  }

  // public void ensureSharedConfig() {
    // This is for DEVELOPMENT ONLY - Need to find a sysadmin way to do this
  //   registerTenantSymbolMapping('RESHARE:DIKUA', 'diku');
  //   registerTenantSymbolMapping('RESHARE:DIKUB', 'diku');
  //   registerTenantSymbolMapping('RESHARE:DIKUC', 'diku');
  //   registerTenantSymbolMapping('RESHARE:ACMAIN', 'diku');
  // }
   

}

