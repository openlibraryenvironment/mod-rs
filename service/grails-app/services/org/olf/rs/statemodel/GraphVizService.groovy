package org.olf.rs.statemodel;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.GraphAttr.splines;
import static guru.nidi.graphviz.attribute.Rank.RankDir.TOP_TO_BOTTOM;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

import groovy.util.logging.Slf4j
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.GraphAttr.SplineMode;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.LinkTarget;

/**
 * Work in progress service to try and build chart of the transitions thqat can occur
 * @author Chas
 *
 */
@Slf4j
public class GraphVizService {

    private final static String[] DEFAULT_EVENT_TO_FROM_STATES = [ 'Event' ];

    StatusService statusService;

    /**
     * Generates a PNG image of the transitions with the status being the node and the link being the action
     * Documentation for graphviz can be found at https://graphviz.org/docs/attrs/label/
     * We are using the java implementation at https://github.com/nidi3/graphviz-java#user-content-api
     * Links in Green are the ones the users are able to perform
     * Links in yellow are events triggered by an action
     * Nodes in Red are completed states, completed states may loop back to the top through an event
     * @param stateModelCode The code for the state model that we are building the graph for
     * @param includeProtocolActions Do we include the protocol actions
     * @param excludeActions The actions that are to be excluded
     * @param outputStream The stream we need to write the diagram to
     * @param height The height of the image that is generated, not quite sure what the units are though (default is 0)
     */
    void generateGraph(String stateModelCode, Boolean includeProtocolActions, List<String> excludeActions, OutputStream outputStream, int height = 0) {
        try {
            // Find the list of tecompletedtates
            StateModel model = StateModel.lookup(stateModelCode);
            List<String> completedStates = getCompletedStates(model);
            ActionEvent[] actions = AvailableAction.getUniqueActionsForModel(model, excludeActions, includeProtocolActions);

            // Now build up the list of the links
            // First the actions
            List<LinkTarget> links = [];
            actions.each { action ->
                Transition[] transitions = statusService.possibleActionTransitionsForModel(model, action);
                BuildLinks(links, transitions, Color.PURPLE, Color.BLACK, completedStates);
            };

            // Now add in the events
            List<ActionEvent> events = ActionEvent.getEventsThatChangeStatus();
            Transition[] transitions = statusService.possibleEventTransitionsForModel(model, events);
            BuildLinks(links, transitions, Color.GREEN, Color.BLACK, completedStates);

            // Now we can define our graph
            Graph stateTransitionsGraph = graph('State Transitions')
                .directed()
                .graphAttr().with(Rank.dir(TOP_TO_BOTTOM))
                .graphAttr().with(splines(SplineMode.POLYLINE))
                .nodeAttr().with(Font.name('arial'))
                .linkAttr().with('class', 'link-class')
                .with(links);

    /* This is the one we used as an example to create what we needed
            Graph g = graph("State Transitions")
                .directed()
                .graphAttr().with(Rank.dir(TOP_TO_BOTTOM))
                .nodeAttr().with(Font.name("arial"))
                .linkAttr().with("class", "link-class")
                .with(
                    node("a").with(Color.RED).link(node("b")),
                    node("b").link(
                        to(node("c")).with(attr("weight", 5), attr("label", "chas"), Style.DASHED)
                    )
                )
    */

            try {
                Graphviz.fromGraph(stateTransitionsGraph).height(height).render(Format.DOT).toOutputStream(outputStream);
                Graphviz.fromGraph(stateTransitionsGraph).height(height).render(Format.DOT).toFile(new File('D:/Temp/graphviz/' + stateModelCode + '.dot'));
                Graphviz.fromGraph(stateTransitionsGraph).height(height).render(Format.SVG).toFile(new File('D:/Temp/graphviz/' + stateModelCode + '.svg'));
            //    Graphviz.fromGraph(stateTransitionsGraph).height(height).render(Format.PNG).toFile(new File('D:/Temp/graphviz/' + stateModelCode + '.png'));
            //    Graphviz.fromGraph(stateTransitionsGraph).render(Format.PNG).toOutputStream(outputStream);
            }
            catch (Exception e) {
                log.error("Exception thrown while building chart", e);
            }
        } catch (Exception e) {
            log.error("Exception thrown while generating chart", e);
        }
    }

    private List<String> getCompletedStates(StateModel model) {
        List<String> completedStates = new ArrayList<String>();
        StateModel.getCompletedStates(model.shortcode).each { status ->
            completedStates.add(status.code);
        }
        return(completedStates);
    }

    private void BuildLinks(List<LinkTarget> links, Transition[] transitions, Color linkLineColor, Color stateColour, List<String> completedStates) {
        // Must have From and To states
        if (transitions && (transitions.size() > 0)) {
            // We need to build a link between each from and to state and if the to node is completed we then make it RED
            transitions.each { transition ->
                String linkName = transition.actionEvent.code;
                if (transition.qualifier != null) {
                    linkName += "-" + transition.qualifier;
                }

                // Is this a completed, in which case we want a different colour
                Boolean iscompletedState =  completedStates.contains(transition.toStatus.code);

                // Initialise the colour of the line and shape of the TO node, shape of the FROM node is always box
                Color toNodeColor = stateColour;
                Color linkColor = linkLineColor;
                Shape toNodeShape = Shape.BOX;

                // If its a completed state TO node, then we override the colours and shape
                if (iscompletedState) {
                    toNodeColor = Color.RED;
                    linkColor = Color.RED;
                    toNodeShape = Shape.OVAL;
                }

                // Now we create the link between the from and to states
                links.add(node(transition.fromStatus.code).with(Shape.BOX).link(
                    to(node(transition.toStatus.code).with(toNodeShape).with(toNodeColor)).with(linkColor, attr('decorate', true), attr('weight', 5), attr('label', ''), attr('tooltip', linkName)).with(linkColor, attr('decorate', true), attr('weight', 5), attr('label', ''), attr('tooltip', linkName))
                ));
            }
        }
    }
}
