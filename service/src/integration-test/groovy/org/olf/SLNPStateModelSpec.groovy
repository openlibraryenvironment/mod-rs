package org.olf


import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.StateModel
import org.olf.rs.statemodel.Status
import spock.lang.Stepwise

@Slf4j
@Integration
@Stepwise
class SLNPStateModelSpec extends TestBase {

  def setupSpecWithSpring() {
    super.setupSpecWithSpring();
  }

  def setupSpec() {}
  def setup() {}
  def cleanup() {}

  void "Set up test tenants"(String tenantId, String name) {
    when:"We post a new tenant request to the OKAPI controller"
    boolean response = setupTenant(tenantId, name);

    then:"The response is correct"
    assert(response);

    where:
    tenantId   | name
    TENANT_ONE | TENANT_ONE
  }

  void "Test SLNP StateModel requester state transition by executed action"(
          String tenantId,
          String requestTitle,
          String requestAuthor,
          String requestSystemId,
          String requestPatronId,
          String requestSymbol,
          String initialState,
          String resultState) {
      when: "When initial state transitions to action result state"

      // Define headers
      def headers = [
              'X-Okapi-Tenant': tenantId,
              'X-Okapi-Token': 'dummy',
              'X-Okapi-User-Id': 'dummy',
              'X-Okapi-Permissions': '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
      ]

      setHeaders(headers);

      // Create mock SLNP patron request
      PatronRequest slnpPatronRequest = new PatronRequest();

      // Set isRequester to true
      slnpPatronRequest.isRequester = true;

      // Create SLNP Requester State Model
      StateModel stateModel = new StateModel();
      stateModel.name = StateModel.MODEL_SLNP_REQUESTER;

      // Create Status with initial state and assign to StateModel
      Status initialStatus = new Status();
      initialStatus.code = initialState;
      stateModel.initialState = initialStatus

      // Set SLNP Requester StateModel and initial state
      slnpPatronRequest.stateModel = stateModel
      slnpPatronRequest.state = initialStatus;

      // Save the SLNP Patron request
      slnpPatronRequest = slnpPatronRequest.save(flush: true, failOnError: true);

      log.debug("Mocked SNLP Patron Request response: ${slnpPatronRequest} ID: ${slnpPatronRequest?.id}");

      // Validate initial state is correct
      RSLifecycleSpec.waitForRequestState(tenantId, 20000, requestPatronId, initialState);

      // Sleep
      Thread.sleep(2000);

      String jsonPayload = new File("src/integration-test/resources/scenarios/slnpRequesterSlnpISO18626Aborted.json").text;

      log.debug("jsonPayload: ${jsonPayload}");
      String performActionURL = "${baseUrl}/rs/patronrequests/${slnpPatronRequest.id}/performAction".toString();
      log.debug("Posting to performAction at $performActionURL");

      // Execute action
      doPost(performActionURL, jsonPayload);

      // Validate state transition
      RSLifecycleSpec.waitForRequestState(tenantId, 20000, requestPatronId, resultState);

      then: "Check values"
      assert true;

      where:
      tenantId    | requestTitle                      | requestAuthor | requestSystemId       | requestPatronId   | requestSymbol | initialState    | resultState
      'RSInstOne' | 'Excellent Argument, However...'  | 'Mom, U.R.'   | '1234-5678-9123-4567' | '9876-1234'       | 'ISIL:RST1'   | 'SLNP_REQ_IDLE' | 'SLNP_REQ_ABORTED'
  }
}
