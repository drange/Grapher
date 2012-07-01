package no.uib.ii.algo.st8.algorithms;

import java.util.HashMap;
import java.util.Map;

import no.uib.ii.algo.st8.StandardEdgeConfiguration;
import no.uib.ii.algo.st8.StandardVertexConfiguration;
import no.uib.ii.algo.st8.start.Coordinate;
import no.uib.ii.algo.st8.start.UnEdge;
import no.uib.ii.algo.st8.start.UnVertex;
import no.uib.ii.algo.st8.start.VisualGraph;

import org.jgrapht.alg.ConnectivityInspector;

/**
 * TODO: Should probably do this individually for connected components, and then
 * try placing them appropriately
 * 
 * @author pgd
 * 
 */
public class SpringLayout {

	public static final float SPRING_CONSTANT = .000001f;
	public static final float TIME_CONSTANT = 400f;

	private final VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> graph;
	private VisualGraph<SpringVertex, SpringEdge> layout;

	public SpringLayout(
			VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> graph) {
		this.graph = graph;
		initialize();
	}

	public void iterate() {

		if (!new ConnectivityInspector<UnVertex, UnEdge>(graph.getGraph())
				.isGraphConnected()) {
			return;
		}

		preprocess();
		doOneIteration();
		copyPositions();
	}

	public void iterate(int n) {

		if (!new ConnectivityInspector<UnVertex, UnEdge>(graph.getGraph())
				.isGraphConnected()) {
			return;
		}

		preprocess();
		for (int i = 0; i < n; i++) {
			doOneIteration();
		}
		copyPositions();
	}

	private void doOneIteration() {

		calculateRepulsion();
		calculateTension();
		move();
		resetNetForce();
	}

	private void resetNetForce() {
		for (UnVertex v : layout.getVertices()) {
			SpringVertex currentLayout = layout.getVertexConfiguration(v);
			currentLayout.netForce = Coordinate.ZERO;
		}
	}

	private void move() {
		for (UnVertex v : layout.getVertices()) {
			SpringVertex currentLayout = layout.getVertexConfiguration(v);
			currentLayout.position = currentLayout.position
					.add(currentLayout.netForce);

			currentLayout.position = currentLayout.position.rounded();
		}
	}

	private void calculateRepulsion() {
		for (UnVertex v : layout.getVertices()) {
			SpringVertex currentLayout = layout.getVertexConfiguration(v);
			for (UnVertex u : layout.getVertices()) {
				if (u != v) {
					Coordinate c1 = currentLayout.position;
					Coordinate c2 = layout.getVertexConfiguration(u).position;
					Coordinate force = repulsion(c1, c2);
					currentLayout.netForce = currentLayout.netForce.add(force);
				}
			}
		}
	}

	private void calculateTension() {
		for (UnEdge edge : layout.getEdges()) {
			UnVertex vertex1 = edge.getSource();
			UnVertex vertex2 = edge.getTarget();

			SpringVertex config1 = layout.getVertexConfiguration(vertex1);
			SpringVertex config2 = layout.getVertexConfiguration(vertex2);

			Coordinate pos1 = config1.position;
			Coordinate pos2 = config2.position;
			Coordinate force1 = tension(pos1, pos2);
			Coordinate force2 = force1.inverse();

			config1.netForce = config1.netForce.add(force1);
			config2.netForce = config2.netForce.add(force2);

		}
	}

	Map<UnVertex, UnVertex> fromGraphToLayout = new HashMap<UnVertex, UnVertex>();
	Map<UnVertex, UnVertex> fromLayoutToGraph = new HashMap<UnVertex, UnVertex>();

	private void initialize() {
		fromGraphToLayout.clear();
		fromLayoutToGraph.clear();
		layout = new VisualGraph<SpringLayout.SpringVertex, SpringLayout.SpringEdge>();
		for (UnVertex v : graph.getVertices()) {
			SpringVertex config = new SpringVertex(v,
					graph.getVertexConfiguration(v));
			UnVertex lv = layout.createVertex(config);
			fromGraphToLayout.put(v, lv);
			fromLayoutToGraph.put(lv, v);
		}
		for (UnEdge e : graph.getEdges()) {
			UnVertex source = e.getSource();
			UnVertex target = e.getTarget();

			layout.createEdge(fromGraphToLayout.get(source),
					fromGraphToLayout.get(target), new SpringEdge());
		}
	}

	/**
	 * Copies positions from original graph to layout
	 */
	private void preprocess() {
		// if (graph.getGraph().vertexSet().size() !=
		// layout.getVertices().size()) {
		initialize();
		// }
		// if (graph.getGraph().edgeSet().size() != layout.getEdges().size()) {
		// initialize();
		// }
		for (UnVertex v : layout.getVertices()) {
			UnVertex gv = fromLayoutToGraph.get(v);
			layout.getVertexConfiguration(v).position = graph
					.getVertexConfiguration(gv).getCoordinate().rounded();
		}
	}

	private Coordinate tension(Coordinate a, Coordinate b) {
		Coordinate forceDirection = a.moveVector(b);
		float dist = a.distance(b);
		float scalar = SPRING_CONSTANT * dist;
		return forceDirection.multiply(scalar).multiply(TIME_CONSTANT);
	}

	private Coordinate repulsion(Coordinate a, Coordinate b) {
		float dist = a.distance(b);
		if (dist == 0) {
			dist = 0.001f;
		}
		float scalar = 1f / (dist * dist);
		Coordinate forceDirection = a.moveVector(b).inverse();
		return forceDirection.multiply(scalar).multiply(TIME_CONSTANT);
		// return Coordinate.ZERO;
	}

	/**
	 * Copies positions to original graph
	 */
	private void copyPositions() {
		for (UnVertex v : layout.getVertices()) {
			graph.getVertexConfiguration(fromLayoutToGraph.get(v))
					.setCoordinate(layout.getVertexConfiguration(v).position);
		}
	}

	class SpringVertex {
		public SpringVertex(UnVertex vertex, StandardVertexConfiguration config) {
			this.position = config.getCoordinate();
			this.vertex = vertex;
		}

		UnVertex vertex;
		Coordinate position;
		Coordinate netForce = Coordinate.ZERO;
	}

	class SpringEdge {
	}

}
