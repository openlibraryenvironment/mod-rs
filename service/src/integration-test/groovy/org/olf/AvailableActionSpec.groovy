package org.olf

import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;

import grails.testing.mixin.integration.Integration;
import groovy.util.logging.Slf4j;
import spock.lang.Stepwise;

@Slf4j
@Integration
@Stepwise
class AvailableActionSpec extends TestBase {

    // This method is declared in the HttpSpec
    def setupSpecWithSpring() {
        super.setupSpecWithSpring();
    }

    def setupSpec() {
    }

    def setup() {
    }

    def cleanup() {
    }

    void "Set up test tenants"(String tenantId, String name) {
        when:"We post a new tenant request to the OKAPI controller"
            boolean response = setupTenant(tenantId, name);

        then:"The response is correct"
            assert(response);

        where:
            tenantId   | name
            TENANT_ONE | TENANT_ONE
    }

    void "Fetch the states we can reach from an action"(String tenantId, String stateModel, String actionEvent) {
        when:"Fetch the sttes we can transition to"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the states
            def response = doGet("${baseUrl}/rs/availableAction/toStates/" + stateModel + "/" + actionEvent);
            log.debug("Response from Get application: " + response.toString());

        then:"Check we have a valid response"
            // Check we have received the to states
            assert(response != null);
            assert(response.toStates != null);

        where:
            tenantId   | stateModel                 | actionEvent
            TENANT_ONE | StateModel.MODEL_REQUESTER | Actions.ACTION_REQUESTER_REQUESTER_CANCEL
            TENANT_ONE | StateModel.MODEL_RESPONDER | Actions.ACTION_RESPONDER_RESPOND_YES
    }

    void "Fetch the states an action can be perfrmed for the action"(String tenantId, String stateModel, String actionEvent) {
        when:"Fetch the sttes we can transition to"

            // Set the headers
            setHeaders([ 'X-Okapi-Tenant': tenantId ]);

            // Fetch the states
            def response = doGet("${baseUrl}/rs/availableAction/fromStates/" + stateModel + "/" + actionEvent);
            log.debug("Response from Get application: " + response.toString());

        then:"Check we have a valid response"
            // Check we have received the from states
            assert(response != null);
            assert(response.fromStates != null);

        where:
            tenantId   | stateModel                 | actionEvent
            TENANT_ONE | StateModel.MODEL_REQUESTER | Actions.ACTION_REQUESTER_REQUESTER_CANCEL
            TENANT_ONE | StateModel.MODEL_RESPONDER | Actions.ACTION_RESPONDER_RESPOND_YES
    }
}
