package org.olf.rs

import org.olf.okapi.modules.directory.DirectoryEntry
import com.k_int.web.toolkit.refdata.RefdataValue
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import java.time.LocalDateTime
import grails.gorm.MultiTenant


class Shipment implements MultiTenant<Shipment> {

  String id
  DirectoryEntry shippingLibrary
  DirectoryEntry receivingLibrary
  RefdataValue shipmentMethod
  String trackingNumber
  RefdataValue status
  LocalDateTime shipDate
  LocalDateTime receivedDate
  
  Date dateCreated
  Date lastUpdated
  
  static hasMany = [shipmentItems: ShipmentItem];

  static mappedBy = [
    shipmentItems: 'shipment'
  ]

  static constraints = {
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    shippingLibrary (nullable: true)
    receivingLibrary (nullable: true)
    shipmentMethod (nullable: true)
    trackingNumber (nullable: true)
    status (nullable: true)
    shipDate (nullable: true)
    receivedDate(nullable: true)
    
  }
  
  static mapping = {
    id column : 'sh_id', generator: 'uuid2', length:36
    version column : 'sh_version'
    dateCreated column : 'sh_date_created'
    lastUpdated column : 'sh_last_updated'
    shippingLibrary column : 'sh_shipping_library_fk'
    receivingLibrary column : 'sh_receiving_library_fk'
    shipmentMethod column : 'sh_shipment_method_fk'
    trackingNumber column : 'sh_tracking_number'
    status column : 'sh_status_fk'
    shipDate column : 'sh_ship_date'
    receivedDate column : 'sh_received_date'
  }
}
