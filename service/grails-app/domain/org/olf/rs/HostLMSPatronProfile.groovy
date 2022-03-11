package org.olf.rs

import grails.gorm.MultiTenant

class HostLMSPatronProfile implements MultiTenant<HostLMSPatronProfile> {

  String id;
  String code;
  String name;
  Date dateCreated;
  Date lastUpdated;
  Boolean canCreateRequests;

  static constraints = {
    code (nullable: false)
    name (nullable: true)
    canCreateRequests (nullable: true)
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
                      dateCreated column : 'hlpp_date_created'
                      lastUpdated column : 'hlpp_last_updated'
  }

  public String toString() {
    return "HostLMSPatronProfile: ${code}".toString()
  }
}


