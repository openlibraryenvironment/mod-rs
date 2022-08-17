package org.olf.rs

import grails.gorm.MultiTenant

class HostLMSItemLoanPolicy implements MultiTenant<HostLMSItemLoanPolicy> {

  String id;
  String code;
  String name;
  Date dateCreated;
  Date lastUpdated;
  boolean lendable = Boolean.TRUE;

  /** The hidden field if set to true, means they have tried to delete it but it is still linked to another record, so we just mark it as hidden */
  boolean hidden = Boolean.FALSE;

  static constraints = {
    code (nullable: false, unique: true)
    name (nullable: true)
    lendable (nullable: false)
    hidden (nullable: false)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
  }

  static mapping = {
    table 'host_lms_item_loan_policy'
                               id column : 'hlilp_id', generator: 'uuid2', length:36
                          version column : 'hlilp_version'
                             code column : 'hlilp_code'
                             name column : 'hlilp_name'
                         lendable column : 'hlilp_lendable', defaultValue: '1'
                           hidden column : 'hlilp_hidden', defaultValue: '0'
                      dateCreated column : 'hlilp_date_created'
                      lastUpdated column : 'hlilp_last_updated'
  }

  public String toString() {
    return "HostLMSItemLoanPolicy: ${code}".toString()
  }
}
