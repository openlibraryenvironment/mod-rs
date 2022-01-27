package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestNotification;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

import com.k_int.web.toolkit.settings.AppSetting;

public abstract class ActionMessagesAllSeenService extends AbstractAction {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		if (parameters.seenStatus == null) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'No seenStatus supplied to mark as seen';
		} else {
			boolean seenStatus = parameters.seenStatus;
			boolean excluding = false;
			if (parameters.excludes) {
			  excluding = parameters.excludes;
			}
			
			def messages = request.notifications
			messages.each{message ->
				// Firstly we only want to be setting messages as read/unread that aren't already, and that we didn't send
				if (message.seen != seenStatus && !message.isSender) {
					// Next we check if we care about the user defined settings
					if (excluding) {
						// Find the chat_auto_read AppSetting
						AppSetting chat_auto_read = AppSetting.findByKey('chat_auto_read')?: null;
			
					  	// If the setting does not exist then assume we want to mark all as read
					  	if (!chat_auto_read) {
						  	log.warn("Couldn't find chat auto mark as read setting, assuming needs to mark all as read")
							message.setSeen(seenStatus)
					  	} else {
						  	if (chat_auto_read.value) {
								markAsReadLogic(message, chat_auto_read.value, seenStatus);
							} else {
								markAsReadLogic(message, chat_auto_read.defValue, seenStatus);
							}
					  	}
					} else {
				  	    // Sometimes we want to just mark all as read without caring about the user defined setting
					    message.setSeen(seenStatus)
					}
				}
			}
		}

		// Ensure the response status is set
		actionResultDetails.responseResult.status = (actionResultDetails.result == ActionResult.SUCCESS ? true : false);
		
		return(actionResultDetails);
	}
	
	protected void markAsReadLogic(PatronRequestNotification message, String valueKey, boolean seenStatus) {
		switch (valueKey) {
			case 'on':
	        	message.setSeen(seenStatus);
				break;
				
			case 'on_(excluding_action_messages)':
	        	if (message.attachedAction == 'Notification') {
					message.setSeen(seenStatus);
				}
				break;
				
			case 'off':
	        	log.debug("chat setting off");
				break;
				
	      default:
	        // This shouldn't ever be reached
	        log.error("Something went wrong determining auto mark as read setting");
	    }
	}
}
