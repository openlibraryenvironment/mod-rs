package org.olf.rs.statemodel;

/**
 * Says how to obtain the request from the data supplied with the event
 */
public enum EventFetchRequestMethod {

	/** The data has come from an external source, so we handle it in the event handler */
	HANDLED_BY_EVENT_HANDLER,
	
	/** A new request is to be created using eventData.bibliographicInfo */
	NEW,
	
	/** Use the delayedGet method with the id from eventData.payload.id */
	PAYLOAD_ID
}
