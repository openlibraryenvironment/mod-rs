package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import grails.gorm.MultiTenant


class RequestVolume implements MultiTenant<RequestVolume> {

  String id

  String name
  String itemId

  PatronRequest patronRequest

  Date dateCreated
  Date lastUpdated
  
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
  }
}
