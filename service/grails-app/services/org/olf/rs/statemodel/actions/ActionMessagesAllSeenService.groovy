package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestNotification;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

import com.k_int.web.toolkit.settings.AppSetting;

/**
 * Abstract action that marks all the messages as seen
 * @author Chas
 *
 */
public class ActionMessagesAllSeenService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_MESSAGES_ALL_SEEN);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        if (parameters.seenStatus == null) {
            actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
            actionResultDetails.auditMessage = 'No seenStatus supplied to mark as seen';
        } else {
            boolean seenStatus = parameters.seenStatus;
            boolean excluding = false;
            if (parameters.excludes) {
                excluding = parameters.excludes;
            }

            PatronRequestNotification[] messages = request.notifications;
            messages.each { message ->
                // Firstly we only want to be setting messages as read/unread that aren't already, and that we didn't send
                if (message.seen != seenStatus && !message.isSender) {
                    // Next we check if we care about the user defined settings
                    if (excluding) {
                        // Find the chat_auto_read AppSetting
                        AppSetting chatAutoRead = AppSetting.findByKey('chat_auto_read') ?: null;

                        // If the setting does not exist then assume we want to mark all as read
                        if (chatAutoRead) {
                            if (chatAutoRead.value) {
                                markAsReadLogic(message, chatAutoRead.value, seenStatus);
                            } else {
                                markAsReadLogic(message, chatAutoRead.defValue, seenStatus);
                            }
                        } else {
                            log.warn("Couldn't find chat auto mark as read setting, assuming needs to mark all as read");
                            message.seen = seenStatus;
                        }
                    } else {
                        // Sometimes we want to just mark all as read without caring about the user defined setting
                        message.seen = seenStatus;
                    }
                }
            }
        }

        // Ensure the response status is set
        actionResultDetails.responseResult.status = (actionResultDetails.result == ActionResult.SUCCESS);

        return(actionResultDetails);
    }

    protected void markAsReadLogic(PatronRequestNotification message, String valueKey, boolean seenStatus) {
        switch (valueKey) {
            case 'on':
                message.seen = seenStatus;
                break;

            case 'on_(excluding_action_messages)':
                if (message.attachedAction == 'Notification') {
                    message.seen = seenStatus;
                }
                break;

            case 'off':
                log.debug('chat setting off');
                break;

          default:
            // This shouldn't ever be reached
            log.error('Something went wrong determining auto mark as read setting');
        }
    }
}
