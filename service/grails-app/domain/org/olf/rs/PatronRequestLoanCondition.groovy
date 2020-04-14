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

  /* The actual code sent along the ISO18626 message, such as "LibraryUseOnly", "SpecCollSupervReq" or "Other"
   * THIS MIGHT NOT BE HUMAN READABLE, and we might not have refdata corresponding to it in the receiver's system,
   * so a translation lookup has to be done when displaying on the request. In cases where we know the refdata exists
   * we can lookup that instead and use the label from there.
   */
  String code

  // Note passed along with the code, human readable content for explanation of user defined codes or extension of what the code says
  String note

  Symbol relevantSupplier


  static constraints = {
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    patronRequest (nullable: true)
    code( nullable: true)
    note( nullable: true)
    relevantSupplier (nullable: true, blank: false)
  }

  static mapping = {
                  id column : 'prlc_id', generator: 'uuid2', length:36
             version column : 'prlc_version'
         dateCreated column : 'prlc_date_created'
         lastUpdated column : 'prlc_last_updated'
                code column : 'prlc_code'
                note column : 'prlc_note'
       patronRequest column : 'prlc_patron_request_fk'
    relevantSupplier column : 'prlc_relevant_supplier_fk'
  }
}