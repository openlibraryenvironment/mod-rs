package org.olf.rs.statemodel.actions

import com.k_int.web.toolkit.refdata.RefdataValue
import org.olf.rs.PatronNoticeService
import org.olf.rs.PatronRequest
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

/**
 * Document has been successfully supplied
 *
 */
public class ActionSLNPNonReturnableRequesterSlnpRequesterReceivedService extends AbstractAction {
    PatronNoticeService patronNoticeService


    @Override
    String name() {
        return(Actions.ACTION_SLNP_REQUESTER_REQUESTER_RECEIVED)
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        patronNoticeService.triggerNotices(request, RefdataValue.lookupOrCreate('noticeTriggers', RefdataValueData.NOTICE_TRIGGER_DOCUMENT_DELIVERED))

        actionResultDetails.auditMessage = 'Document has been successfully supplied'

        return (actionResultDetails)
    }
}
