package org.olf.rs

import grails.gorm.MultiTenant

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

  /** The hidden field if set to true, means they have tried to delete it but it is still linked to another record, so we just mark it as hidden */
  Boolean hidden;

  static constraints = {
    code (nullable: false)
    name (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    supplyPreference (nullable: true)
    hidden (nullable: true)
  }

  static hasMany = [
    sites : ShelvingLocationSite,
  ]

  static mappedBy = [
    sites: 'shelvingLocation'
  ]

  static mapping = {
    table 'host_lms_shelving_loc'
                               id column : 'hlsl_id', generator: 'uuid2', length:36
                          version column : 'hlsl_version'
                             code column : 'hlsl_code'
                             name column : 'hlsl_name'
                      dateCreated column : 'hlsl_date_created'
                      lastUpdated column : 'hlsl_last_updated'
                 supplyPreference column : 'hlsl_supply_preference'
                           hidden column : 'hlsl_hidden', defaultValue: false
  }

  public String toString() {
    return "HostLMSShelvingLocation: ${code}".toString()
  }

    def canDelete() {
        def deleteValid = true;
        def error;

        // Is there an associated location site pointing at this record
        long numberShelvingLocationSites = ShelvingLocationSite.countByShelvingLocation(this);
        if (numberShelvingLocationSites > 0) {
            // There is at least 1 site that has this shelving location associated with it
            deleteValid = false;
            error = "There are " + numberShelvingLocationSites.toString() + " location sites attached to this shelving location";
        }

        return([
            deleteValid: deleteValid,
            error: error
        ]);
    }

    def beforeDelete() {
        return(canDelete().deleteValid);
    }
}
