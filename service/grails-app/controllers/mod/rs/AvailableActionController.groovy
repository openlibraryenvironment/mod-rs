package mod.rs;

import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionService;
import org.olf.rs.statemodel.AvailableAction;
import org.olf.rs.statemodel.GraphVizBuilder;
import org.olf.rs.statemodel.StateModel;

import com.k_int.okapi.OkapiTenantAwareController;

import grails.converters.JSON;
import grails.gorm.multitenancy.CurrentTenant;
import grails.util.Holders;
import groovy.util.logging.Slf4j;

@Slf4j
@CurrentTenant
class AvailableActionController extends OkapiTenantAwareController<AvailableAction>  {

    ActionService actionService;

	AvailableActionController() {
		super(AvailableAction)
	}

	/**
	 * Gets hold of the states an action can be called from
	 * Example call: curl --http1.1 -sSLf -H "accept: application/json" -H "Content-type: application/json" -H "X-Okapi-Tenant: diku" --connect-timeout 10 --max-time 30 -XGET http://localhost:8081/rs/availableAction/toStates/Responder/respondYes
	 * @return the array of states the action can be called from
	 */
	def fromStates() {

		def result = [:]
		if (request.method == 'GET') {
			if (params.stateModel && params.actionCode) {
				AbstractAction actionBean = actionService.getServiceAction(params.actionCode, params.stateModel == StateModel.MODEL_REQUESTER);
				if (actionBean == null) {
					result.message = "Can find no class for the action " + params.actionCode + " for the state model " + params.stateModel;
				} else {
					result.fromStates = actionBean.fromStates(params.stateModel);
				}
			} else {
				result.message = "Need to supply both action and state model , to see what states this action could transition from";
			}
		} else {
			request.message("Only GET requests are supported");
		}
		render result as JSON;
    }

	/**
	 * Gets hold of the states an action can be called from
	 * Example call: curl --http1.1 -sSLf -H "accept: application/json" -H "Content-type: application/json" -H "X-Okapi-Tenant: diku" --connect-timeout 10 --max-time 30 -XGET http://localhost:8081/rs/availableAction/fromStates/Responder/respondYes
	 * @return the array of states the action can be called from
	 */
	def toStates() {

		def result = [:]
		if (request.method == 'GET') {
			if (params.stateModel && params.actionCode) {
				AbstractAction actionBean = actionService.getServiceAction(params.actionCode, params.stateModel == StateModel.MODEL_REQUESTER);
				if (actionBean == null) {
					result.message = "Can find no class for the action " + params.actionCode + " for the state model " + params.stateModel;
				} else {
					result.toStates = actionBean.possibleToStates(params.stateModel);
				}
			} else {
				result.message = "Need to supply both action and state model , to see what states this action could transition to";
			}
		} else {
			request.message("Only GET requests are supported");
		}
		render result as JSON;
    }

	/**
	 * Builds a graph of the state models actions
	 * Example call: curl --http1.1 -sSLf -H "accept: image/png" -H "X-Okapi-Tenant: diku" --connect-timeout 10 --max-time 300 -XGET http://localhost:8081/rs/availableAction/createGraph/PatronRequest?height=4000&excludeActions=requesterCancel,borrowerCheck
	 * @return The png file that is the graph
	 */
	def createGraph() {

		// The actions and events we will build a graph for
		List<AbstractAction> actions = new ArrayList<AbstractAction>();
		List<AbstractEvent> events = new ArrayList<AbstractEvent>();

		// Determine the appropriate list of actions
		Map<String, AbstractAction> abstractActions = Holders.grailsApplication.mainContext.getBeansOfType(AbstractAction);

		// Remove messagesAllSeen, messageSeen and message as they occur for all states
		// We also only want to keep those for the state model we are interested in
		String nameStartsWith = "action" + params.stateModel.capitalize();
		List<String> ignoredActions = ["messagesAllSeen", "messageSeen", "message"];
		if (params.excludeActions) {
			// They have specified some additional actions that should be ignored
			ignoredActions.addAll(params.excludeActions.split(","));
		}

		abstractActions.each{ beanName, abstractAction ->
			// Is this action for the right state model
			if (beanName.startsWith(nameStartsWith)) {
				// Is it one we will ignore
				if (!ignoredActions.contains(abstractAction.name())) {
					// It is an action we want to take notice off
					actions.add(abstractAction);
					log.info("Adding action: " + abstractAction.name());
				}
			}
		}

		// Determine the appropriate list of events
		Map<String, AbstractEvent> abstractEvents = Holders.grailsApplication.mainContext.getBeansOfType(AbstractEvent);

		// Is this event for this state model
		abstractEvents.each{ beanName, abstractEvent ->
			// Is this for the right state model
			if (abstractEvent.supportsModel(params.stateModel)) {
				events.add(abstractEvent);
			}
		}

		// Send it straight to the output stream
		OutputStream outputStream = response.getOutputStream();

		// Were we passed a height in the parameters
		int height = 2000;
		if (params.height) {
			try {
				height = params.height as int;
			} catch (Exception e) {
			}
		}
		// Tell it to build the graph, it should return the dot file in the output stream
		GraphVizBuilder.createGraph(params.stateModel, actions, events, outputStream, height);

		// Hopefully we have what we want in the output stream
		outputStream.flush();
		response.status = 200;
		response.setContentType("text/plain");
	}
}
