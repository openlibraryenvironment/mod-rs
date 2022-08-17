package org.olf.rs

import grails.gorm.MultiTenant;

class HostLMSPatronProfile implements MultiTenant<HostLMSPatronProfile> {

  String id;
  String code;
  String name;
  Date dateCreated;
  Date lastUpdated;
  Boolean canCreateRequests;

  /** The hidden field if set to true, means they have tried to delete it but it is still linked to another record, so we just mark it as hidden */
  Boolean hidden;

  static constraints = {
    code (nullable: false, unique: true)
    name (nullable: true)
    canCreateRequests (nullable: true)
    hidden (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
  }

  static mapping = {
    table 'host_lms_patron_profile'
                               id column : 'hlpp_id', generator: 'uuid2', length:36
                          version column : 'hlpp_version'
                             code column : 'hlpp_code'
                             name column : 'hlpp_name'
                canCreateRequests column : 'hlpp_can_create_requests'
                           hidden column : 'hlpp_hidden', defaultValue: false
                      dateCreated column : 'hlpp_date_created'
                      lastUpdated column : 'hlpp_last_updated'
  }

  public String toString() {
    return "HostLMSPatronProfile: ${code}".toString()
  }
}
