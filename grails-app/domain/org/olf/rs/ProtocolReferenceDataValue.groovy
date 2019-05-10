package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue
import grails.gorm.annotation.Entity
import com.k_int.web.toolkit.databinding.BindUsingWhenRef

class ProtocolReferenceDataValue extends RefdataValue {

	static public final String CATEGORY_PUBLICATION_TYPE = "request.publicationType";
	static public final String CATEGORY_SERVICE_LEVEL    = "request.serviceLevel";
	static public final String CATEGORY_SERVICE_TYPE     = "request.serviceType";
	
	static hasMany = [protocolConversions : ProtocolConversion];
}
