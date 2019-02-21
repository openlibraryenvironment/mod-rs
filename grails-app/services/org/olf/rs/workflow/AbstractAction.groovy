package org.olf.rs.workflow

import java.util.Date;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestAudit;


abstract class AbstractAction {

	static private final Integer ONE_SECOND      = 1000;
	static private final Integer THIRTY_SECONDS  = 30 * ONE_SECOND;

	// The increment to be used when we are retrying
	static Integer retryIncrement = THIRTY_SECONDS;
	static private final int MAXIMUM_RETRY_COUNT = 16;

	/** For Service used for sending requests to the ReShare queue */
	ReShareMessageService reShareMessageService;
	
	protected enum ActionResponse {
		// An error has occurred, could be as simple as validation error, details will be in the audit
		ERROR,

		// This was a conditional action and we have a negative response
		NO,

		// This is for when we cannot carry out the action and therefore will retry a minute later
		RETRY,
		
		// We were successful or a positive response for a conditional
		SUCCESS,

		// We have sent the message off to a protocol message queue and are awaiting a response
		IN_PROTOCOL_QUEUE
	}

	static public int setSmallRetryIncrement() {
		retryIncrement = ONE_SECOND;
	}

	static public int setStandardRetryIncrement() {
		retryIncrement = THIRTY_SECONDS;
	}

	/** Returns the code that represents this action */
	abstract String getActionCode();

	/** Returns the action that this class represents */	
	public Action getAction() {
		return(Action.get(getActionCode()));
	}

	/** Performs the action */
	abstract ActionResponse perform(PatronRequest requestToBeProcessed);

	public void execute(PatronRequest requestToBeProcessed) {
		// Shouldn't really have got here if we do not have a pending action
		if (requestToBeProcessed.pendingAction) {
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

						case ActionResponse.IN_PROTOCOL_QUEUE:
						// We stay at the same status
						patronRequestAudit.toStatus = patronRequestAudit.fromStatus;
						requestToBeProcessed.awaitingProtocolResponse = true;
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
				if (!requestToBeProcessed.awaitingProtocolResponse && !performRetry && !errored) {
					// Now we have the new status, determine if we have a new action to perform
					nextAction = StateTransition.getNextAction(requestToBeProcessed.state, getAction(), patronRequestAudit.toStatus, requestToBeProcessed.isRequester);
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
					requestToBeProcessed.state = patronRequestAudit.toStatus;
					requestToBeProcessed.numberOfRetries = null;

					// If we are waiting for a protocol response we do not reset the pending action, that will happen when we get the protocol response				
					if (!requestToBeProcessed.awaitingProtocolResponse) {
						requestToBeProcessed.pendingAction = nextAction;
					}

					// Not forgetting to add the audit record
					patronRequestAudit.duration = System.currentTimeMillis() - processingStartTime;
					requestToBeProcessed.addToAudit(patronRequestAudit);
				}

				// We do not want validation occuring on the pendingAction field
				requestToBeProcessed.systemUpdate = true;

				// We can now save
				if (!requestToBeProcessed.save(flush : true)) {
					String errors = "\n";
					requestToBeProcessed.errors.each() {error ->
						errors += "\t" + error + "\n";
					}
					log.error("Error saving request " + requestToBeProcessed.id + errors);
				}

				// If we have a next action add that into the queue
				if (nextAction != null) {
					// TODO: We need to put this request back onto the queue
				}

				// If we are performing a retry add it into the queue with a delay
				if (performRetry) {
					// TODO: Add it back into the queue with a delay
				}
			} catch (Exception e) {
				// TODO: Log to the log file and see if we can generate an audit record to log this exception
				log.error("Excpetion thrown while processnig request: " + requestToBeProcessed.id + " for action: " + patronRequestAudit.action.name, e);
			}
		}
	}
}
