package no.uib.ii.algo.st8;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import no.uib.ii.algo.st8.algorithms.DiameterInspector;
import no.uib.ii.algo.st8.algorithms.ExactDominatingSet;
import no.uib.ii.algo.st8.algorithms.ExactVertexCover;
import no.uib.ii.algo.st8.algorithms.GirthInspector;
import no.uib.ii.algo.st8.algorithms.MaximalClique;
import no.uib.ii.algo.st8.algorithms.SpringLayout;
import no.uib.ii.algo.st8.start.CenterPositioning;
import no.uib.ii.algo.st8.start.Coordinate;
import no.uib.ii.algo.st8.start.UnEdge;
import no.uib.ii.algo.st8.start.UnVertex;
import no.uib.ii.algo.st8.start.VisualGraph;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GraphViewController {

	private GraphView view;
	private VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> graph;

	private SpringLayout layout;

	private Set<UnVertex> markedVertices = new HashSet<UnVertex>();
	private Set<UnVertex> userSelectedVertices = new HashSet<UnVertex>();
	private Set<UnEdge> markedEdges = new HashSet<UnEdge>();

	private UnVertex prevTouch;

	public final static float USER_MISS_RADIUS = 30;

	public GraphViewController(SuperTango8Activity activity,
			OnTouchListener listener) {

		graph = new VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration>();

		view = new GraphView(activity);

		view.setOnClickListener(activity);
		view.setOnTouchListener(listener);
		// insertPetersen();
	}

	public VisualGraph<StandardVertexConfiguration, StandardEdgeConfiguration> getGraph() {
		return graph;
	}

	/**
	 * Returns closest vertex to coordinate within the range of radius. Radius
	 * can be POSITIVE_INFINITY, in which case we accept any radius.
	 * 
	 * We return null if no such vertex exists.
	 * 
	 * If two vertices have exactly the same distance, one is chosen
	 * arbitrarily.
	 * 
	 * @param coordinate
	 * @param radius
	 * @return the vertex closest to coordinate constrained to radius, or null
	 */
	public UnVertex getClosestVertex(Coordinate coordinate, float radius) {
		Set<UnVertex> vertices = graph.getVertices();
		if (vertices.isEmpty())
			return null;

		float bestDistance = radius;
		UnVertex bestVertex = null;

		// int debug_vertices_within_radi = 0;

		for (UnVertex currentVertex : vertices) {
			Coordinate pos = graph.getVertexConfiguration(currentVertex)
					.getCoordinate();
			float currentDistance = pos.distance(coordinate);
			if (currentDistance < bestDistance) {
				bestVertex = currentVertex;
				bestDistance = currentDistance;
			}
			// if (currentDistance < radius)
			// debug_vertices_within_radi++;
		}
		return bestVertex;
	}

	private void fixPositions() {
		int height = view.getHeight();
		int width = view.getWidth();

		boolean sane = true;

		for (UnVertex v : graph.getVertices()) {
			Coordinate c = graph.getVertexConfiguration(v).getCoordinate();
			if (c.getX() < 0 || c.getX() > width) {
				sane = false;
				break;
			}
			if (c.getY() < 0 || c.getY() > height) {
				sane = false;
				break;
			}
		}

		if (sane)
			return;

		float xmin = width, xmax = 0, ymin = height, ymax = 0;
		for (UnVertex v : graph.getVertices()) {
			Coordinate c = graph.getVertexConfiguration(v).getCoordinate();
			xmin = Math.min(xmin, c.getX());
			xmax = Math.max(xmax, c.getX());

			ymin = Math.min(ymin, c.getY());
			ymax = Math.max(ymax, c.getY());
		}

		for (UnVertex v : graph.getVertices()) {
			Coordinate c = graph.getVertexConfiguration(v).getCoordinate();
			if (xmin < 0) {
				graph.getVertexConfiguration(v).setCoordinate(
						c.add(new Coordinate(-xmin, 0)));
			}
			if (xmax > width) {
				graph.getVertexConfiguration(v).setCoordinate(
						c.add(new Coordinate(width - xmax, 0)));
			}

			if (ymin < 0) {
				graph.getVertexConfiguration(v).setCoordinate(
						c.add(new Coordinate(0, -ymin)));
			}
			if (ymax > height) {
				graph.getVertexConfiguration(v).setCoordinate(
						c.add(new Coordinate(0, height - ymax)));
			}
		}

	}

	public void clearAll() {
		prevTouch = null;
		markedEdges.clear();
		userSelectedVertices.clear();
		markedVertices.clear();
	}

	/**
	 * Highlights path and returns length of path (number of vertices)
	 * 
	 * @return number of vertices or <0 if no path
	 */
	public int showPath() {
		if (userSelectedVertices.size() != 2) {
			return -1;
		}

		Iterator<UnVertex> ite = userSelectedVertices.iterator();
		UnVertex s = ite.next();
		UnVertex t = ite.next();

		clearAll();

		DijkstraShortestPath<UnVertex, UnEdge> dp = new DijkstraShortestPath<UnVertex, UnEdge>(
				graph.getGraph(), s, t);

		GraphPath<UnVertex, UnEdge> path = dp.getPath();
		if (path == null || path.getEdgeList() == null)
			return -1;
		if (path.getEdgeList().size() == 0)
			return 0;

		hightlightPath(path);

		redraw();

		return path.getEdgeList().size() + 1;
	}

	/**
	 * Adds edges and vertices to markedEdges and markedVertices
	 * 
	 * @param gp
	 */
	private void hightlightPath(GraphPath<UnVertex, UnEdge> gp) {
		for (UnEdge e : gp.getEdgeList()) {
			markedEdges.add(e);
			markedVertices.add(e.getSource());
			markedVertices.add(e.getTarget());
		}
	}

	/**
	 * Computes and highlight diameter path, returns diameter
	 * 
	 * @return diameter of graph, or -1 if infinite
	 */
	public int diameter() {

		clearAll();

		GraphPath<UnVertex, UnEdge> gp = DiameterInspector.diameterPath(graph
				.getGraph());

		if (gp == null || gp.getEdgeList() == null)
			return -1;
		if (gp.getEdgeList().size() == 0)
			return 0;

		hightlightPath(gp);

		redraw();

		return gp.getEdgeList().size() + 1;
	}

	public int girth() {
		clearAll();
		int g = GirthInspector.girth(graph.getGraph());

		redraw();

		return g;
	}

	public void showSpanningTree() {
		KruskalMinimumSpanningTree<UnVertex, UnEdge> mst = new KruskalMinimumSpanningTree<UnVertex, UnEdge>(
				graph.getGraph());
		Set<UnEdge> spanning = mst.getEdgeSet();
		clearAll();
		markedEdges.addAll(spanning);
	}

	public View getView() {
		return view;
	}

	public void userClicked(Coordinate coordinate) {
		StandardVertexConfiguration config = new StandardVertexConfiguration(
				coordinate);

		UnVertex hit = getClosestVertex(coordinate, USER_MISS_RADIUS);

		if (hit != null) {
			if (prevTouch != null) {
				if (prevTouch != hit)
					graph.createEdge(hit, prevTouch,
							new StandardEdgeConfiguration());
			} else {
				prevTouch = hit;
			}
		} else {
			if (prevTouch == null)
				graph.createVertex(config);
			prevTouch = null;
		}
		redraw();
	}

	public void userLongPress(Coordinate coordinate) {

		UnVertex hit = getClosestVertex(coordinate, USER_MISS_RADIUS);

		if (hit != null) {
			if (userSelectedVertices.contains(hit)) {
				userSelectedVertices.remove(hit);
			} else {
				userSelectedVertices.add(hit);
			}
		}
		redraw();
	}

	public boolean userDoubleTap(Coordinate coordinate) {
		UnVertex hit = getClosestVertex(coordinate, USER_MISS_RADIUS);

		if (hit != null) {
			graph.removeVertex(hit);
			clearAll();
			redraw();
			return true;
		}
		return false;
	}

	public void moveView(Coordinate difference) {
		// System.out.println("Redrawing, moving everything by " + difference);

		for (UnVertex vertex : graph.getVertices()) {
			StandardVertexConfiguration c = graph
					.getVertexConfiguration(vertex);
			c.setCoordinate(c.getCoordinate().add(difference));
		}
		redraw();

	}

	public void fling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Coordinate from = new Coordinate(e1.getX(), e1.getY());
		Coordinate to = new Coordinate(e2.getX(), e2.getY());

		UnVertex fromVertex = getClosestVertex(from, USER_MISS_RADIUS);

		if (fromVertex != null) {
			// we move a vertex
			StandardVertexConfiguration c = graph
					.getVertexConfiguration(fromVertex);
			c.setCoordinate(to);
			redraw();
		} else {
			// user missed vertex, did user try to navigate?
			fromVertex = getClosestVertex(from, 2 * USER_MISS_RADIUS);
			if (fromVertex != null)
				return;
			// user was quite far away from any vertex

			Coordinate difference = from.moveVector(to);
			moveView(difference);
		}
	}

	Coordinate dragCoordinate = null;

	public void dragStart(Coordinate c) {
		dragCoordinate = c;
	}

	public void dragTo(Coordinate c) {
		Coordinate diff = dragCoordinate.moveVector(c);
		dragCoordinate = c;
		moveView(diff);
	}

	public void longShake(int n) {
		if (layout == null)
			layout = new SpringLayout(graph);
		layout.iterate(n);
		fixPositions();
		redraw();
	}

	public void shake() {
		if (layout == null)
			layout = new SpringLayout(graph);
		layout.iterate();
		fixPositions();
		redraw();
	}

	public void showVertexCover() {
		Set<UnVertex> cover = ExactVertexCover.findExactVertexCover(graph
				.getGraph());
		if (cover != null) {
			clearAll();
			markedVertices.addAll(cover);
		}
	}

	public void showMaximumIndependentSet() {
		Set<UnVertex> cover = ExactVertexCover.findExactVertexCover(graph
				.getGraph());
		if (cover != null) {
			clearAll();
			markedVertices.addAll(graph.getVertices());
			markedVertices.removeAll(cover);
		}
	}

	public void showMaximumClique() {
		Set<UnVertex> clique = MaximalClique.findExactMaximumClique(graph
				.getGraph());
		clearAll();
		markedVertices.addAll(clique);
		redraw();
	}

	public void showDominatingSet() {
		Set<UnVertex> domset = ExactDominatingSet.exactDominatingSet(graph
				.getGraph());
		clearAll();
		markedVertices.addAll(domset);
		redraw();
	}

	public void insertClique(int n) {
		// TODO this should get a rectangle to draw it in, maybe?
		CenterPositioning cp = new CenterPositioning(n);
		for (Coordinate c : cp.getPoints()) {
			c = c.multiply(100);
			c = c.add(new Coordinate(120, 200));
			graph.createVertex(new StandardVertexConfiguration(c.add(c)));
		}
		for (UnVertex v : graph.getVertices()) {
			for (UnVertex u : graph.getVertices()) {
				if (u != v)
					graph.createEdge(u, v, new StandardEdgeConfiguration());
			}
		}
		redraw();
	}

	public void insertPetersen() {
		CenterPositioning cp = new CenterPositioning(5);
		List<UnVertex> vertices = new ArrayList<UnVertex>(10);

		// inner
		for (Coordinate c : cp.getPoints()) {
			// System.out.println(c.getX() + "," + c.getY());
			c = c.multiply(100);
			c = c.add(new Coordinate(220, 250));
			vertices.add(graph.createVertex(new StandardVertexConfiguration(c)));
		}

		// outer
		for (Coordinate c : cp.getPoints()) {
			// System.out.println(c.getX() + "," + c.getY());
			c = c.multiply(200);
			c = c.add(new Coordinate(220, 250));
			vertices.add(graph.createVertex(new StandardVertexConfiguration(c)));
		}

		redraw();
	}

	public void insertCycle(int n) {
		// TODO this should get a rectangle to draw it in, maybe?
		CenterPositioning cp = new CenterPositioning(n);
		List<UnVertex> vertices = new ArrayList<UnVertex>(n);
		for (Coordinate c : cp.getPoints()) {
			// System.out.println(c.getX() + "," + c.getY());
			c = c.multiply(100);
			c = c.add(new Coordinate(120, 200));
			vertices.add(graph.createVertex(new StandardVertexConfiguration(c)));
		}
		for (int i = 0; i < n - 1; i++) {
			graph.createEdge(vertices.get(i), vertices.get(i + 1),
					new StandardEdgeConfiguration());
		}
		graph.createEdge(vertices.get(n - 1), vertices.get(0),
				new StandardEdgeConfiguration());
		redraw();
	}

	public String graphInfo() {
		return graph.graphInfo();
	}

	public void redraw() {
		for (UnVertex v : graph.getVertices()) {
			StandardVertexConfiguration c = graph.getVertexConfiguration(v);
			c.setColor(Color.RED);
			if (markedVertices.contains(v)) {
				c.setColor(Color.YELLOW);
			}
			if (userSelectedVertices.contains(v)) {
				c.setColor(Color.CYAN);
			}
			if (v.equals(prevTouch)) {
				c.setColor(Color.BLUE);
			}
		}
		for (UnEdge e : graph.getEdges()) {
			StandardEdgeConfiguration c = graph.getEdgeConfiguration(e);
			c.setColor(Color.WHITE);
			if (markedEdges.contains(e)) {
				c.setColor(Color.GREEN);
			}
		}
		view.redraw(graphInfo(), graph);
	}
}
