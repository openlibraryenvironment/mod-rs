package org.olf.rs

import org.olf.rs.shared.TenantSymbolMapping
import grails.gorm.multitenancy.*
import grails.gorm.transactions.Transactional

public class GlobalConfigService {

  def grailsApplication

  private static final String SHARED_SCHEMA_NAME = '__global_mod_rs';

  public void registerSymbolForTenant(String symbol, String tenant_id) {

    log.debug("registerSymbolForTenant(${symbol},${tenant_id}) (${SHARED_SCHEMA_NAME})");
    Tenants.withId(SHARED_SCHEMA_NAME) {
      TenantSymbolMapping.findBySymbol(symbol) ?: new TenantSymbolMapping(
              symbol:symbol,
              tenant:tenant_id).save(flush:true, failOnError:true);
    }
  }

  public String getTenantForSymbol(String symbol) {
    log.debug("getTenantForSymbol(${symbol})");
    String result = null;
    Tenants.withId(SHARED_SCHEMA_NAME) {
      result = TenantSymbolMapping.findBySymbol(symbol)?.tenant
    }
    return result
  }

  // public void ensureSharedConfig() {
    // This is for DEVELOPMENT ONLY - Need to find a sysadmin way to do this
  //   registerTenantSymbolMapping('RESHARE:DIKUA', 'diku');
  //   registerTenantSymbolMapping('RESHARE:DIKUB', 'diku');
  //   registerTenantSymbolMapping('RESHARE:DIKUC', 'diku');
  //   registerTenantSymbolMapping('RESHARE:ACMAIN', 'diku');
  // }
   

}

