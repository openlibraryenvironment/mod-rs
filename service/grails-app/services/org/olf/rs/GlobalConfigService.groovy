package org.olf.rs

import org.olf.rs.shared.TenantSymbolMapping
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional

@Transactional
public class GlobalConfigService {

  def grailsApplication

  private static Map<String,String> data = new java.util.HashMap()
  
  private static final String SHARED_SCHEMA_NAME = '__shared_ill_mappings';

  public void registerSymbolForTenant(String symbol, String tenant_id) {

    log.debug("registerSymbolForTenant(${symbol},${tenant_id}) (${SHARED_SCHEMA_NAME})");

    // Tenants.withId(SHARED_SCHEMA_NAME) {
    //     TenantSymbolMapping.findBySymbol(symbol) ?: new TenantSymbolMapping(
    //       symbol:symbol,
    //       tenant:tenant_id).save(flush:true, failOnError:true);
    // }
    data[symbol] = tenant_id
  }

  public String getTenantForSymbol(String symbol) {
    log.debug("getTenantForSymbol(${symbol})");
    // Tenants.withId(SHARED_SCHEMA_NAME) {
    //   result = TenantSymbolMapping.findBySymbol(symbol)
    //   log.debug("GetTenant returns: " + result)
    // }
    // return "GetTenant returns: " + result;
    log.debug("Data for the symbol: ${data[symbol]}")
    return data[symbol]
  }

  // public void ensureSharedConfig() {
    // This is for DEVELOPMENT ONLY - Need to find a sysadmin way to do this
  //   registerTenantSymbolMapping('RESHARE:DIKUA', 'diku');
  //   registerTenantSymbolMapping('RESHARE:DIKUB', 'diku');
  //   registerTenantSymbolMapping('RESHARE:DIKUC', 'diku');
  //   registerTenantSymbolMapping('RESHARE:ACMAIN', 'diku');
  // }
   

}

