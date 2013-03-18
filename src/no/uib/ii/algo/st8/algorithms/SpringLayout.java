package no.uib.ii.algo.st8.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultEdgeFactory;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.settings.Geometric;
import no.uib.ii.algo.st8.util.Coordinate;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

/**
 * @author pgd
 */
public class SpringLayout {

	/** This spring's constant, see Hooke's law */
	public static final float SPRING_CONSTANT = .000002f; // or 1?

	/** How much time "passes" between iterations */
	public static final float TIME_CONSTANT = 400f; // or 300?

	/**
	 * The most a vertex is allowed to move during one iteration. If net force
	 * is greater, we scale it down to this value.
	 */
	public static final float MAX_MOVEMENT = 50; // or 100?

	private final SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;
	private SimpleGraph<SpringVertex, DefaultEdge<SpringVertex>> layout;

	private Map<DefaultVertex, Integer> vertexToComponent;

	public SpringLayout(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph) {
		this.graph = graph;
		vertexToComponent = new HashMap<DefaultVertex, Integer>();
		initialize();
	}

	public void iterate() {
		preprocess();
		doOneIteration();
		copyPositions();
	}

	public void iterate(int n) {
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
		for (SpringVertex v : layout.vertexSet()) {
			v.netForce = Coordinate.ZERO;
		}
	}

	private void move() {
		for (SpringVertex v : layout.vertexSet()) {
			// If net force is too large, we scale down to MAX_MOVEMENT
			if (v.netForce.length() > MAX_MOVEMENT) {
				Coordinate unit = v.netForce.normalize();
				v.netForce = unit.multiply(MAX_MOVEMENT);
			}
			v.position = v.position.add(v.netForce);
			v.position = v.position.rounded();
		}
	}

	private void calculateRepulsion() {
		for (SpringVertex v : layout.vertexSet()) {
			for (SpringVertex u : layout.vertexSet()) {
				if (u != v && u.sameComponent(v)) {
					Coordinate c1 = v.position;
					Coordinate c2 = u.position;
					Coordinate force = repulsion(c1, c2);
					v.netForce = v.netForce.add(force);
				}
			}
		}
	}

	/**
	 * Calculates how much two adjacent vertices attract each other. Uses
	 * Hooke's law, with SPRING_CONSTANT.
	 */
	private void calculateTension() {
		for (DefaultEdge<SpringVertex> edge : layout.edgeSet()) {
			SpringVertex vertex1 = edge.getSource();
			SpringVertex vertex2 = edge.getTarget();

			Coordinate pos1 = vertex1.position;
			Coordinate pos2 = vertex2.position;

			Coordinate force1 = tension(pos1, pos2);
			Coordinate force2 = force1.inverse();

			vertex1.netForce = vertex1.netForce.add(force1);
			vertex2.netForce = vertex2.netForce.add(force2);

		}
	}

	Map<Geometric, SpringVertex> fromGraphToLayout = new HashMap<Geometric, SpringVertex>();
	Map<SpringVertex, Geometric> fromLayoutToGraph = new HashMap<SpringVertex, Geometric>();

	private void initialize() {
		fromGraphToLayout.clear();
		fromLayoutToGraph.clear();
		vertexToComponent.clear();

		// computes which connected components the different vertices belong to.
		ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>> ci = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);
		List<Set<DefaultVertex>> ccs = ci.connectedSets();
		for (int i = 0; i < ccs.size(); i++) {
			for (DefaultVertex v : ccs.get(i)) {
				vertexToComponent.put(v, i + 1);
			}
		}

		layout = new SimpleGraph<SpringVertex, DefaultEdge<SpringVertex>>(
				new DefaultEdgeFactory<SpringVertex>());

		for (Geometric v : graph.vertexSet()) {
			SpringVertex sp = new SpringVertex(v, vertexToComponent.get(v));
			layout.addVertex(sp);
			fromGraphToLayout.put(v, sp);
			fromLayoutToGraph.put(sp, v);
		}

		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			Geometric source = e.getSource();
			Geometric target = e.getTarget();

			layout.addEdge(fromGraphToLayout.get(source),
					fromGraphToLayout.get(target));
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
		for (SpringVertex v : layout.vertexSet()) {
			Geometric gv = fromLayoutToGraph.get(v);
			v.position = gv.getCoordinate().rounded();
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
		for (Geometric v : layout.vertexSet()) {
			fromLayoutToGraph.get(v).setCoordinate(v.getCoordinate());
		}
	}

	class SpringVertex implements Geometric {
		final Geometric vertex;
		final int component;
		Coordinate position;
		Coordinate netForce = Coordinate.ZERO;

		public SpringVertex(Geometric vertex, int component) {
			this.position = vertex.getCoordinate();
			this.vertex = vertex;
			this.component = component;
		}

		public Coordinate getCoordinate() {
			return position;
		}

		public void setCoordinate(Coordinate coordinate) {
			this.position = coordinate;
		}

		boolean sameComponent(SpringVertex other) {
			return component == other.component;
		}

	}
}
