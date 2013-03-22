package no.uib.ii.algo.st8;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import no.uib.ii.algo.st8.algorithms.Algorithm;
import no.uib.ii.algo.st8.algorithms.BalancedSeparatorInspector;
import no.uib.ii.algo.st8.algorithms.BandwidthInspector;
import no.uib.ii.algo.st8.algorithms.BipartiteInspector;
import no.uib.ii.algo.st8.algorithms.CenterInspector;
import no.uib.ii.algo.st8.algorithms.ClawInspector;
import no.uib.ii.algo.st8.algorithms.ClawInspector.ClawCollection;
import no.uib.ii.algo.st8.algorithms.ConnectedVertexCover;
import no.uib.ii.algo.st8.algorithms.CutAndBridgeInspector;
import no.uib.ii.algo.st8.algorithms.CycleInspector;
import no.uib.ii.algo.st8.algorithms.DiameterInspector;
import no.uib.ii.algo.st8.algorithms.EulerianInspector;
import no.uib.ii.algo.st8.algorithms.ExactDominatingSet;
import no.uib.ii.algo.st8.algorithms.ExactVertexCover;
import no.uib.ii.algo.st8.algorithms.FeedbackVertexSet;
import no.uib.ii.algo.st8.algorithms.FlowInspector;
import no.uib.ii.algo.st8.algorithms.GirthInspector;
import no.uib.ii.algo.st8.algorithms.GraphInformation;
import no.uib.ii.algo.st8.algorithms.HamiltonianCycleInspector;
import no.uib.ii.algo.st8.algorithms.HamiltonianInspector;
import no.uib.ii.algo.st8.algorithms.MaximalClique;
import no.uib.ii.algo.st8.algorithms.OddCycleTransversal;
import no.uib.ii.algo.st8.algorithms.PerfectCodeInspector;
import no.uib.ii.algo.st8.algorithms.PowerGraph;
import no.uib.ii.algo.st8.algorithms.RegularityInspector;
import no.uib.ii.algo.st8.algorithms.SimplicialInspector;
import no.uib.ii.algo.st8.algorithms.SpringLayout;
import no.uib.ii.algo.st8.algorithms.TreewidthInspector;
import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultEdgeFactory;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.model.EdgeStyle;
import no.uib.ii.algo.st8.util.Coordinate;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleGraph;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class GraphViewController {

	public static int DEFAULT_VERTEX_COLOR = Color.rgb(100, 100, 100);

	public static int MARKED_VERTEX_COLOR = Color.rgb(0, 150, 30);

	public static int USERSELECTED_VERTEX_COLOR = Color.rgb(0, 100, 50);

	public static int TOUCHED_VERTEX_COLOR = Color.rgb(255, 75, 0);

	public static int MARKED_EDGE_COLOR = Color.rgb(180, 255, 200);

	public static int DEFAULT_EDGE_COLOR = Color.GRAY;

	/** true if the labels should be drawn or not */
	public static boolean DO_SHOW_LABELS = true;

	/** true if the labels should be drawn or not */
	public static boolean EDGE_DRAW_MODE = true;

	private String info = "";
	private GraphView view;
	private final SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;

	private SpringLayout layout = null;

	private Set<DefaultVertex> highlightedVertices = new HashSet<DefaultVertex>();
	private Set<DefaultVertex> userSelectedVertices = new HashSet<DefaultVertex>();
	private Set<DefaultEdge<DefaultVertex>> markedEdges = new HashSet<DefaultEdge<DefaultVertex>>();

	// TODO this should depend on screen size and or zoom (scale of matrix)
	public final static float USER_MISS_RADIUS = 40;

	private final Coordinate CENTER_COORDINATE;

	private final Workspace activity;
	private final Vibrator vibrator;
	private final Undo graphWithMemory;

	public boolean toggleEdgeDraw() {
		EDGE_DRAW_MODE = !EDGE_DRAW_MODE;

		activity.shortToast(EDGE_DRAW_MODE ? "Draw edges."
				: "Tap to create vertices.");

		clearAll();
		redraw();
		return EDGE_DRAW_MODE;
	}

	public void setEdgeDrawMode(boolean mode) {
		if (EDGE_DRAW_MODE != mode)
			toggleEdgeDraw();
	}

	public GraphViewController(Workspace activity, int width, int height) {
		this.activity = activity;
		vibrator = (Vibrator) activity
				.getSystemService(Context.VIBRATOR_SERVICE);

		graph = new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(
				new DefaultEdgeFactory<DefaultVertex>());

		this.graphWithMemory = new Undo(graph);

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

	public boolean undo() {
		boolean ret = graphWithMemory.undo();
		redraw();
		return ret;
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

		for (DefaultVertex currentVertex : vertices) {
			Coordinate pos = currentVertex.getCoordinate();
			float currentDistance = pos.distance(coordinate);
			if (currentDistance < bestDistance) {
				bestVertex = currentVertex;
				bestDistance = currentDistance;
			}
		}
		return bestVertex;
	}

	private volatile long startTime = 0;

	public void time(boolean start) {
		long now = System.currentTimeMillis();
		if (start) {
			startTime = now;
		} else {
			long duration = now - startTime;
			double sec = duration / 1000d;
			System.out.printf("> %.3f\n", sec);
		}
	}

	/**
	 * Deselects all selected vertices and edges
	 */
	public void clearAll() {
		markedEdges.clear();
		userSelectedVertices.clear();
		highlightedVertices.clear();
		redraw();
	}

	/**
	 * Deselects all selected vertices and edges
	 */
	public void removeHighlight(Object obj) {
		markedEdges.remove(obj);
		userSelectedVertices.remove(obj);
		highlightedVertices.remove(obj);
	}

	/**
	 * Tabula rasa, remove the graph and all we know.
	 */
	public void newGraph() {
		clearAll();
		setEdgeDrawMode(false);
		while (!graph.vertexSet().isEmpty())
			graphWithMemory.removeVertex(graph.vertexSet().iterator().next());
		layout = null;
		redraw();
	}

	public void treewidth() {
		Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Integer> algorithm;
		AlgoWrapper<Integer> algoWrapper;

		algorithm = new TreewidthInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);
		algoWrapper = new AlgoWrapper<Integer>(activity, algorithm) {

			@Override
			protected String resultText(Integer result) {
				return "Treewidth is " + result;
			}
		};

		algoWrapper.setTitle("Computing treewidth ...");
		algoWrapper.execute();
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

		userSelectedVertices.addAll(highlightedVertices);
		highlightedVertices.clear();
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
					graphWithMemory.addEdge(u, v);
					redraw();
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
			if (graphWithMemory.removeVertex(v)) {
				deleted++;
			}
		}
		graphInfo();
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

	public boolean bruteForceHamiltonianPath() {
		time(true);
		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> hamPath = HamiltonianInspector
				.bruteForceHamiltonianPath(graph);
		time(false);

		clearAll();

		if (hamPath == null) {
			return false;
		}

		highlightPath(hamPath);
		redraw();
		return true;
	}

	/**
	 * Perfect code.
	 * 
	 * @return the number of vertices in efficient dominating set or -1 if none
	 *         exists
	 */
	public void showPerfectCode() {
		Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Collection<DefaultVertex>> perfectCodeAlgo;
		AlgoWrapper<Collection<DefaultVertex>> algoWrapper;

		perfectCodeAlgo = new PerfectCodeInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);
		algoWrapper = new AlgoWrapper<Collection<DefaultVertex>>(activity,
				perfectCodeAlgo) {

			@Override
			protected String resultText(Collection<DefaultVertex> result) {
				clearAll();
				if (result == null) {
					return "Not perfect code";
				} else {
					highlightedVertices.addAll(result);
					redraw();
					return "Perfect code size " + result.size();
				}
			}
		};

		algoWrapper.setTitle("Computing perfect code ...");
		algoWrapper.execute();
	}

	/**
	 * Simplicial vertices
	 */
	public int showSimplicialVertices() {
		time(true);
		Collection<DefaultVertex> simplicials = SimplicialInspector
				.getSimplicialVertices(graph);
		time(false);

		clearAll();

		highlightedVertices.addAll(simplicials);

		redraw();
		return simplicials.size();
	}

	/**
	 * Chordality (tmp)
	 */
	public boolean isChordal() {
		clearAll();
		redraw();
		return SimplicialInspector.isChordal(graph);
	}

	public boolean showHamiltonianPath() {
		time(true);
		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> hamPath = HamiltonianInspector
				.getHamiltonianPath(graph);
		time(false);

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

		if (!new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph).pathExists(s, t)) {
			return 0;
		}

		clearAll();

		DijkstraShortestPath<DefaultVertex, DefaultEdge<DefaultVertex>> dp = new DijkstraShortestPath<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph, s, t);

		GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> path = dp
				.getPath();
		if (path == null || path.getEdgeList() == null
				|| path.getEdgeList().size() == 0)
			return 0;

		highlightPath(path);

		redraw();

		return path.getEdgeList().size() + 1;
	}

	public int showFlow() {
		if (userSelectedVertices.size() != 2) {
			return -1;
		}
		Iterator<DefaultVertex> ite = userSelectedVertices.iterator();
		DefaultVertex s = ite.next();
		DefaultVertex t = ite.next();

		if (!new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph).pathExists(s, t)) {
			return 0;
		}

		clearAll();

		Pair<Integer, Collection<DefaultEdge<DefaultVertex>>> flow = FlowInspector
				.findFlow(graph, s, t);

		Collection<DefaultEdge<DefaultVertex>> edges = flow.second;

		highlightedVertices.add(s);
		highlightedVertices.add(t);
		for (DefaultEdge<DefaultVertex> e : edges) {
			markedEdges.add(e);
		}

		return flow.first;
	}

	public void constructPower() {
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> power = PowerGraph
				.constructPowerGraph(graph);
		for (DefaultEdge<DefaultVertex> edge : power.edgeSet()) {
			DefaultVertex a = power.getEdgeSource(edge);
			DefaultVertex b = power.getEdgeTarget(edge);
			if (!graph.containsEdge(a, b))
				graphWithMemory.addEdge(a, b);
		}
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
			highlightedVertices.add(e.getSource());
			highlightedVertices.add(e.getTarget());
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
			highlightedVertices.add(v);
			highlightedVertices.add(u);
		}
	}

	public boolean isEmptyGraph() {
		if (graph == null) {
			throw new NullPointerException("Graph was null, from isEmptyGraph");
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
		highlightedVertices.addAll(odds);
		redraw();
		return odds.size() == 0;
	}

	/**
	 * Finds a balanced separator and returns the size, or -1 if none exists.
	 * 
	 * @return the size of the separator, or -1 if no balanced separator exists.
	 */
	public void showSeparator() {
		time(true);
		AlgoWrapper<Collection<DefaultVertex>> algoWrapper;
		Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Collection<DefaultVertex>> balsam = new BalancedSeparatorInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);

		algoWrapper = new AlgoWrapper<Collection<DefaultVertex>>(activity,
				balsam) {

			@Override
			protected String resultText(Collection<DefaultVertex> result) {
				if (result == null)
					return "No balanced separator";
				else {
					clearAll();
					highlightedVertices.addAll(result);
					redraw();
					return "Balanced separator of size " + result.size();
				}
			}
		};
		algoWrapper.setTitle("Computing balanced separator ...");
		algoWrapper.execute();
		time(false);
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

		highlightedVertices.addAll(part);

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
		highlightedVertices.add(v);
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
		highlightedVertices.addAll(cuts);
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
		graphWithMemory.addVertex(universal);
		for (DefaultVertex v : graph.vertexSet()) {
			if (v != universal)
				graphWithMemory.addEdge(universal, v);
		}
		graphInfo();
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
	 * @return returns the edge if it is added, null if it is removed
	 */
	private DefaultEdge<DefaultVertex> toggleEdge(DefaultVertex v,
			DefaultVertex u) {
		if (isEmptyGraph())
			return null;

		DefaultEdge<DefaultVertex> edge = null;

		if (graph.containsEdge(v, u)) {
			graphWithMemory.removeEdge(v, u);
		} else {
			edge = graphWithMemory.addEdge(v, u);
		}

		graphInfo();
		redraw();
		return edge;
	}

	public void longShake(int n) {
		if (layout == null)
			layout = new SpringLayout(graph);
		layout.iterate(n);
		redraw();
	}

	public void shake() {
		if (layout == null)
			layout = new SpringLayout(graph);
		longShake(20);
		redraw();
	}

	public int showRegularityDeletionSet() {
		if (isEmptyGraph()) {
			return 0;
		}
		Set<DefaultVertex> regdel = RegularityInspector
				.regularDeletionSet(graph);
		clearAll();
		highlightedVertices.addAll(regdel);
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
		highlightedVertices.addAll(oct);
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
		highlightedVertices.addAll(fvs);
		redraw();
		return fvs.size();
	}

	public int showConnectedFeedbackVertexSet() {
		if (isEmptyGraph()) {
			return 0;
		}
		Collection<DefaultVertex> fvs = FeedbackVertexSet
				.findExactConnectedFeedbackVertexSet(graph);
		clearAll();
		highlightedVertices.addAll(fvs);
		redraw();
		return fvs.size();
	}

	public int showVertexCover() {
		if (isEmptyGraph()) {
			return 0;
		}
		time(true);
		Set<DefaultVertex> cover = ExactVertexCover.findExactVertexCover(graph);
		time(false);
		clearAll();
		highlightedVertices.addAll(cover);
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
		time(true);
		SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> cvc = ConnectedVertexCover
				.getConnectedVertexCover(graph);
		time(false);

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
		highlightedVertices.addAll(graph.vertexSet());
		highlightedVertices.removeAll(cover);

		return highlightedVertices.size();
	}

	public void showMaximumClique() {
		Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Set<DefaultVertex>> algorithm = new MaximalClique<DefaultVertex, DefaultEdge<DefaultVertex>>(
				graph);
		AlgoWrapper<Set<DefaultVertex>> algoWrapper = new AlgoWrapper<Set<DefaultVertex>>(
				activity, algorithm) {

			@Override
			protected String resultText(Set<DefaultVertex> result) {
				clearAll();
				highlightedVertices.addAll(result);
				redraw();
				return "Clique Number is " + result.size();
			}
		};
		algoWrapper.setTitle("Computing maximum clique...");
		algoWrapper.execute();
	}

	public int showDominatingSet() {
		if (isEmptyGraph()) {
			return 0;
		}
		Collection<DefaultVertex> domset = ExactDominatingSet
				.exactDominatingSet(graph);
		clearAll();
		highlightedVertices.addAll(domset);
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
		highlightedVertices.add(center);
		redraw();
		return true;

	}

	public int computeBandwidth() {
		if (isEmptyGraph()) {
			return 0;
		}

		clearAll();

		// TODO don't know

		new AsyncTask<Void, Integer, Integer>() {
			@Override
			protected Integer doInBackground(Void... params) {
				BandwidthInspector<DefaultVertex, DefaultEdge<DefaultVertex>> bi = new BandwidthInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
						graph);
				return bi.execute();
			}
		}.execute();
		redraw();
		return 0;
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

	public int showClawDeletion() {
		Collection<DefaultEdge<DefaultVertex>> edges = ClawInspector
				.minimalClawDeletionSet(graph);
		clearAll();
		if (edges != null) {
			markedEdges.addAll(edges);
			redraw();
			return edges.size();
		}
		return 0;
	}

	public boolean showAllClaws() {
		ClawCollection<DefaultVertex> col = ClawInspector.getClaws(graph);
		clearAll();
		highlightedVertices.addAll(col.getCenters());
		for (Pair<DefaultVertex, DefaultVertex> e : col.getArms()) {
			markedEdges.add(graph.getEdge(e.first, e.second));
		}
		redraw();
		return col.getCenters().size() > 0;
	}

	public int showAllCycle4() {
		Collection<List<DefaultVertex>> cycles = CycleInspector
				.findAllC4(graph);
		clearAll();
		for (List<DefaultVertex> cycle : cycles) {
			System.out.println("\t" + cycle);
			for (int i = 0; i < cycle.size(); i++) {
				DefaultVertex v = cycle.get(i % cycle.size());
				DefaultVertex u = cycle.get((i + 1) % cycle.size());
				highlightedVertices.add(v);
				highlightedVertices.add(u);
				if (graph.containsEdge(v, u)) {
					DefaultEdge<DefaultVertex> e = graph.getEdge(v, u);
					markedEdges.add(e);
				} else {
					System.err.println("Strange, lacks edge for v=" + v
							+ ", u=" + u);
					System.err.println(cycle);
				}
			}
		}
		return cycles.size();
	}

	public String graphInfo() {
		isEmptyGraph(); // hack to test if null
		info = GraphInformation.graphInfo(graph);
		return info;
	}

	public void redraw() {

		if (graph == null) {
			return;
		}

		graphInfo();

		for (DefaultVertex v : graph.vertexSet()) {
			v.setLabel(""); // todo fix
		}
		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			e.setStyle(EdgeStyle.SOLID); // todo fix
		}

		for (DefaultVertex v : graph.vertexSet()) {
			v.setColor(EDGE_DRAW_MODE ? DEFAULT_VERTEX_COLOR
					: TOUCHED_VERTEX_COLOR);
			if (highlightedVertices.contains(v)) {
				v.setColor(MARKED_VERTEX_COLOR);
			}
			if (userSelectedVertices.contains(v)) {
				v.setColor(USERSELECTED_VERTEX_COLOR);
			}
		}
		for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
			e.setColor(DEFAULT_EDGE_COLOR);
			if (markedEdges.contains(e)) {
				e.setStyle(EdgeStyle.BOLD);
			}
		}

		view.redraw(info, graph);
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
		/** This vertex was touch, e.g. for scrolling and moving purposes */
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

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			Coordinate sCoordinate = new Coordinate(e2.getX(), e2.getY());
			Coordinate gCoordinate = translateCoordinate(sCoordinate);
			DefaultVertex hit = getClosestVertex(gCoordinate, USER_MISS_RADIUS);

			float dist = (float) Math.round(Math.sqrt((velocityX * velocityX)
					+ (velocityY * velocityY)));

			System.out.println("dist=" + dist + " \tdx=" + velocityX + " \tdy="
					+ velocityY);

			if (dist < 4000)
				return true;

			System.out.print("fling: ");

			if (hit != null) {
				System.out.println(hit.getId());
				clearAll();
				graphWithMemory.removeVertex(hit);
				touchedVertex = null;
				redraw();
				return true;
			} else {
				System.out.println("miss");
			}

			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (EDGE_DRAW_MODE) {

				Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
				Coordinate gCoordinate = translateCoordinate(sCoordinate);
				DefaultVertex hit = getClosestVertex(gCoordinate,
						USER_MISS_RADIUS);

				if (hit == null) {
					clearAll();
					touchedVertex = null;
					redraw();
					return true;
				}

				graphInfo();

			} else {

				Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
				Coordinate gCoordinate = translateCoordinate(sCoordinate);
				DefaultVertex hit = getClosestVertex(gCoordinate,
						USER_MISS_RADIUS);

				if (hit == null) {
					DefaultVertex newvertex = new DefaultVertex(gCoordinate);
					graphWithMemory.addVertex(newvertex);

					graphInfo();
				} else {
					if (userSelectedVertices.contains(hit)) {
						userSelectedVertices.remove(hit);
					} else {
						userSelectedVertices.add(hit);
					}
				}
			}

			touchedVertex = null;

			redraw();
			return true;

		}

		public void onLongPress(MotionEvent e) {

			vibrator.vibrate(50);
			toggleEdgeDraw();
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
				if (EDGE_DRAW_MODE) {
					Coordinate sCoordinate = new Coordinate(e2.getX(),
							e2.getY());
					Coordinate gCoordinate = translateCoordinate(sCoordinate);
					DefaultVertex hit = getClosestVertex(gCoordinate,
							USER_MISS_RADIUS);

					if (hit != null) {
						System.out.println("HIT " + hit.getId());
						if (touchedVertex != null && touchedVertex != hit) {
							DefaultEdge<DefaultVertex> edge = toggleEdge(hit,
									touchedVertex);
							userSelectedVertices.remove(touchedVertex);
							userSelectedVertices.add(hit);
							markedEdges.clear();
							if (edge != null)
								markedEdges.add(edge);
						}
						touchedVertex = hit;
					}

				} else {
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
