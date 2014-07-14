package no.uib.ii.algo.st8.algorithms;

import java.util.HashMap;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.util.Coordinate;
import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

public class LineGraphInspector extends
		Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>> {

	public LineGraphInspector(SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		super(graph);
	}

	@Override
	public SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> execute() {
		if (graphSize() == 0 || graphEdgeSize() == 0)
			return new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(graph.getEdgeFactory());

		return constructLineGraph();
	}

	private SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> constructLineGraph() {

		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> lg = new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph.getEdgeFactory());

		HashMap<DefaultVertex, DefaultEdge<DefaultVertex>> map = new HashMap<DefaultVertex, DefaultEdge<DefaultVertex>>();

		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			DefaultVertex v = getMidPoint(e.getSource(), e.getTarget());
			lg.addVertex(v);
			map.put(v, e);
		}

		for (DefaultVertex v1 : lg.vertexSet()) {
			for (DefaultVertex v2 : lg.vertexSet()) {
				if (v1 == v2 || lg.containsEdge(v1, v2))
					continue;
				DefaultEdge<DefaultVertex> e1 = map.get(v1);
				DefaultEdge<DefaultVertex> e2 = map.get(v2);

				if (Neighbors.isIncidentEdge(graph, e1, e2))
					lg.addEdge(v1, v2);
			}
		}

		return lg;
	}

	private DefaultVertex getMidPoint(DefaultVertex v1, DefaultVertex v2) {
		return new DefaultVertex(getMidpoint(v1.getCoordinate(), v2.getCoordinate()));
	}

	private Coordinate getMidpoint(Coordinate c1, Coordinate c2) {
		float x1 = c1.getX();
		float y1 = c1.getY();

		float x2 = c2.getX();
		float y2 = c2.getY();

		float x = (x1 + x2) / 2;
		float y = (y1 + y2) / 2;
		return new Coordinate(x, y);
	}
}
