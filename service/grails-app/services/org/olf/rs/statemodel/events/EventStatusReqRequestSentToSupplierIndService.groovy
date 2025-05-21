package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

class EventStatusReqRequestSentToSupplierIndService extends AbstractEvent {

    SettingsService settingsService;

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        String requestRouterSetting = settingsService.getSettingValue(SettingsData.SETTING_ROUTING_ADAPTER);

        if (requestRouterSetting == "disabled") {
            String localSymbolsString = settingsService.getSettingValue(SettingsData.SETTING_LOCAL_SYMBOLS) ?: "";
            if (symbolPresent(request.supplyingInstitutionSymbol, localSymbolsString)) {
                log.debug("Symbol ${request.supplyingInstitutionSymbol} is local");
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCAL_REVIEW;
                eventResultDetails.auditMessage = 'Sent to local review';
            }
        }

        return(eventResultDetails);
    }

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_REQUEST_SENT_TO_SUPPLIER_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    public static Boolean symbolPresent(String symbol, String symbolListString) {
        if (!symbol) {
            return false;
        }
        List<String> parts = symbol.split(":",2);
        if (parts.size() != 2) {
            return false;
        }
        List<String> symbolList = symbolListString.split(',');
        for (String candidateSymbol : symbolList) {
            List<String> candidateParts = candidateSymbol.split(":", 2);
            if (candidateParts.size() != 2) {
                continue;
            }
            if (parts[0].equalsIgnoreCase(candidateParts[0]) && parts[1].equalsIgnoreCase(candidateParts[1])) {
                return true;
            }
        }
        return false;
    }
}
