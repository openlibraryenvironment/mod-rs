package org.olf.rs

import org.olf.rs.shared.TenantSymbolMapping
import grails.gorm.multitenancy.*
import grails.gorm.transactions.Transactional

public class GlobalConfigService {

  def grailsApplication

  private static final String SHARED_SCHEMA_NAME = '__global_mod_rs';

  public void registerSymbolForTenant(String symbol, String tenant_id) {

    log.debug("registerSymbolForTenant(symbol:${symbol},tenant:${tenant_id}) in_schema:(${SHARED_SCHEMA_NAME})");
    Tenants.withId(SHARED_SCHEMA_NAME) {
      TenantSymbolMapping tsm = TenantSymbolMapping.findBySymbol(symbol.toUpperCase()) 
      if ( tsm == null ) {
        log.debug("No tsm for ${symbol} so register a new one against tenant ${tenant_id}");
        tsm = new TenantSymbolMapping(
                         symbol:symbol.toUpperCase(),
                         tenant:tenant_id).save(flush:true, failOnError:true);
      }
      else {
        if ( tsm.tenant == tenant_id ) {
          // Nothing to do
          log.debug("tenant symbol mapping for that tuple already exists.");
        }
        else {
	  log.debug("Update existing mapping...");
          tsm.tenant = tenant_id
          tsm.save(flush:true, failOnError:true);
        }
      }
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

  public List<String> getSymbolsForTenant(String tenant) {
    List<String> result = new ArrayList<String>();
    Tenants.withId(SHARED_SCHEMA_NAME) {
      def symbols_for_tenant = TenantSymbolMapping.findAllByTenant(tenant)
      symbols_for_tenant.each { tsm ->
        result.add(tsm.symbol);
      }
    }
    log.debug("getSymbolsForTenant(${tenant}) returns ${result}");

    return result;
  }

}

