package org.olf.rs.workflow

import java.io.File;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestAudit;


abstract class AbstractAction {

	static private final Integer ONE_SECOND      = 1000;
	static private final Integer THIRTY_SECONDS  = 30 * ONE_SECOND;

		// The increment to be used when we are retrying
	static Integer retryIncrement = THIRTY_SECONDS;
	static private final int MAXIMUM_RETRY_COUNT = 16;

	protected enum ActionResponse {
		// An error has occurred, could be as simple as validation error, details will be in the audit
		ERROR,

		// This was a conditional action and we have a negative response
		NO,

		// This is for when we cannot carry out the action and therefore will retry a minute later
		RETRY,
		
		// We were successful or a positive response for a conditional
		SUCCESS
	}

	/** For determining the valid actions */
	def workflowService;

	static public int setSmallRetryIncrement() {
		retryIncrement = ONE_SECOND;
	}

	static public int setStandardRetryIncrement() {
		retryIncrement = THIRTY_SECONDS;
	}

	/** Returns the action that this class represents */	
	abstract Action getAction();

	/** Performs the action */
	abstract ActionResponse perform(PatronRequest requestToBeProcessed);


	public void execute(PatronRequest requestToBeProcessed) {
		long processingStartTime = System.currentTimeMillis();
		boolean errored = false;

		// Setup the audit record
		PatronRequestAudit patronRequestAudit = new PatronRequestAudit();
		patronRequestAudit.dateCreated = new Date();
		patronRequestAudit.fromStatus = requestToBeProcessed.state;
		patronRequestAudit.action = requestToBeProcessed.pendingAction;
		patronRequestAudit.patronRequest = requestToBeProcessed;
		requestToBeProcessed
		
		// The Next action that is to be executed
		Action nextAction = null;

		// Will we be performing a retry
		boolean performRetry = false;
	
		try {
			// Now perform the action
			switch (perform(requestToBeProcessed)) {
				case ActionResponse.SUCCESS:
					patronRequestAudit.toStatus = getAction().statusSuccessYes;
					break

				case ActionResponse.NO:
					patronRequestAudit.toStatus = getAction().statusSuccessNo;
					break;

				case ActionResponse.RETRY:
					// We will retry later
					performRetry = true;

					// Increment the number of retries
					if (requestToBeProcessed.numberOfRetries) {
						// Should probably do something more sensible when we reach the max retry count
						if (requestToBeProcessed.numberOfRetries < MAXIMUM_RETRY_COUNT) {
							requestToBeProcessed.numberOfRetries++;
						}
					} else {
						requestToBeProcessed.numberOfRetries = 1;
					}

					// Note: The next retry time increment is a power of 2 based on the number of retries, eg. 1, 2, 4, 8, 16
					requestToBeProcessed.delayPerformingActionUntil = new Date(System.currentTimeMillis() + (retryIncrement * 2.power(requestToBeProcessed.numberOfRetries + 1)));
					break;

				case ActionResponse.ERROR:
					errored = true;
					patronRequestAudit.toStatus = getAction().statusFailure;
					break;

				default:
					// Hit an error if we have reached here
					errored = true;
					break;
			}

			// No point looking up the next action for a retry as it will not change
			if (!performRetry && !errored) {
				// Now we have the new status, determine if we have a new action to perform
				nextAction = StateTransition.getNextAction(requestToBeProcessed.state, getAction(), patronRequestAudit.toStatus);
			}

		} catch (Exception e) {
			// Note: If an exception is thrown from another method and not caught within it
			// Then the entire transaction will get rolled back including any saves made after the exception
			// Will need to test whether a read only transaction service throwing an exception causes a rollback
			errored = true;
		}

		try {
			// If we have hit an error, save the current status and pending action
			if (errored) {
				nextAction = null;
				requestToBeProcessed.preErrorStatus = requestToBeProcessed.state;
				requestToBeProcessed.errorAction = requestToBeProcessed.pendingAction;
				if (patronRequestAudit.toStatus == null) {
					patronRequestAudit.toStatus = Status.get(Status.ERROR);
				}
			}
		
			// Set the status and pending action on the request and the audit trail, if a retry will not be performed
			if (!performRetry) {
				requestToBeProcessed.lastUpdated = new Date();
				requestToBeProcessed.pendingAction = nextAction;
				requestToBeProcessed.state = patronRequestAudit.toStatus;
				requestToBeProcessed.numberOfRetries = null;

				// Not forgetting to add the audit record
				patronRequestAudit.duration = System.currentTimeMillis() - processingStartTime;
				requestToBeProcessed.AddToAdudit(patronRequestAudit);
			}

			if (!requestToBeProcessed.save(flush : true)) {
				String errors = "\n";
				requestToBeProcessed.errors.each() {error ->
					errors += "\t" + error + "\n";
				}
				log.error("Error saving request " + requestToBeProcessed.id + errors);
			}
		} catch (Exception e) {
			// TODO: Log to the log file and see if we can generate an audit record to log this exception
			log.error("Excpetion thrown while processnig request: " + requestToBeProcessed.id + " for action: " + requestToBeProcessed.pendingAction.name, e);
		}
	}
}
