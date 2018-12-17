package com.k_int.folio.rs.models.ISO18626

import com.k_int.folio.rs.models.ISO18626.Types.Closed.Action;

public class ActiveSection {

	/** The action to be performed */
	Action action;

	/** Notes that may be relevant for this action */
	String notes;

	public ActiveSection(Action action, String notes) {
		this.action = action;
		this.notes = notes;
	}
}
