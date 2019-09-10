package org.olf.rs

enum NormalisedAvailability {

	/** The status of the item is unknown */
	Unknown,

	/** The item was unavailable */
	Unavailable,

	/** The item was available */
	Available,

	/** The item is currently checked out, but will be available after the specified date */
	AvailableFromDate
}
