package no.uib.ii.algo.st8;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import no.uib.ii.algo.st8.algorithms.BandwidthInspector;
import no.uib.ii.algo.st8.algorithms.BipartiteInspector;
import no.uib.ii.algo.st8.algorithms.CenterInspector;
import no.uib.ii.algo.st8.algorithms.ConnectedVertexCover;
import no.uib.ii.algo.st8.algorithms.CutAndBridgeInspector;
import no.uib.ii.algo.st8.algorithms.DiameterInspector;
import no.uib.ii.algo.st8.algorithms.EulerianInspector;
import no.uib.ii.algo.st8.algorithms.ExactDominatingSet;
import no.uib.ii.algo.st8.algorithms.ExactVertexCover;
import no.uib.ii.algo.st8.algorithms.FeedbackVertexSet;
import no.uib.ii.algo.st8.algorithms.GirthInspector;
import no.uib.ii.algo.st8.algorithms.GraphInformation;
import no.uib.ii.algo.st8.algorithms.HamiltonianCycleInspector;
import no.uib.ii.algo.st8.algorithms.HamiltonianInspector;
import no.uib.ii.algo.st8.algorithms.MaximalClique;
import no.uib.ii.algo.st8.algorithms.OddCycleTransversal;
import no.uib.ii.algo.st8.algorithms.PowerGraph;
import no.uib.ii.algo.st8.algorithms.RegularityInspector;
import no.uib.ii.algo.st8.algorithms.SpringLayout;
import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultEdgeFactory;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.util.Coordinate;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleGraph;

