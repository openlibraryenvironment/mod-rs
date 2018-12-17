package com.k_int.folio.rs.models.ISO18626

public enum ErrorData {

	UNSUPPORTED_ACTION_TYPE("UnsupoortedActionType", "Error occured when procesing the request"),
	OK("OK", "The supplying library cannot handle the received message because the Action in Requesting Agency Message is not supported."),
	UNSUPPORTED_REASON_FOR_MESSAGE_TYPE("UnsupportedReasonforMessageType", "The requesting library cannot handle the received Agency Message because the ReasonforMessage in the Supplying Agency Message is not supported"),
	UNRECOGNISED_DATA_ELEMENT("UnrecognisedDataElement", "The supplying or requesting library has received a data element that it does not reconize."),
	UNRECONISED_DATA_VALUE("UnrecognisedDataValue", "For data elements with code lists, the receiving system may respond with an error message if the code or combination of codes is not reconized."),
	BADLY_FORMED_MESSAGE("BadlyFormedMessage", "The structure of the incoming message did not conform to the protocol schema definition and could not be interpreted by the receiving system")
}
