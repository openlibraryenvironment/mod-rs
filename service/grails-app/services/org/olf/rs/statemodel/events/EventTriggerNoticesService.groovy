package org.olf.rs.statemodel.events;

import org.olf.rs.PatronNoticeService;
import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;

import com.k_int.web.toolkit.refdata.RefdataValue;

/**
 * Base event class for notices
 * @author Chas
 *
 */
public abstract class EventTriggerNoticesService extends AbstractEvent {

    PatronNoticeService patronNoticeService;

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    public void triggerNotice(PatronRequest request, String trigger) {
        patronNoticeService.triggerNotices(request, RefdataValue.lookupOrCreate('noticeTriggers', trigger));
    }
}
