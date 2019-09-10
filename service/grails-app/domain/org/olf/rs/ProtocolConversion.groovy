package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue

import grails.gorm.MultiTenant

class ProtocolConversion implements MultiTenant<ProtocolConversion> {

  /** internal ID of the conversion record */
  String id

	static belongsTo = [protocol       : Protocol,
		                referenceValue : ProtocolReferenceDataValue];

    /** The value the reference data will be converted to for the specified protocol */
    String conversionValue;
	
    static constraints = {
        conversionValue (nullable : false)
        protocol        (nullable : false)
        referenceValue  (nullable : false)
    }

    static mapping = {
        id              column : 'pc_id',              length : 36, generator : 'uuid2'
        version         column : 'pc_version'
        conversionValue column : 'pc_conversion_value', length : 255
        protocol        column : 'pc_protocol'
        referenceValue  column : 'pc_reference_value'
    }
}
