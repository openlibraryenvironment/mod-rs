package org.olf.rs.statemodel;

/**
 * Signifies whether the undo action should be available for an action or event
 */
public enum UndoStatus {

    /** If the to and from status are the same then treat it as SKIP, otherwise treat it as a NO */
    DECIDE,

	/** The undo action should not be available */
	NO,

	/** The undo action is not relevant for this action / event */
	SKIP,

	/** The action / event can be undone */
	YES
}
