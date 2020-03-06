package org.olf.rs

import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import org.olf.okapi.modules.directory.Symbol;
import java.time.Instant;

class PatronRequestLoanCondition implements MultiTenant<PatronRequest> {
  
  // Internal id of the message
  String id

  static belongsTo = [patronRequest : PatronRequest]

  // Default date metadata maintained by the db
  Date dateCreated
  Date lastUpdated

  String code

  String note

  Symbol relevantSupplier


  static constraints = {
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    patronRequest (nullable: true)
    code( nullable: true)
    note( nullable: true)
    messageSender (nullable: true, blank: false)
  }

  static mapping = {
                  id column : 'prlc_id', generator: 'uuid2', length:36
             version column : 'prlc_version'
         dateCreated column : 'prlc_date_created'
         lastUpdated column : 'prlc_last_updated'
                code column : 'prlc_code'
                note column : 'prlc_note'
    relevantSupplier column : 'prlc_relevant_supplier_fk'
  }
}