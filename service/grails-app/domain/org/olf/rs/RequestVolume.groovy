package org.olf.rs

import com.k_int.web.toolkit.refdata.Defaults
import com.k_int.web.toolkit.refdata.RefdataCategory
import com.k_int.web.toolkit.refdata.RefdataValue

import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant

class RequestVolume implements MultiTenant<RequestVolume> {

  String id

  String name
  String itemId

  PatronRequest patronRequest

  Date dateCreated
  Date lastUpdated

  /* 
    This allows us to check whether each item in turn has succeeded NCIP call
    -----SUPPLIER'S SIDE-----
    awaiting_lms_check_out ─┬─► lms_check_out_complete         ─┬─► awaiting_lms_check_in ─┬─► completed
                            └─► lms_check_out_(no_integration) ─┘                          └─► lms_check_in_(no_integration)

    -----REQUESTER'S SIDE-----    
    awaiting_temporary_item_creation ─┬─► temporary_item_created_in_host_lms
                                      └─► temporary_item_creation_(no_integration)
  */
  @Defaults([
    'Awaiting LMS check out', // Automatic
    'LMS check out complete', // Requires NCIP call
    'LMS check out (no integration)', // NCIP off -- deal with manually
    'Awaiting LMS check in', // Automatic
    'Completed',  // Requires NCIP call
    'LMS check in (no integration)', // NCIP off -- deal with manually

    'Awaiting temporary item creation', // Automatic
    'Temporary item created in host LMS', // Requires NCIP call
    'Temporary item creation (no integration)' // NCIP off -- deal with manually
  ])
  RefdataValue status

  static constraints = {
    itemId (blank: false)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
  }
  
  static mapping = {
               id column : 'rv_id', generator: 'uuid2', length:36
          version column : 'rv_version'
      dateCreated column : 'rv_date_created'
      lastUpdated column : 'rv_last_updated'
             name column : 'rv_name'
           itemId column : 'rv_item_id'
    patronRequest column : 'rv_patron_request_fk'
           status column : 'rv_status_fk'
  }
}
