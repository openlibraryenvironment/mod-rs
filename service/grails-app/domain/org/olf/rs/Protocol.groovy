package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue

class Protocol extends RefdataValue {

	static hasMany = [protocolConversions : ProtocolConversion];
}
