package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import java.time.LocalDateTime
import org.olf.okapi.modules.directory.DirectoryEntry

class HostLMSShelvingLocation implements MultiTenant<HostLMSShelvingLocation> {

  String id
  String code
  String name
  Date dateCreated
  Date lastUpdated
  // < 0 - Never use
  // 0 - No preference / default
  // > 0 - Preference order
  Long supplyPreference

  static constraints = {
    code (nullable: false)
    name (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    supplyPreference (nullable: true)
  }

  static mapping = {
    table 'host_lms_shelving_loc'
                               id column : 'hlsl_id', generator: 'uuid2', length:36
                          version column : 'hlsl_version'
                             code column : 'hlsl_code'
                             name column : 'hlsl_name'
                      dateCreated column : 'hlsl_date_created'
                      lastUpdated column : 'hlsl_last_updated'
                 supplyPreference column : 'hlsl_supply_preference'
  }

  public String toString() {
    return "HostLMSShelvingLocation: ${code}".toString()
  }
}

