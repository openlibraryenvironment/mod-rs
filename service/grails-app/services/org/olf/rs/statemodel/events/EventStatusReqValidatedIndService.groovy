package org.olf.rs.statemodel.events;

import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.rs.AvailabilityStatement;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.RequestRouterService
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.routing.RankedSupplier;
import org.olf.rs.routing.RequestRouter;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * Event triggered when the request requires validation
 * @author Chas
 *
 */
public class EventStatusReqValidatedIndService extends AbstractEvent {

    RequestRouterService requestRouterService;
    SettingsService settingsService;

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_VALIDATED_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    // This takes a request with the state of VALIDATED and changes the state to REQ_SOURCING_ITEM,
    // and then on to REQ_SUPPLIER_IDENTIFIED if a rota could be established
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_SOURCING;
        eventResultDetails.auditMessage = 'Sourcing potential items';

        String requestRouterSetting = settingsService.getSettingValue(SettingsData.SETTING_ROUTING_ADAPTER);

        if (requestRouterSetting == "disabled") {
            eventResultDetails.qualifier = null;
            eventResultDetails.auditMessage = 'Request router disabled';
        }
        else if (request.rota?.size() != 0) {
            eventResultDetails.qualifier = null;
            eventResultDetails.auditMessage = 'Request supplied with Lending String';
        } else {
            Map operationData = [ : ];
            operationData.candidates = [];

            // We will shortly refactor this block to use requestRouterService to get the next block of requests
            RequestRouter selectedRouter = requestRouterService.getRequestRouter();

            if (selectedRouter == null) {
                throw new RuntimeException('Unable to locate router');
            }

            List<RankedSupplier> possibleSuppliers = selectedRouter.findMoreSuppliers(request.getDescriptiveMetadata(), []);

            // See if we have an app setting for lender of last resort
            AppSetting last_resort_lenders_setting = AppSetting.findByKey('last_resort_lenders');
            String last_resort_lenders = last_resort_lenders_setting?.value ?: last_resort_lenders_setting?.defValue;
            if ( last_resort_lenders && ( last_resort_lenders.length() > 0 ) ) {
                String[] additionals = last_resort_lenders.split(',');
                additionals.each { al ->
                    if ( ( al != null ) && ( al.trim().length() > 0 ) ) {
                        possibleSuppliers.add(new RankedSupplier(
                            supplier_symbol: al.trim(),
                            instance_identifier: null,
                            copy_identifier: null,
                            ill_policy: AvailabilityStatement.LENDABLE_POLICY
                        ));
                    }
                }
            }


            log.debug("Created ranked rota: ${possibleSuppliers}");

            if (possibleSuppliers.size() > 0) {
                int ctr = 0;

                // Pre-process the list of candidates
                possibleSuppliers?.each { rankedSupplier  ->
                    if (rankedSupplier .supplier_symbol != null) {
                        operationData.candidates.add([symbol:rankedSupplier .supplier_symbol, message:'Added']);
                        if (rankedSupplier.ill_policy == AvailabilityStatement.LENDABLE_POLICY) {
                            log.debug("Adding to rota: ${rankedSupplier }");

                            // Pull back any data we need from the shared index in order to sort the list of candidates
                            request.addToRota(new PatronRequestRota(
                                patronRequest : request,
                                rotaPosition : ctr++,
                                directoryId : rankedSupplier .supplier_symbol,
                                instanceIdentifier : rankedSupplier .instance_identifier,
                                copyIdentifier : rankedSupplier .copy_identifier,
                                loadBalancingScore : rankedSupplier .rank,
                                loadBalancingReason : rankedSupplier .rankReason
                            )
                        );
                    } else {
                            log.warn('ILL Policy was not Will lend');
                            operationData.candidates.add([symbol:rankedSupplier .supplier_symbol, message:"Skipping - illPolicy is \"${rankedSupplier .ill_policy}\""]);
                        }
                    } else {
                        log.warn('requestRouterService returned an entry without a supplier symbol');
                    }
                }

                // Processing
                eventResultDetails.qualifier = null;
                eventResultDetails.auditMessage = 'Ratio-Ranked lending string calculated by ' + selectedRouter.getRouterInfo()?.description;
            } else {
                log.error("Unable to identify any suppliers for patron request ID ${eventData.payload.id}")
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_END_OF_ROTA;
                eventResultDetails.auditMessage =  'Unable to locate lenders';
            }
        }
        return(eventResultDetails);
    }
}
