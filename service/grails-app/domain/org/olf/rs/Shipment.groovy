package org.olf.rs

import org.olf.okapi.modules.directory.DirectoryEntry
import com.k_int.web.toolkit.refdata.RefdataValue
import java.time.LocalDateTime

class Shipment {
  String id
  DirectoryEntry directoryEntry
  RefdataValue shipmentMethod
  RefdataValue status
  LocalDateTime shipDate
  LocalDateTime receivedDate
  
  Date dateCreated
  Date lastUpdated
  
  static hasMany = [shipmentItems: ShipmentItem];

  static constraints = {
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    directoryEntry (nullable: true)
    shipmentMethod (nullable: true)
    status (nullable: true)
    shipDate (nullable: true)
    receivedDate(nullable: true)
    
  }
  
  static mapping = {
    id column : 'sh_id', generator: 'uuid2', length:36
    version column : 'sh_version'
    dateCreated column : 'sh_date_created'
    lastUpdated column : 'sh_last_updated'
    directoryEntry column : 'sh_directory_entry_fk'
    shipmentMethod column : 'sh_shipment_method_fk'
    status column : 'sh_status_fk'
    shipDate column : 'sh_ship_date_fk'
    receivedDate column : 'sh_received_date_fk'
  }
}
