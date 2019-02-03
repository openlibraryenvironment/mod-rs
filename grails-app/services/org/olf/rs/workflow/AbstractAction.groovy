package org.olf.rs.workflow

import java.io.File;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.olf.rs.PatronRequest


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

	/** For sending data to the client */
	def clientService;

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
			
		// TODO: I have removed the auditing / error recording I had previously, need to put in place something else
		 
		// The Next action that is to be executed
		Action nextAction = null;

		// The status we will be changing the request  being processed to
		Status newStatus = null;

		// Will we be performing a retry
		boolean performRetry = false;
	
		try {
			// Now perform the action
			switch (perform(requestToBeProcessed)) {
				case ActionResponse.SUCCESS:
					newStatus = getAction().statusSuccessYes;
					break

				case ActionResponse.NO:
					newStatus = getAction().statusSuccessNo;
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
					newStatus = getAction().statusFailure;
					break;

				default:
					// Need to set the status to error processing or something similar, need to look up the record as opposed to caching it as it is a multi tennant system
					newStatus = Status.GetByCode(Status.ERROR_PROCESSING);
					break;
			}

			// No point looking up the next action for a retry as it will not change
			if (!performRetry) {
				// Now we have the new status, determine if we have a new action to perform
				nextAction = StateTransition.getNextAction(requestToBeProcessed.Status, getAction(), newStatus);
			}

		} catch (Exception e) {
			// Note: If an exception is thrown from another method and not caught within it
			// Then the entire transaction will get rolled back including any saves made after the exception
			// Will need to test whether a read only transaction service throwing an exception causes a rollback
			newStatus = Status.GetByCode(Status.ERROR_PROCESSING);
			nextAction = null;
		}

		try {
			// Set the status and pending action on the request and the audit trail, if a retry will not be performed
			if (!performRetry) {
				requestToBeProcessed.lastUpdated = new Date();
				requestToBeProcessed.pendingAction = nextAction;
				requestToBeProcessed.Status = newStatus;
				requestToBeProcessed.numberOfRetries = null;

				// Not forgetting abut the audit
	
				// Save the request, this should save all the errors and audit record as well
				long duration = System.currentTimeMillis() - processingStartTime;
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
		}
	}

	/** Queues up a batch of requests to perform this action */	
	public Map batchQueue(List<String> requestIds) {
		// We return a map of list of request ids for each error
		Map errors = [ : ];

		// Process each request
		requestIds.each() { String requestId ->
			PatronRequest requestToBeProcessed = PatronRequest.get(requestId);

			// Queue the request
			Map error = queue(requestToBeProcessed);

			// Obtain the error description as we will use that as the key to the map
			String errorDescription = error.get(Error.ERROR_DESCRIPTION);
			List errorIds = errors.get(errorDescription);
			if (!errorIds) {
				// Not seen this error before, so create  anew array and map it by the description
				errorIds = [ ];
				errors.putAt(errorDescription, errorIds);
			}

			// Add the request to the array that is associated with this error
			errorIds.add(requestId);
		}
		return(errors);
	}
	
	/** Queues the file up into performing this action */	
	public Map queue(PatronRequest requestToBeProcessed) {
		// Set up our return structure
		Map error = getNoErrorResponse();

		if (requestToBeProcessed) {
			// If we already an action queued, then reject the action
			if (requestToBeProcessed.pendingAction == null) {
				// is this a valid action for the current status
				if (workflowService.isValidAction(requestToBeProcessed.Status, getAction())) {
					// It is a valid action, so update the pending action
					requestToBeProcessed.pendingAction = getAction();
					requestToBeProcessed.save(flush : true);
				} else {
					error = Error.actionNotValidForstatus.buildMessage([requestToBeProcessed.Status.name, getAction().name]);
				}
			} else {
				error = Error.actionAlreadyQueued.buildMessage([requestToBeProcessed.pendingAction.name]);
			}
		} else {
			error = Error.noRecordSupplied.buildMessage();
		}
		return(error);
	}
}
