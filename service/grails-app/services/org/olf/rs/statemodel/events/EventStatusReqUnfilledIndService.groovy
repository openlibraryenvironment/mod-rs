package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * Event triggered by the supplier saying the request cannot be filled
 * @author Chas
 *
 */
public class EventStatusReqUnfilledIndService extends EventSendToNextLenderService {

    SettingsService settingsService;

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {

        String requestRouterSetting = settingsService.getSettingValue(SettingsData.SETTING_ROUTING_ADAPTER);
        if (requestRouterSetting == "disabled") {
            log.warn("Routing disabled, end of rota reached");
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_END_OF_ROTA;
            eventResultDetails.auditMessage = 'End of rota';
            return eventResultDetails;
        }

        return super.processEvent(request, eventData, eventResultDetails);
    }

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_UNFILLED_INDICATION);
    }
}