import android.graphics.Color;
import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class GraphViewController {

	private GraphView view;
	private SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;

	private SpringLayout layout = null;

	private Set<DefaultVertex> markedVertices = new HashSet<DefaultVertex>();
	private Set<DefaultVertex> userSelectedVertices = new HashSet<DefaultVertex>();
	private Set<DefaultEdge<DefaultVertex>> markedEdges = new HashSet<DefaultEdge<DefaultVertex>>();

	private DefaultVertex prevTouch;

	// TODO this should depend on screen size and or zoom (scale of matrix)
	public final static float USER_MISS_RADIUS = 40;
	private final Coordinate CENTER_COORDINATE;

	// private DefaultVertex startedOnVertex;
	// private Coordinate startedOnCoordinate;

	public GraphViewController(Workspace activity, int width, int height) {

		graph = new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(
				new DefaultEdgeFactory<DefaultVertex>());

		view = new GraphView(activity);

		view.setOnClickListener(activity);
		view.setOnTouchListener(new View.OnTouchListener() {
			PrivateGestureListener gl = new PrivateGestureListener();
			GestureDetector gd = new GestureDetector(gl); // TODO deprecated!

			public boolean onTouch(View view, MotionEvent event) {
				return gd.onTouchEvent(event);
			}
		});

		CENTER_COORDINATE = new Coordinate(width / 2, height / 2);
	}

	public SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> getGraph() {
		return graph;

	}

	public Matrix getTransformMatrix() {
		return view.getTransformMatrix();
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

	// TODO This does not work after introducing the transformationMatrix
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

	/**
	 * Deselects all selected vertices and edges
	 */
	public void clearAll() {
		prevTouch = null;
		markedEdges.clear();
		userSelectedVertices.clear();
		markedVertices.clear();
	}

	/**
	 * Deselects all selected vertices and edges
	 */
	public void removeHighlight(Object obj) {
		if (prevTouch == obj)
			prevTouch = null;
		markedEdges.remove(obj);
		userSelectedVertices.remove(obj);
		markedVertices.remove(obj);
	}

	/**
	 * Tabula rasa, remove the graph and all we know.
	 */
	public void clear() {
		clearAll();
		graph = new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(
				new DefaultEdgeFactory<DefaultVertex>());
		layout = null;
	}

	public void selectAll() {
		if (isEmptyGraph())
			return;
		clearAll();
		userSelectedVertices.addAll(graph.vertexSet());
		redraw();
	}

	public void deselectAll() {
		if (isEmptyGraph())
			return;
		userSelectedVertices.clear();
		redraw();
	}

	public void selectAllHighlightedVertices() {
		if (isEmptyGraph())
			return;

		userSelectedVertices.addAll(markedVertices);
		markedVertices.clear();
		redraw();
	}

	/**
	 * Selects all vertices that are not selected and deselects those who are.
	 * 
	 */
	public void invertSelectedVertices() {
		if (isEmptyGraph())
			return;

		Set<DefaultVertex> select = new HashSet<DefaultVertex>(graph
				.vertexSet().size());
		for (DefaultVertex v : graph.vertexSet()) {
			if (!userSelectedVertices.contains(v)) {
				select.add(v);
			}
		}
		clearAll();
		userSelectedVertices = select;
		redraw();
	}

	/**
	 * Selects the set of all reachable vertices from the currently selected
	 * vertices.
	 * 
	 * 
	 * @return
	 */
	public Set<DefaultVertex> selectAllReachableVertices() {
		Set<DefaultVertex> reachable = new HashSet<DefaultVertex>(graph
				.vertexSet().size());
		Set<DefaultVertex> userselect = new HashSet<DefaultVertex>(
				userSelectedVertices);
		reachable.addAll(userSelectedVertices);
		clearAll();
		ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>> ci = new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);
		for (DefaultVertex v : userselect) {
			reachable.addAll(ci.connectedSetOf(v));
		}
		userSelectedVertices.addAll(reachable);
		redraw();
		return reachable;
	}

	/**
	 * Makes the selected vertices into a clique.
	 */
	public void completeSelectedVertices() {
		for (DefaultVertex v : userSelectedVertices) {
			for (DefaultVertex u : userSelectedVertices) {
				if (u != v && !graph.containsEdge(u, v)) {
					graph.addEdge(u, v);
				}
			}
		}
		redraw();
	}

	/**
	 * Between every pair of selected vertices, toggles edge.
	 * 
	 */
	public void complementSelected() {
		if (isEmptyGraph())
			return;
		if (userSelectedVertices == null || userSelectedVertices.size() == 0)
			return;

		ArrayList<DefaultVertex> vertices = new ArrayList<DefaultVertex>(graph
				.vertexSet().size());
		vertices.addAll(userSelectedVertices);

		for (int i = 0; i < vertices.size(); i++) {
			DefaultVertex v = vertices.get(i);
			for (int j = i + 1; j < vertices.size(); j++) {
				DefaultVertex u = vertices.get(j);
				toggleEdge(v, u);
			}
		}
		redraw();
	}

	/**
	 * Deletes the selected vertices.
	 * 
	 * @return Returns the number of vertices deleted.
	 */
	public int deleteSelectedVertices() {
		if (isEmptyGraph())
			return 0;
		int deleted = 0;

		for (DefaultVertex v : userSelectedVertices) {
			graph.removeVertex(v);
			deleted++;
		}
		clearAll();
		redraw();

		return deleted;
	}

	/**
	 * Deletes all vertices not selected.
	 * 
	 * @return number of vertices deleted.
	 */
	public int induceSubgraph() {
		invertSelectedVertices();
		return deleteSelectedVertices();
	}

	public boolean showHamiltonianPath() {
		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> hamPath = HamiltonianInspector
				.getHamiltonianPath(graph);

		clearAll();

		if (hamPath == null) {
			return false;
		}

		highlightPath(hamPath);
		redraw();
		return true;
	}

	public boolean showHamiltonianCycle() {
		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> hamCyc = HamiltonianCycleInspector
				.getHamiltonianCycle(graph);

		clearAll();

		if (hamCyc == null) {
			return false;
		}

		highlightPath(hamCyc);
		redraw();
		return true;
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

		highlightPath(path);

		redraw();

		return path.getEdgeList().size() + 1;
	}

	public void constructPower() {
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> power = PowerGraph
				.constructPowerGraph(graph);
		this.graph = power;
		redraw();
	}

	/**
	 * Adds edges and vertices to markedEdges and markedVertices
	 * 
	 * @param gp
	 */
	private void highlightPath(
			GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> gp) {
		for (DefaultEdge<DefaultVertex> e : gp.getEdgeList()) {
			markedEdges.add(e);
			markedVertices.add(e.getSource());
			markedVertices.add(e.getTarget());
		}
	}

	/**
	 * Adds edges and vertices to markedEdges and markedVertices
	 * 
	 * @param gp
	 */
	private void highlightGraph(
			SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> h) {
		for (DefaultEdge<DefaultVertex> e : h.edgeSet()) {
			DefaultVertex v = e.getSource();
			DefaultVertex u = e.getTarget();
			DefaultEdge<DefaultVertex> edge = graph.getEdge(v, u);
			if (edge != null) {
				markedEdges.add(edge);
			}
			markedVertices.add(v);
			markedVertices.add(u);
		}
	}

	public boolean isEmptyGraph() {
		if (graph == null) {
			new NullPointerException("Graph was null, from isEmptyGraph")
					.printStackTrace();
			graph = new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(
					new DefaultEdgeFactory<DefaultVertex>());
		}
		return graph.vertexSet().size() == 0;
	}

	/**
	 * Tests if graph is eulerian. If not, highlights all vertices of odd
	 * degree.
	 * 
	 * @return true if eulerian.
	 */
	public boolean isEulerian() {
		if (isEmptyGraph())
			return true;

		Set<DefaultVertex> odds = EulerianInspector.getOddDegreeVertices(graph);
		clearAll();
		markedVertices.addAll(odds);
		redraw();
		return odds.size() == 0;
	}

	/**
	 * Computes and highlight diameter path, returns diameter
	 * 
	 * @return diameter of graph, or -1 if infinite
	 */
	public int diameter() {
		if (isEmptyGraph()) {
			return -1;
		}
		clearAll();

		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> gp = DiameterInspector
				.diameterPath(graph);

		if (gp == null || gp.getEdgeList() == null)
			return -1;
		if (gp.getEdgeList().size() == 0)
			return 0;

		highlightPath(gp);

		redraw();

		return gp.getEdgeList().size() + 1;
	}

	/**
	 * Computes and highlight diameter path, returns diameter
	 * 
	 * @return diameter of graph, or -1 if infinite
	 */
	public boolean showBipartition() {
		if (isEmptyGraph()) {
			return true;
		}
		clearAll();

		Set<DefaultVertex> part = BipartiteInspector.getBipartition(graph);
		if (part == null) {
			redraw();
			return false;
		}

		markedVertices.addAll(part);

		redraw();
		return true;
	}

	/**
	 * Returns negative number if acyclic.
	 * 
	 * @return girth of graph or -1 if acyclic.
	 */
	public int girth() {
		if (isEmptyGraph()) {
			return -1;
		}
		clearAll();
		int girth = GirthInspector.girth(graph);

		redraw();

		return girth;
	}

	public void showSpanningTree() {
		if (isEmptyGraph()) {
			return;
		}

		KruskalMinimumSpanningTree<DefaultVertex, DefaultEdge<DefaultVertex>> mst = new KruskalMinimumSpanningTree<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);
		Set<DefaultEdge<DefaultVertex>> spanning = mst.getEdgeSet();
		clearAll();
		markedEdges.addAll(spanning);
	}

	/**
	 * Finds if there is a cut vertex, ie if there is a vertex whose removal
	 * increases the number of connected components
	 * 
	 * @return true iff there is a cut vertex
	 */
	public boolean showCutVertex() {
		if (isEmptyGraph()) {
			return false;
		}

		clearAll();
		DefaultVertex v = CutAndBridgeInspector.findCutVertex(graph);
		if (v == null)
			return false;
		markedVertices.add(v);
		return true;
	}

	/**
	 * Finds if there is a cut vertex, ie if there is a vertex whose removal
	 * increases the number of connected components, returns them all.
	 * 
	 * @return number of cut vertices
	 */
	public int showAllCutVertices() {
		if (isEmptyGraph()) {
			return 0;
		}

		clearAll();
		Set<DefaultVertex> cuts = CutAndBridgeInspector
				.findAllCutVertices(graph);
		markedVertices.addAll(cuts);
		return cuts.size();
	}

	/**
	 * Finds if there is an edge whose removal increases the number of connected
	 * components.
	 * 
	 * @return true iff there is a bridge
	 */
	public boolean showBridge() {
		if (isEmptyGraph()) {
			return false;
		}

		clearAll();
		DefaultEdge<DefaultVertex> e = CutAndBridgeInspector.findBridge(graph);
		if (e == null)
			return false;
		markedEdges.add(e);
		return true;
	}

	/**
	 * Finds if there is an edge whose removal increases the number of connected
	 * components.
	 * 
	 * @return number of bridges
	 */
	public int showAllBridges() {
		if (isEmptyGraph()) {
			return 0;
		}

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
		if (isEmptyGraph())
			return;

		if (graph.containsEdge(v, u)) {
			graph.removeEdge(v, u);
		} else {
			graph.addEdge(v, u);
		}

		redraw();
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

	public int showRegularityDeletionSet() {
		if (isEmptyGraph()) {
			return 0;
		}
		Set<DefaultVertex> regdel = RegularityInspector
				.regularDeletionSet(graph);
		clearAll();
		markedVertices.addAll(regdel);
		redraw();
		return regdel.size();
	}

	public int showOddCycleTransversal() {
		if (isEmptyGraph()) {
			return 0;
		}
		Collection<DefaultVertex> oct = OddCycleTransversal
				.findOddCycleTransversal(graph);
		clearAll();
		markedVertices.addAll(oct);
		redraw();
		return oct.size();
	}

	public int showFeedbackVertexSet() {
		if (isEmptyGraph()) {
			return 0;
		}
		Collection<DefaultVertex> fvs = FeedbackVertexSet
				.findExactFeedbackVertexSet(graph);
		clearAll();
		markedVertices.addAll(fvs);
		redraw();
		return fvs.size();
	}

	public int showVertexCover() {
		if (isEmptyGraph()) {
			return 0;
		}
		Set<DefaultVertex> cover = ExactVertexCover.findExactVertexCover(graph);
		clearAll();
		markedVertices.addAll(cover);
		return cover.size();
	}

	/**
	 * Finds connected vertex cover (cvc), highlights it and returns its size
	 * (order). If no CVC exists, e.g. there are two connected components
	 * containing edges, we return -1.
	 * 
	 * @return order of cvc, or -1 if none exists
	 */
	public int showConnectedVertexCover() {
		if (isEmptyGraph()) {
			return 0;
		}
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> cvc = ConnectedVertexCover
				.getConnectedVertexCover(graph);

		if (cvc == null)
			return -1;

		clearAll();

		highlightGraph(cvc);

		return cvc.vertexSet().size();
	}

	public int showMaximumIndependentSet() {
		if (isEmptyGraph()) {
			return 0;
		}
		Set<DefaultVertex> cover = ExactVertexCover.findExactVertexCover(graph);
		clearAll();
		markedVertices.addAll(graph.vertexSet());
		markedVertices.removeAll(cover);
		return markedVertices.size();
	}

	public int showMaximumClique() {
		if (isEmptyGraph()) {
			return 0;
		}
		Set<DefaultVertex> clique = MaximalClique.findExactMaximumClique(graph);
		clearAll();
		markedVertices.addAll(clique);
		redraw();
		return clique.size();
	}

	public int showDominatingSet() {
		if (isEmptyGraph()) {
			return 0;
		}
		Collection<DefaultVertex> domset = ExactDominatingSet
				.exactDominatingSet(graph);
		clearAll();
		markedVertices.addAll(domset);
		redraw();
		return domset.size();
	}

	public boolean showCenterVertex() {
		if (isEmptyGraph()) {
			return false;
		}
		clearAll();
		redraw();
		DefaultVertex center = CenterInspector.getCenter(graph);
		if (center == null)
			return false;
		markedVertices.add(center);
		redraw();
		return true;

	}

	public int computeBandwidth() {
		if (isEmptyGraph()) {
			return 0;
		}

		clearAll();
		int bandwidth = BandwidthInspector.computeBandwidth(graph);
		redraw();
		return bandwidth;
	}

	public void centralize() {
		if (isEmptyGraph()) {
			return;
		}
		DefaultVertex center = CenterInspector.getCenter(graph);
		if (center == null)
			return;
		Matrix transformMatrix = view.getTransformMatrix();
		Coordinate moveVector = center.getCoordinate().moveVector(
				CENTER_COORDINATE);
		transformMatrix.postTranslate(moveVector.getX(), moveVector.getY());
		redraw();
		return;
	}

	public String graphInfo() {
		isEmptyGraph(); // hack to test if null
		return GraphInformation.graphInfo(graph);
	}

	public void redraw() {
		if (isEmptyGraph()) {
			return;
		}

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

	/**
	 * Returns the coordinate the given point/coordinate on the screen
	 * represents in the graph
	 */
	private Coordinate translateCoordinate(Coordinate screenCoordinate) {

		float[] screenPoint = { screenCoordinate.getX(),
				screenCoordinate.getY() };
		Matrix invertedTransformMatrix = new Matrix();

		view.getTransformMatrix().invert(invertedTransformMatrix);
		invertedTransformMatrix.mapPoints(screenPoint);

		return new Coordinate(screenPoint[0], screenPoint[1]);
	}

	private class PrivateGestureListener extends SimpleOnGestureListener {
		private DefaultVertex touchedVertex = null;
		private int previousPointerCount = 0;
		private Coordinate[] previousPointerCoords = null;

		public boolean onDown(MotionEvent e) {
			Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
			Coordinate gCoordinate = translateCoordinate(sCoordinate);
			previousPointerCount = -1; // make any ongoing scroll restart

			if (e.getPointerCount() == 1) {
				touchedVertex = getClosestVertex(gCoordinate, USER_MISS_RADIUS);
			} else {
				touchedVertex = null;
			}
			return super.onDown(e);
		}

		public boolean onDoubleTap(MotionEvent e) {
			// TODO fix that a double tap actually hits same vertex both times
			Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
			Coordinate gCoordinate = translateCoordinate(sCoordinate);
			DefaultVertex hit = getClosestVertex(gCoordinate, USER_MISS_RADIUS);

			if (hit != null) {
				graph.removeVertex(hit);
				removeHighlight(hit);
				redraw();
				return true;
			} else {
				return false;
			}
		}

		public void onLongPress(MotionEvent e) {
			Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
			Coordinate gCoordinate = translateCoordinate(sCoordinate);
			DefaultVertex hit = getClosestVertex(gCoordinate, USER_MISS_RADIUS);

			if (hit != null) {
				if (userSelectedVertices.contains(hit)) {
					userSelectedVertices.remove(hit);
				} else {
					userSelectedVertices.add(hit);
				}
			}
			redraw();
		}

		public boolean onSingleTapUp(MotionEvent e) {
			if (e.getPointerCount() != 1)
				return false; // TODO Is this needed?
			if (touchedVertex == null && prevTouch == null) {
				Coordinate touchedCoord = new Coordinate(e.getX(), e.getY());
				Coordinate gCoordinate = translateCoordinate(touchedCoord);

				graph.addVertex(new DefaultVertex(gCoordinate));
			} else if (touchedVertex == null || prevTouch == touchedVertex) {
				prevTouch = null;
			} else if (prevTouch == null) {
				prevTouch = touchedVertex;
			} else {
				toggleEdge(touchedVertex, prevTouch);
			}

			touchedVertex = null;
			redraw();
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			switch (e2.getPointerCount()) {
			case 2:
				if (previousPointerCoords == null || previousPointerCount != 2) {
					previousPointerCoords = new Coordinate[2];
					previousPointerCoords[0] = new Coordinate(e2.getX(0),
							e2.getY(0));
					previousPointerCoords[1] = new Coordinate(e2.getX(1),
							e2.getY(1));
				} else {
					Coordinate[] newCoords = {
							new Coordinate(e2.getX(0), e2.getY(0)),
							new Coordinate(e2.getX(1), e2.getY(1)) };
					Coordinate VectorPrevious = previousPointerCoords[1]
							.subtract(previousPointerCoords[0]);
					Coordinate VectorNew = newCoords[1].subtract(newCoords[0]);
					float diffAngle = VectorNew.angle()
							- VectorPrevious.angle();
					float scale = VectorNew.length() / VectorPrevious.length();

					// the transformations
					view.getTransformMatrix().postTranslate(
							-previousPointerCoords[0].getX(),
							-previousPointerCoords[0].getY());
					view.getTransformMatrix().postRotate(diffAngle);
					view.getTransformMatrix().postScale(scale, scale);
					view.getTransformMatrix().postTranslate(
							newCoords[0].getX(), newCoords[0].getY());

					previousPointerCoords = newCoords;
				}
				break;
			case 1:
				previousPointerCoords = null;
				if (touchedVertex != null) {
					Coordinate sCoordinate = new Coordinate(e2.getX(),
							e2.getY());
					Coordinate gCoordinate = translateCoordinate(sCoordinate);
					touchedVertex.setCoordinate(gCoordinate);
				} else {
					if (previousPointerCount == 1)
						view.getTransformMatrix().postTranslate(-distanceX,
								-distanceY);
				}
				break;
			default: // 3 or more
				previousPointerCoords = null;
				previousPointerCount = e2.getPointerCount();
				return false;
			}
			previousPointerCount = e2.getPointerCount();
			redraw();
			return true;
		}
	}
}
