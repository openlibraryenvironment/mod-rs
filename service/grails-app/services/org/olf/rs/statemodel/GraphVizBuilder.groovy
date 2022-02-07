package org.olf.rs.statemodel;

import org.olf.rs.statemodel.AbstractAction;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.attribute.GraphAttr.SplineMode;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizV8Engine;
import guru.nidi.graphviz.model.*;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Rank.RankDir.TOP_TO_BOTTOM;
import static guru.nidi.graphviz.attribute.Rank.RankType.SAME;
import static guru.nidi.graphviz.attribute.Records.rec;
import static guru.nidi.graphviz.attribute.Records.turn;
import static guru.nidi.graphviz.engine.Format.SVG;
import static guru.nidi.graphviz.model.Compass.WEST;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Link.to;

import java.io.OutputStream;

public class GraphVizBuilder {

	private static String[] defaultEventToFromStates = [ "Event" ];
	 
	/**
	 * Builds a PNG image of the transitions with the status being the node and the link being the action
	 * Documentation for graphviz can be found at https://graphviz.org/docs/attrs/label/
	 * We are using the java implementation at https://github.com/nidi3/graphviz-java#user-content-api
	 * Links in Green are the ones the users are able to perform
	 * Links in yellow are events triggered by an action
	 * Nodes in Red are terminal states, terminal states may loop back to the top through an event
	 * @param stateModelCode The code for the state model that we are building the graph for
	 * @param actions The actions that are going to be covered by this diagram
	 * @param actions The actions that are going to be covered by this diagram
	 * @param outputStream The stream we need to write the diagram to
	 * @param height The height of the image that is generated, not quite sure what the units are though (default is 0)
	 */
	static void createGraph(String stateModelCode, List<AbstractAction> actions, List<AbstractEvent> events, OutputStream outputStream, int height = 0) {
		// Find the list of terminal states
		List<String> terminalStates = Status.getTerminalStates(stateModelCode);

		// Now build up the list of the links
		// First the actions
		List<LinkTarget> links = new ArrayList<LinkTarget>();
		actions.each { action ->
			String[] fromStates = action.fromStates(stateModelCode);
			String[] toStates = action.possibleToStates(stateModelCode);
			BuildLinks(links, action.name(), fromStates, toStates, Color.GREEN, Color.BLACK, terminalStates);
		};

		// Now add in the events
		events.each { event ->
			String[] fromStates = event.fromStates(stateModelCode);
			String[] toStates = event.possibleToStates(stateModelCode);
			if (fromStates && (fromStates.size() == 0)) {
				fromStates = defaultEventToFromStates;
			}
			if (toStates && (toStates.size() == 0)) {
				toStates = defaultEventToFromStates;
			}
			BuildLinks(links, event.name(), fromStates, toStates, Color.PURPLE, Color.BLACK, terminalStates);
		};

		// Now we can define our graph
		Graph stateTransitionsGraph = graph("State Transitions")
			.directed()
			.graphAttr().with(Rank.dir(TOP_TO_BOTTOM))
			.graphAttr().with(GraphAttr.splines(SplineMode.POLYLINE))
			.nodeAttr().with(Font.name("arial"))
			.linkAttr().with("class", "link-class")
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
			Graphviz.fromGraph(stateTransitionsGraph).height(height).render(Format.DOT).toFile(new File("D:/Temp/graphviz/" + stateModelCode + ".dot"));
			Graphviz.fromGraph(stateTransitionsGraph).height(height).render(Format.PNG).toFile(new File("D:/Temp/graphviz/" + stateModelCode + ".png"));
//			Graphviz.fromGraph(stateTransitionsGraph).render(Format.PNG).toOutputStream(outputStream);
		}
		catch (Exception e) {
			
		}
	}
	
	static void BuildLinks(List<LinkTarget> links, String linkName, String[] fromStates, String[] toStates, Color linkLineColor, Color stateColour, List<String> terminalStates) {
		// Must have From and To states
		if (fromStates && (fromStates.size() > 0) && toStates && (toStates.size() > 0)) {
			// We need to build a link between each from and to state and if the to node is terminal we then make it RED
			fromStates.each { fromState ->
				toStates.each { toState ->
					Boolean isTerminalState =  terminalStates.contains(toState);
					
					// Initialise the colour of the line and shape of the TO node, shape of the FROM node is always box
					Color toNodeColor = stateColour;
					Color linkColor = linkLineColor;
					Shape toNodeShape = Shape.BOX;
					
					// If its a terminal state TO node, then we override the colours and shape
					if (isTerminalState) {
						toNodeColor = Color.RED;
						linkColor = Color.RED;
						toNodeShape = Shape.OVAL;
					}
					 					
					// Now we create the link between the from and to states
					links.add(node(fromState).with(Shape.BOX).link(
						to(node(toState).with(toNodeShape).with(toNodeColor)).with(linkColor, attr("decorate", true), attr("weight", 5), attr("label", linkName))
					));
				}
			}
		}
	}
}
