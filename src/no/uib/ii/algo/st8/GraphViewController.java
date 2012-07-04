package no.uib.ii.algo.st8;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import no.uib.ii.algo.st8.algorithms.CutAndBridgeInspector;
import no.uib.ii.algo.st8.algorithms.DiameterInspector;
import no.uib.ii.algo.st8.algorithms.ExactDominatingSet;
import no.uib.ii.algo.st8.algorithms.ExactVertexCover;
import no.uib.ii.algo.st8.algorithms.GirthInspector;
import no.uib.ii.algo.st8.algorithms.GraphInformation;
import no.uib.ii.algo.st8.algorithms.MaximalClique;
import no.uib.ii.algo.st8.algorithms.Neighbors;
import no.uib.ii.algo.st8.algorithms.SpringLayout;
import no.uib.ii.algo.st8.settings.Geometric;
import no.uib.ii.algo.st8.start.Coordinate;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleGraph;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GraphViewController {

	private GraphView view;
	private SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;

	private SpringLayout layout = null;

	private Set<DefaultVertex> markedVertices = new HashSet<DefaultVertex>();
	private Set<DefaultVertex> userSelectedVertices = new HashSet<DefaultVertex>();
	private Set<DefaultEdge<DefaultVertex>> markedEdges = new HashSet<DefaultEdge<DefaultVertex>>();

	private DefaultVertex prevTouch;

	public final static float USER_MISS_RADIUS = 30;

	public GraphViewController(SuperTango8Activity activity,
			OnTouchListener listener) {

		graph = new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(
				new DefaultEdgeFactory<DefaultVertex>());

		view = new GraphView(activity);

		view.setOnClickListener(activity);
		view.setOnTouchListener(listener);
		// insertPetersen();
	}

	public SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> getGraph() {
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
	public DefaultVertex getClosestVertex(Coordinate coordinate, float radius) {
		Set<DefaultVertex> vertices = graph.vertexSet();
		if (vertices.isEmpty())
			return null;

		float bestDistance = radius;
		DefaultVertex bestVertex = null;

		// int debug_vertices_within_radi = 0;

		for (DefaultVertex currentVertex : vertices) {
			Coordinate pos = currentVertex.getCoordinate();
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

		for (DefaultVertex v : graph.vertexSet()) {
			Coordinate c = v.getCoordinate();
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
		for (DefaultVertex v : graph.vertexSet()) {
			Coordinate c = v.getCoordinate();
			xmin = Math.min(xmin, c.getX());
			xmax = Math.max(xmax, c.getX());

			ymin = Math.min(ymin, c.getY());
			ymax = Math.max(ymax, c.getY());
		}

		for (DefaultVertex v : graph.vertexSet()) {
			Coordinate c = v.getCoordinate();
			if (xmin < 0) {
				v.setCoordinate(c.add(new Coordinate(-xmin, 0)));
			}
			if (xmax > width) {
				v.setCoordinate(c.add(new Coordinate(width - xmax, 0)));
			}

			if (ymin < 0) {
				v.setCoordinate(c.add(new Coordinate(0, -ymin)));
			}
			if (ymax > height) {
				v.setCoordinate(c.add(new Coordinate(0, height - ymax)));
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

		Iterator<DefaultVertex> ite = userSelectedVertices.iterator();
		DefaultVertex s = ite.next();
		DefaultVertex t = ite.next();

		clearAll();

		DijkstraShortestPath<DefaultVertex, DefaultEdge<DefaultVertex>> dp = new DijkstraShortestPath<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph, s, t);

		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> path = dp
				.getPath();
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
	private void hightlightPath(
			GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> gp) {
		for (DefaultEdge<DefaultVertex> e : gp.getEdgeList()) {
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

		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> gp = DiameterInspector
				.diameterPath(graph);

		if (gp == null || gp.getEdgeList() == null)
			return -1;
		if (gp.getEdgeList().size() == 0)
			return 0;

		hightlightPath(gp);

		redraw();

		return gp.getEdgeList().size() + 1;
	}

	/**
	 * Returns negative number if acyclic.
	 * 
	 * @return girth of graph or -1 if acyclic.
	 */
	public int girth() {
		clearAll();
		int girth = GirthInspector.girth(graph);

		redraw();

		return girth;
	}

	public void showSpanningTree() {
		KruskalMinimumSpanningTree<DefaultVertex, DefaultEdge<DefaultVertex>> mst = new KruskalMinimumSpanningTree<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);
		Set<DefaultEdge<DefaultVertex>> spanning = mst.getEdgeSet();
		clearAll();
		markedEdges.addAll(spanning);
	}

	public boolean showCutVertex() {
		clearAll();
		DefaultVertex v = CutAndBridgeInspector.findCutVertex(graph);
		if (v == null)
			return false;
		markedVertices.add(v);
		return true;
	}

	public int showAllCutVertices() {
		clearAll();
		Set<DefaultVertex> cuts = CutAndBridgeInspector
				.findAllCutVertices(graph);
		markedVertices.addAll(cuts);
		return cuts.size();
	}

	public boolean showBridge() {
		clearAll();
		DefaultEdge<DefaultVertex> e = CutAndBridgeInspector.findBridge(graph);
		if (e == null)
			return false;
		markedEdges.add(e);
		return true;
	}

	public int showAllBridges() {
		clearAll();
		Set<DefaultEdge<DefaultVertex>> bridges = CutAndBridgeInspector
				.findAllBridges(graph);
		markedEdges.addAll(bridges);
		return bridges.size();
	}

	/**
	 * Inserts universal vertex, returns its degree, i.e. the size of graph
	 * before insertion.
	 * 
	 * @return degree of universal vertex (n-1)
	 */
	public int addUniversalVertex() {
		int deg = graph.vertexSet().size();

		Coordinate pos = new Coordinate(200, 200);
		if (deg != 0) {
			float x = 0;
			float y = 0;
			for (DefaultVertex v : graph.vertexSet()) {
				x += v.getCoordinate().getX();
				y += v.getCoordinate().getY();
			}
			pos = new Coordinate(x / deg + USER_MISS_RADIUS, y / deg
					+ USER_MISS_RADIUS);
		}

		System.out.println("Universal: " + pos + " (" + deg + ")");

		DefaultVertex universal = new DefaultVertex(pos);
		graph.addVertex(universal);
		for (DefaultVertex v : graph.vertexSet()) {
			if (v != universal)
				graph.addEdge(universal, v);
		}

		return deg;
	}

	public View getView() {
		return view;
	}

	/**
	 * Toggles edges between given vertex. Redraws as well!
	 * 
	 * @param v
	 *            vertex v
	 * @param u
	 *            vertex u
	 */
	private void toggleEdge(DefaultVertex v, DefaultVertex u) {
		DefaultEdge<DefaultVertex> edge = null;
		edge = graph.getEdge(v, u);
		if (edge != null) {
			graph.removeEdge(edge);
		} else {

			graph.addEdge(v, u);
		}

		redraw();
	}

	public void userClicked(Coordinate coordinate) {
		DefaultVertex hit = getClosestVertex(coordinate, USER_MISS_RADIUS);

		if (hit != null) {
			if (prevTouch != null) {
				if (prevTouch != hit) {
					toggleEdge(hit, prevTouch);
				} else {
					prevTouch = null;
				}
			} else {
				prevTouch = hit;
			}
		} else {
			if (prevTouch == null)
				graph.addVertex(new DefaultVertex(coordinate));
			prevTouch = null;
		}
		redraw();
	}

	public void userLongPress(Coordinate coordinate) {

		DefaultVertex hit = getClosestVertex(coordinate, USER_MISS_RADIUS);

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
		DefaultVertex hit = getClosestVertex(coordinate, USER_MISS_RADIUS);

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

		for (DefaultVertex vertex : graph.vertexSet()) {
			vertex.setCoordinate(vertex.getCoordinate().add(difference));
		}
		redraw();

	}

	public void fling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Coordinate from = new Coordinate(e1.getX(), e1.getY());
		Coordinate to = new Coordinate(e2.getX(), e2.getY());

		Coordinate move = from.moveVector(to);

		DefaultVertex fromVertex = getClosestVertex(from, USER_MISS_RADIUS);
		DefaultVertex toVertex = getClosestVertex(to, USER_MISS_RADIUS);

		if (fromVertex != null && toVertex != null && fromVertex != toVertex) {
			// someone tried to move a vertex onto another, let's make an edge
			toggleEdge(fromVertex, toVertex);
		} else if (fromVertex != null) {
			// we move a vertex

			if (userSelectedVertices.contains(fromVertex)) {
				// move all userselected
				for (Geometric v : userSelectedVertices) {
					v.setCoordinate(v.getCoordinate().add(move));
				}

			} else if (fromVertex == prevTouch) {
				// move neighborhood of prevtouch
				prevTouch.setCoordinate(prevTouch.getCoordinate().add(move));
				for (Geometric v : Neighbors.neighborhood(graph, prevTouch)) {
					v.setCoordinate(v.getCoordinate().add(move));
				}

			} else {
				// move only the hit vertex
				fromVertex.setCoordinate(fromVertex.getCoordinate().add(move));
			}
			redraw();
		} else {
			// user missed vertex, did user try to navigate?
			// We simply assume user tried to navigate

			// Coordinate difference = from.moveVector(to);
			moveView(move);
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

	public int showVertexCover() {
		Set<DefaultVertex> cover = ExactVertexCover.findExactVertexCover(graph);
		clearAll();
		markedVertices.addAll(cover);
		return cover.size();
	}

	public int showMaximumIndependentSet() {
		Set<DefaultVertex> cover = ExactVertexCover.findExactVertexCover(graph);
		clearAll();
		markedVertices.addAll(graph.vertexSet());
		markedVertices.removeAll(cover);
		return markedVertices.size();
	}

	public int showMaximumClique() {
		Set<DefaultVertex> clique = MaximalClique.findExactMaximumClique(graph);
		clearAll();
		markedVertices.addAll(clique);
		redraw();
		return clique.size();
	}

	public int showDominatingSet() {
		Set<DefaultVertex> domset = ExactDominatingSet
				.exactDominatingSet(graph);
		clearAll();
		markedVertices.addAll(domset);
		redraw();
		return domset.size();
	}

	// public void insertClique(int n) {
	// CenterPositioning cp = new CenterPositioning(n);
	// for (Coordinate c : cp.getPoints()) {
	// c = c.multiply(100);
	// c = c.add(new Coordinate(120, 200));
	// graph.createVertex(new StandardVertexConfiguration(c.add(c)));
	// }
	// for (DefaultVertex v : graph.vertexSet()) {
	// for (DefaultVertex u : graph.vertexSet()) {
	// if (u != v)
	// graph.createEdge(u, v, new StandardEdgeConfiguration());
	// }
	// }
	// redraw();
	// }

	// public void insertPetersen() {
	// CenterPositioning cp = new CenterPositioning(5);
	// List<DefaultVertex> vertices = new ArrayList<DefaultVertex>(10);
	//
	// // inner
	// for (Coordinate c : cp.getPoints()) {
	// // System.out.println(c.getX() + "," + c.getY());
	// c = c.multiply(100);
	// c = c.add(new Coordinate(220, 250));
	// vertices.add(graph.createVertex(new StandardVertexConfiguration(c)));
	// }
	//
	// // outer
	// for (Coordinate c : cp.getPoints()) {
	// // System.out.println(c.getX() + "," + c.getY());
	// c = c.multiply(200);
	// c = c.add(new Coordinate(220, 250));
	// vertices.add(graph.createVertex(new StandardVertexConfiguration(c)));
	// }
	//
	// redraw();
	// }

	// public void insertCycle(int n) {
	// CenterPositioning cp = new CenterPositioning(n);
	// List<DefaultVertex> vertices = new ArrayList<DefaultVertex>(n);
	// for (Coordinate c : cp.getPoints()) {
	// // System.out.println(c.getX() + "," + c.getY());
	// c = c.multiply(100);
	// c = c.add(new Coordinate(120, 200));
	// DefaultVertex ver = new DefaultVertex(c);
	// graph.addVertex(ver)
	// vertices.addVertex(ver);
	// }
	// for (int i = 0; i < n - 1; i++) {
	// graph.addEdge(vertices.get(i), vertices.get(i + 1));
	// }
	// graph.createEdge(vertices.get(n - 1), vertices.get(0),
	// new StandardEdgeConfiguration());
	// redraw();
	// }

	public String graphInfo() {
		int vertexCount = graph.vertexSet().size();
		if (vertexCount == 0) {
			return "The empty graph";
		}
		int edgeCount = graph.edgeSet().size();
		if (edgeCount == 0) {
			if (vertexCount == 1) {
				return "K1";
			} else {
				return "The trivial graph on " + vertexCount + " vertices";
			}
		}

		ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>> inspector = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);

		boolean isConnected = inspector.isGraphConnected();
		int nc = 1;
		if (!isConnected) {
			nc = inspector.connectedSets().size();
		}
		int maxDegree = GraphInformation.maxDegree(getGraph());
		int minDegree = GraphInformation.minDegree(getGraph());
		String s = "";
		s += (isConnected ? "Connected" : "Disconnected (" + nc
				+ " components)");
		s += " graph on " + vertexCount + " vertices";
		s += " and " + edgeCount + " edges.";
		if (maxDegree == minDegree) {
			if (maxDegree == vertexCount - 1) {
				s += " Complete, K_" + vertexCount;
			} else {
				s += " " + maxDegree + "-regular";
			}
		} else {
			s += " Max degree " + maxDegree + ", min degree " + minDegree;
		}
		return s;
	}

	public void redraw() {
		for (DefaultVertex v : graph.vertexSet()) {
			v.setColor(Color.RED);
			if (markedVertices.contains(v)) {
				v.setColor(Color.YELLOW);
			}
			if (userSelectedVertices.contains(v)) {
				v.setColor(Color.CYAN);
			}
			if (v.equals(prevTouch)) {
				v.setColor(Color.BLUE);
			}
		}
		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			e.setColor(Color.WHITE);
			if (markedEdges.contains(e)) {
				e.setColor(Color.GREEN);
			}
		}
		view.redraw(graphInfo(), graph);
	}
}
