package org.olf.rs.statemodel;

import grails.gorm.MultiTenant;
import grails.util.Holders;
import groovy.util.logging.Slf4j

/**
 * This class represents the actions and events that can occur in the system.
 * The difference between an action and an event is that an action occurs immediately where am event is queued,
 * so the result of an event is not returned to the caller
 */
@Slf4j
class ActionEvent implements MultiTenant<ActionEvent> {

    /** The services that actually do the work for the action / event */
    private static Map services = [ : ];

    // Query to find all the events that have a result list that changes the state
    private static final String EVENTS_CHANGE_STATUS_QUERY = 'from ActionEvent ae where isAction = false and exists (from ae.resultList.results r where r.status is not null)';

    /** The id of the record */
    String id;

    /** The code this action / event is known by */
    String code;

    /** A description of what this action / event does */
    String description;

    /** Is this an action or event */
    Boolean isAction;

    /** The default set of results to use for this action / event */
    ActionEventResultList resultList;

    /** can this action / event be undone */
    UndoStatus undoStatus;

    /** The name of the service (excluding the ptrfix of Action / Event and the postfix of service) that executes this action / event */
    String serviceClass;

    /** If the action can be both requester and responder and we need to differentiate, then this is the class for the responder */
    String responderServiceClass;

    static constraints = {
                         code (nullable: false, blank: false, unique: true)
                  description (nullable: false, blank: false)
                     isAction (nullable: false)
                   resultList (nullable: true)
                   undoStatus (nullable: true)
                 serviceClass (nullable: true, blank: false)
        responderServiceClass (nullable: true, blank: false)
    }

    static mapping = {
                           id column: 'ae_id', generator: 'uuid2', length: 36
                      version column: 'ae_version'
                         code column: 'ae_code', length: 64
                  description column: 'ae_description'
                     isAction column: 'ae_is_action'
                   resultList column: 'ae_result_list'
                   undoStatus column: 'ae_undo_status', length: 20
                 serviceClass column: 'ae_service_class', length: 64
        responderServiceClass column: 'ae_responder_service_class', length: 64
    }

    public static ActionEvent ensure(
        String code,
        String description,
        boolean isAction,
        String serviceClass,
        String resultListCode,
        UndoStatus undoStatus = UndoStatus.NO,
        String responderServiceClass = null
    ) {
        // Lookup to see if the code exists
        ActionEvent actionEvent = findByCode(code);

        // Did we find it
        if (actionEvent == null) {
            // No we did not, so create a new one
            actionEvent = new ActionEvent (
                code: code
            );
        }

        // Just update the other fields as something may have changed
        actionEvent.description = description;
        actionEvent.isAction = isAction;
        actionEvent.resultList = ActionEventResultList.lookup(resultListCode);
        actionEvent.serviceClass = serviceClass;
        actionEvent.undoStatus = undoStatus;
        actionEvent.responderServiceClass = responderServiceClass;

        // and save it
        actionEvent.save(flush:true, failOnError:true);

        // Return the actionEvent to the caller
        return(actionEvent);
    }

    public static ActionEvent lookup(String code) {
        ActionEvent actionEvent = null;
        if (code != null) {
            actionEvent = findByCode(code);
        }
        return(actionEvent);
    }

    public static List<ActionEvent> getEventsThatChangeStatus() {
        return(findAll(EVENTS_CHANGE_STATUS_QUERY));
    }

    public static def lookupService(String actionEventCode, boolean isRequester, def defaultService = null) {
        // Have we previously looked it up
        String key = actionEventCode + '~' + isRequester.toString();
        def result = services[key];
        if (result == null) {
            // No we have not
            ActionEvent actionEvent = lookup(actionEventCode);

            // Determine the service name
            String serviceClass = null;

            // Did we find a record
            if (actionEvent != null) {
                if ((actionEvent.responderServiceClass != null) && !isRequester) {
                    // This action is used for both requester and responder and we have a different service for the responder
                    serviceClass = actionEvent.responderServiceClass;
                } else {
                    // Simple scenario
                    serviceClass = actionEvent.serviceClass;
                }
            }

            // Did we find a service class
            if (serviceClass == null) {
                // We did not or the service class was not set, so return the default service
                result = defaultService;
            } else {
                // Determine the bean name, if we had a separate action table we could store it as a transient against that
                String beanName = (actionEvent.isAction ? "action" : "event") + serviceClass + "Service";

                // Now setup the link to the service that actually does the work
                try {
                    // Try and get hold of the service instance
                    result = Holders.grailsApplication.mainContext.getBean(beanName);
                } catch (Exception e) {
                    // This could be legitimate if we have been supplied a default service
                    if (defaultService == null) {
                        log.error("Unable to locate ActionEvent bean: " + beanName);
                    } else {
                        // Set the result to the default
                        result = defaultService;
                    }
                }

                // Update the services map
                services[key] = result;
            }
        }
        return(result);
    }
}
