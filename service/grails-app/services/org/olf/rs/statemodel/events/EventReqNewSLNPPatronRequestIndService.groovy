package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.settings.AppSetting
import groovy.sql.Sql
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events
/**
 * This event service takes a new requester SLNP patron request and validates and generates HRID.
 */
public class EventReqNewSLNPPatronRequestIndService extends AbstractEvent {

    @Override
    String name() {
        return(Events.EVENT_REQUESTER_NEW_SLNP_PATRON_REQUEST_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request == null) {
            log.warn("Unable to locate request for ID ${eventData.payload.id} isRequester=${request?.isRequester} StateModel=${eventData.stateModel}");
            return(eventResultDetails);
        }

        // Generate a human readable ID to use
        if (!request.hrid) {
            request.hrid = generateHrid();
            log.debug("set request.hrid to ${request.hrid}");
        }

        return(eventResultDetails);
    }

    private String generateHrid() {
        String result = null;

        AppSetting prefixSetting = AppSetting.findByKey('request_id_prefix');
        log.debug("Got app setting ${prefixSetting} ${prefixSetting?.value} ${prefixSetting?.defValue}");

        String hridPrefix = prefixSetting.value ?: prefixSetting.defValue ?: '';

        // Use this to make sessionFactory.currentSession work as expected
        PatronRequest.withSession { session ->
            log.debug('Generate hrid');
            Sql sql = new Sql(session.connection())
            List queryResult  = sql.rows("select nextval('pr_hrid_seq')");
            log.debug("Query result: ${queryResult }");
            result = hridPrefix + queryResult [0].get('nextval')?.toString();
        }
        return(result);
    }
}
