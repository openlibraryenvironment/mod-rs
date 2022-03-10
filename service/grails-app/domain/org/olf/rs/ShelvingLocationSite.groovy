package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import java.time.LocalDateTime
import org.olf.okapi.modules.directory.DirectoryEntry

class ShelvingLocationSite implements MultiTenant<ShelvingLocationSite> {

  String id
  HostLMSShelvingLocation shelvingLocation
  HostLMSLocation location
  Date dateCreated
  Date lastUpdated
  // < 0 - Never use
  // 0 - No preference / default
  // > 0 - Preference order
  Long supplyPreference

  static constraints = {
    shelvingLocation (nullable: false)
    location (nullable: false)
    supplyPreference (nullable: true)
  }

  static mapping = {
    table 'shelving_loc_site'
                               id column : 'sls_id', generator: 'uuid2', length:36
                          version column : 'sls_version'
                 shelvingLocation column : 'sls_shelving_loc_fk'
                         location column : 'sls_location_fk'
                      dateCreated column : 'sls_date_created'
                      lastUpdated column : 'sls_last_updated'
                 supplyPreference column : 'sls_supply_preference'
  }

  public String toString() {
    return "ShelvingLocationSite: ${id}".toString()
  }
}

