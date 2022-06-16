package org.olf.rs.statemodel

/**
 * Holds a valid transition
 */
public class Transition {
	/** The status the request is at */
	Status fromStatus;

	/** The action or event that has been performed */
	ActionEvent actionEvent;

	/** The qualifier returned required to get us at this state */
	String qualifier;

	/** The ending state the request will be at */
	Status toStatus;
}
