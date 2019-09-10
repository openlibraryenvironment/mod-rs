package org.olf.rs

import grails.gorm.MultiTenant


class ShipmentItem implements MultiTenant<ShipmentItem> {

  String id

  //This will be used to track whether an item is being shipped out on loan or back as a return
  Boolean isReturning
  
  Date dateCreated
  Date lastUpdated
  
  static belongsTo = [
    shipment: Shipment,
    patronRequest: PatronRequest
    ];
  
  
  static constraints = {
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    shipment (nullable: false)
    patronRequest (nullable: false)
    isReturning (nullable: true)
  }
  
  static mapping = {
    id column : 'si_id', generator: 'uuid2', length:36
    version column : 'si_version'
    dateCreated column : 'si_date_created'
    lastUpdated column : 'si_last_updated'
    shipment column : 'si_shipment_fk'
    patronRequest column : 'si_patron_request_fk'
    isReturning : 'si_is_returning'
  }
}
