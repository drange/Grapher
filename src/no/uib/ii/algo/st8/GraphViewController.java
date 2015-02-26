package no.uib.ii.algo.st8;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import no.uib.ii.algo.st8.algorithms.Algorithm;
import no.uib.ii.algo.st8.algorithms.BalancedSeparatorInspector;
import no.uib.ii.algo.st8.algorithms.BandwidthInspector;
import no.uib.ii.algo.st8.algorithms.BipartiteInspector;
import no.uib.ii.algo.st8.algorithms.CenterInspector;
import no.uib.ii.algo.st8.algorithms.Chordalization;
import no.uib.ii.algo.st8.algorithms.ChromaticNumber;
import no.uib.ii.algo.st8.algorithms.ClawInspector;
import no.uib.ii.algo.st8.algorithms.ClawInspector.ClawCollection;
import no.uib.ii.algo.st8.algorithms.ConnectedFeedbackVertexSet;
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
import no.uib.ii.algo.st8.algorithms.HamiltonianPathInspector;
import no.uib.ii.algo.st8.algorithms.MaximalClique;
import no.uib.ii.algo.st8.algorithms.MinimalTriangulation;
import no.uib.ii.algo.st8.algorithms.OddCycleTransversal;
import no.uib.ii.algo.st8.algorithms.OptimalColouring;
import no.uib.ii.algo.st8.algorithms.PerfectCodeInspector;
import no.uib.ii.algo.st8.algorithms.PowerGraph;
import no.uib.ii.algo.st8.algorithms.RedBlueDominatingSet;
import no.uib.ii.algo.st8.algorithms.RegularityInspector;
import no.uib.ii.algo.st8.algorithms.SimplicialInspector;
import no.uib.ii.algo.st8.algorithms.SpringLayout;
import no.uib.ii.algo.st8.algorithms.SteinerTree;
import no.uib.ii.algo.st8.algorithms.ThresholdInspector;
import no.uib.ii.algo.st8.algorithms.TreewidthInspector;
import no.uib.ii.algo.st8.algorithms.VertexIntegrity;
import no.uib.ii.algo.st8.interval.IntervalGraph;
import no.uib.ii.algo.st8.interval.SimpleToBasicWrapper;
import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultEdgeFactory;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.model.EdgeStyle;
import no.uib.ii.algo.st8.util.Coordinate;
import no.uib.ii.algo.st8.util.Neighbors;
import no.uib.ii.algo.st8.util.SnapToGrid;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleGraph;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Vibrator;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

//import android.graphics.Matrix;

public class GraphViewController {

  public static final int DEFAULT_VERTEX_COLOR = Color.rgb(100, 100, 100);

  public static final int MARKED_VERTEX_COLOR = Color.rgb(0, 150, 30);

  public static final int USERSELECTED_VERTEX_COLOR = Color.rgb(0, 100, 50);

  public static final int TOUCHED_VERTEX_COLOR = Color.rgb(255, 75, 0);

  public static final int MARKED_EDGE_COLOR = Color.rgb(180, 255, 200);

  public static final int DEFAULT_EDGE_COLOR = Color.GRAY;

  /** true if the labels should be drawn or not */
  public static boolean DO_SHOW_LABELS = true;

  /** true if the labels should be drawn or not */
  public static boolean EDGE_DRAW_MODE = true;

  public static int TRASH_CAN = 0;

  private String info = "";
  private GraphView view;
  private final SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> graph;

  private SpringLayout layout = null;

  private Set<DefaultVertex> highlightedVertices = new HashSet<DefaultVertex>();
  private Set<DefaultVertex> userSelectedVertices = new HashSet<DefaultVertex>();
  private Set<DefaultEdge<DefaultVertex>> markedEdges = new HashSet<DefaultEdge<DefaultVertex>>();

  private Map<DefaultVertex, Integer> colourMap = new HashMap<DefaultVertex, Integer>();
  private DefaultVertex deleteVertex = null;

  // TODO this should depend on screen size and or zoom (scale of matrix)
  public final static float USER_MISS_RADIUS = 40;

  private final Coordinate CENTER_COORDINATE;

  private final Workspace activity;
  private final Vibrator vibrator;
  private final Undo graphWithMemory;

  public void snapToGrid() {

    view.getTransformMatrix().reset();

    System.out.println("SNAP!");
    new SnapToGrid(graph).snap();

    centralize();

    redraw();
  }

  public boolean toggleEdgeDraw() {
    EDGE_DRAW_MODE = !EDGE_DRAW_MODE;

    activity.shortToast(EDGE_DRAW_MODE ? "Draw edges." : "Tap to create vertices.");

    clearAll();
    redraw();

    return EDGE_DRAW_MODE;

  }

  public void setEdgeDrawMode(boolean mode) {
    if (EDGE_DRAW_MODE != mode)
      toggleEdgeDraw();
  }

  public void trashCan(int mode) {
    TRASH_CAN = mode;
    redraw();
  }

  public GraphViewController(Workspace activity, int width, int height) {
    this.activity = activity;
    vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

    graph = new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(new DefaultEdgeFactory<DefaultVertex>());

    this.graphWithMemory = new Undo(graph);

    view = new GraphView(activity);

    view.setOnClickListener(activity);
    view.setOnTouchListener(new View.OnTouchListener() {
      PrivateGestureListener gl = new PrivateGestureListener();
      GestureDetector gd = new GestureDetector(gl); // TODO deprecated!

      public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
          if (TRASH_CAN == 2) {
            if (deleteVertex != null) {
              graphWithMemory.removeVertex(deleteVertex);
              Coordinate c = gl.clearCoordinate();
              if (c != null)
                deleteVertex.setCoordinate(c);
              deleteVertex = null;
            }
          }
          gl.clearCoordinate();
          trashCan(0);
        }
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
   * Returns closest vertex to coordinate within the range of radius. Radius can
   * be POSITIVE_INFINITY, in which case we accept any radius.
   * 
   * We return null if no such vertex exists.
   * 
   * If two vertices have exactly the same distance, one is chosen arbitrarily.
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

  private static volatile long startTime = 0;
  private static volatile boolean isStarted = false;

  public static void time(boolean start) {
    long now = System.currentTimeMillis();
    if (start) {
      startTime = now;
      isStarted = true;
    } else {
      if (!isStarted)
        return;
      long duration = now - startTime;
      double sec = duration / 1000d;
      System.out.printf("> %.3f seconds\n", sec);
      isStarted = false;
    }
  }

  public Bitmap screenShot() {
    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    view.draw(canvas);
    return bitmap;
  }

  /**
   * Deselects all selected vertices and edges
   */
  public void clearAll() {
    markedEdges.clear();
    userSelectedVertices.clear();
    highlightedVertices.clear();
    colourMap.clear();
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

    algorithm = new TreewidthInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(graph);
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
    clearAll();
    userSelectedVertices.addAll(graph.vertexSet());
    redraw();
  }

  public void deselectAll() {
    userSelectedVertices.clear();
    redraw();
  }

  public void selectAllHighlightedVertices() {
    userSelectedVertices.addAll(highlightedVertices);
    highlightedVertices.clear();
    redraw();
  }

  /**
   * Selects all vertices that are not selected and deselects those who are.
   * 
   */
  public void invertSelectedVertices() {
    Set<DefaultVertex> select = new HashSet<DefaultVertex>(graph.vertexSet().size());
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
    Set<DefaultVertex> reachable = new HashSet<DefaultVertex>(graph.vertexSet().size());
    Set<DefaultVertex> userselect = new HashSet<DefaultVertex>(userSelectedVertices);
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
   * Contracts the two selected vertices
   * 
   * @return true if something happened
   */
  public boolean contract() {
    if (userSelectedVertices.size() != 2)
      return false;
    Iterator<DefaultVertex> i = userSelectedVertices.iterator();
    DefaultVertex v = i.next();
    DefaultVertex u = i.next();
    if (graph.containsEdge(v, u)) {
      Collection<DefaultVertex> neighbors = Neighbors.openNeighborhood(graph, v);
      neighbors.addAll(Neighbors.openNeighborhood(graph, u));
      neighbors.remove(v);
      neighbors.remove(u);

      Coordinate cv = v.getCoordinate();
      Coordinate cu = u.getCoordinate();
      Coordinate c = new Coordinate((cv.getX() + cu.getX()) / 2, (cv.getY() + cu.getY()) / 2);

      graphWithMemory.removeVertex(v);
      graphWithMemory.removeVertex(u);
      DefaultVertex w = new DefaultVertex(c);

      graphWithMemory.addVertex(w);

      for (DefaultVertex x : neighbors) {
        graphWithMemory.addEdge(w, x);
      }

      userSelectedVertices.clear();

      redraw();

      return true;
    } else {
      return false;
    }
  }

  /**
   * Perform local complement.
   */
  public boolean localComplement() {
    if (userSelectedVertices.isEmpty()) {
      return false;
    }
    for (DefaultVertex v : userSelectedVertices) {
      ArrayList<DefaultVertex> neigh = new ArrayList<DefaultVertex>(Neighbors.openNeighborhood(graph, v));
      for (int i = 0; i < neigh.size(); i++) {
        for (int j = i + 1; j < neigh.size(); j++) {
          toggleEdge(neigh.get(i), neigh.get(j), false);
        }
      }
    }
    redraw();
    return true;
  }

  /**
   * Makes the selected vertices into a clique.
   */
  public void completeSelectedVertices() {
    for (DefaultVertex v : userSelectedVertices) {
      for (DefaultVertex u : userSelectedVertices) {
        if (u != v && !graph.containsEdge(u, v)) {
          graphWithMemory.addEdge(u, v);
        }
      }
    }
    redraw();
  }

  /**
   * Between every pair of selected vertices, toggles edge.
   * 
   */
  public void complement() {
    ArrayList<DefaultVertex> vertices = new ArrayList<DefaultVertex>(graph.vertexSet().size());
    vertices.addAll(graph.vertexSet());

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
   * Between every pair of selected vertices, toggles edge.
   * 
   */
  public void complementSelected() {
    if (userSelectedVertices == null || userSelectedVertices.size() == 0)
      return;

    ArrayList<DefaultVertex> vertices = new ArrayList<DefaultVertex>(graph.vertexSet().size());
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
    int deleted = 0;

    for (DefaultVertex v : userSelectedVertices) {
      if (graphWithMemory.removeVertex(v)) {
        deleted++;
      }
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

  public void minimalTriangulation() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Set<DefaultEdge<DefaultVertex>>> minTri;
    AlgoWrapper<Set<DefaultEdge<DefaultVertex>>> algoWrapper;

    minTri = new MinimalTriangulation<DefaultVertex, DefaultEdge<DefaultVertex>>(graph);
    algoWrapper = new AlgoWrapper<Set<DefaultEdge<DefaultVertex>>>(activity, minTri) {

      @Override
      protected String resultText(Set<DefaultEdge<DefaultVertex>> result) {
        clearAll();
        String ret = "";
        if (result == null) {
          ret = "Could not compute minimal fill in";
        } else {
          ret = "Minimum fill in of size " + result.size();
          markedEdges.addAll(result);
          for (DefaultEdge<DefaultVertex> e : result)
            graphWithMemory.addEdge(e);
        }
        redraw();
        return ret;
      }
    };

    algoWrapper.setTitle("Computing minmal triangulation");
    algoWrapper.execute();

  }

  public void chromaticNumber() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Integer> chromaticAlgo;
    AlgoWrapper<Integer> algoWrapper;

    chromaticAlgo = new ChromaticNumber<DefaultVertex, DefaultEdge<DefaultVertex>>(graph);
    algoWrapper = new AlgoWrapper<Integer>(activity, chromaticAlgo) {

      @Override
      protected String resultText(Integer result) {
        return "The chromatic number is " + result;
      }
    };

    algoWrapper.setTitle("Computing chromatic number");
    algoWrapper.execute();
  }

  public void showColouring() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Set<Set<DefaultVertex>>> colouring;
    AlgoWrapper<Set<Set<DefaultVertex>>> algoWrapper;

    colourMap = new HashMap<DefaultVertex, Integer>();
    colouring = new OptimalColouring<DefaultVertex, DefaultEdge<DefaultVertex>>(graph);
    algoWrapper = new AlgoWrapper<Set<Set<DefaultVertex>>>(activity, colouring) {

      @Override
      protected String resultText(Set<Set<DefaultVertex>> result) {
        for (Set<DefaultVertex> col : result) {
          Random r = new Random();
          Integer colour = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));
          for (DefaultVertex v : col) {
            colourMap.put(v, colour);
          }
        }
        redraw();
        return "Here is a colouring of size " + result.size();
      }
    };

    algoWrapper.setTitle("Computing colouring");
    algoWrapper.execute();
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

    perfectCodeAlgo = new PerfectCodeInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(graph);
    algoWrapper = new AlgoWrapper<Collection<DefaultVertex>>(activity, perfectCodeAlgo) {

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
    Collection<DefaultVertex> simplicials = SimplicialInspector.getSimplicialVertices(graph);
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

  public void showChordalization() {
    Chordalization<DefaultVertex, DefaultEdge<DefaultVertex>> algo = new Chordalization<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Set<DefaultEdge<DefaultVertex>>> wrapper = new AlgoWrapper<Set<DefaultEdge<DefaultVertex>>>(activity, algo) {

      @Override
      protected String resultText(Set<DefaultEdge<DefaultVertex>> result) {
        clearAll();
        String ret = "";
        if (result == null) {
          ret = "Could not compute minimum fill in";
        } else {
          ret = "Minimum fill in of size " + result.size();
          clearAll();
          for (DefaultEdge<DefaultVertex> e : result) {
            markedEdges.add(e);
          }
        }
        redraw();
        return ret;
      }

    };
    wrapper.setTitle("Computing minimum fill in ...");
    wrapper.execute();
  }

  public int showInterval() {
    clearAll();

    SimpleToBasicWrapper<DefaultVertex, DefaultEdge<DefaultVertex>> wrap = new SimpleToBasicWrapper<DefaultVertex, DefaultEdge<DefaultVertex>>(
        getGraph());

    IntervalGraph ig = wrap.getIntervalGraph();
    if (ig != null) {
      return 0;
    } else {
      if (wrap.isChordal()) {
        highlightedVertices.addAll(wrap.getAT());
        redraw();
        return 1; // has AT
      }
      return 2; // is not chordal
    }
  }

  /**
   * True if threshold graph.
   * 
   * @return
   */
  public void showThreshold() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Set<DefaultVertex>> thresholdAlgo;
    AlgoWrapper<Set<DefaultVertex>> algoWrapper;

    thresholdAlgo = new ThresholdInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(graph);

    algoWrapper = new AlgoWrapper<Set<DefaultVertex>>(activity, thresholdAlgo) {

      protected String resultText(Set<DefaultVertex> result) {

        clearAll();
        if (result == null) {
          return "Computation aborted";
        } else {
          if (result.isEmpty()) {
            return "Graph is threshold";
          }
          highlightedVertices.addAll(result);
          redraw();
          return "Threshold obstruction highlighted";
        }
      }
    };
    algoWrapper.setTitle("Recognizing threshold graph ...");
    algoWrapper.execute();

  }

  public void showSteinerTree() {

    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Collection<DefaultEdge<DefaultVertex>>> steinerAlgo;
    AlgoWrapper<Collection<DefaultEdge<DefaultVertex>>> algoWrapper;

    final HashSet<DefaultVertex> selected = new HashSet<DefaultVertex>();
    selected.addAll(userSelectedVertices);

    steinerAlgo = new SteinerTree<DefaultVertex, DefaultEdge<DefaultVertex>>(graph, userSelectedVertices);
    algoWrapper = new AlgoWrapper<Collection<DefaultEdge<DefaultVertex>>>(activity, steinerAlgo) {

      @Override
      protected String resultText(Collection<DefaultEdge<DefaultVertex>> result) {
        if (selected.isEmpty())
          return "Terminal set cannot be empty.";
        clearAll();
        if (result == null) {
          if (!SteinerTree.areTerminalsConnected(graph, selected)) {
            return "Terminals are not connected";
          }
          return "Computation aborted";
        } else {
          markedEdges.addAll(result);
          userSelectedVertices.clear();
          userSelectedVertices.addAll(selected);

          redraw();
          return "Steiner tree on " + result.size() + " edges highlighted.";
        }
      }
    };
    algoWrapper.setTitle("Computing steiner tree ...");
    algoWrapper.execute();
  }

  public void showHamiltonianPath() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>>> hamPathAlgo;
    AlgoWrapper<GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>>> algoWrapper;

    hamPathAlgo = new HamiltonianPathInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(graph);
    algoWrapper = new AlgoWrapper<GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>>>(activity, hamPathAlgo) {

      @Override
      protected String resultText(GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> result) {
        clearAll();
        if (result == null) {
          return "No hamiltonian path";
        } else {
          highlightPath(result);
          redraw();
          return "Hamiltonian path";
        }
      }
    };
    algoWrapper.setTitle("Computing hamiltonian path ...");
    algoWrapper.execute();
  }

  public void showHamiltonianCycle() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>>> hamcyc;
    AlgoWrapper<GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>>> alg;

    hamcyc = new HamiltonianCycleInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(graph);
    alg = new AlgoWrapper<GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>>>(activity, hamcyc) {

      @Override
      protected String resultText(GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> result) {
        clearAll();
        if (result == null) {
          redraw();
          return "Not hamiltonian.";
        } else {
          highlightPath(result);
          redraw();
          return "Graph is hamiltonian";
        }
      }
    };
    alg.setTitle("Computing hamiltonian cycle ...");
    alg.execute();
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

    if (!new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(graph).pathExists(s, t)) {
      return 0;
    }

    clearAll();

    DijkstraShortestPath<DefaultVertex, DefaultEdge<DefaultVertex>> dp = new DijkstraShortestPath<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph, s, t);

    GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> path = dp.getPath();
    if (path == null || path.getEdgeList() == null || path.getEdgeList().size() == 0)
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

    if (!new ConnectivityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(graph).pathExists(s, t)) {
      clearAll();
      redraw();
      return 0;
    }

    clearAll();

    Pair<Integer, Collection<DefaultEdge<DefaultVertex>>> flow = FlowInspector.findFlow(graph, s, t);

    Collection<DefaultEdge<DefaultVertex>> edges = flow.second;

    highlightedVertices.add(s);
    highlightedVertices.add(t);
    for (DefaultEdge<DefaultVertex> e : edges) {
      markedEdges.add(e);
    }
    redraw();
    return flow.first;
  }

  public void constructPower() {
    SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> power = PowerGraph.constructPowerGraph(graph);
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
  private void highlightPath(GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> gp) {
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
  private void highlightGraph(SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> h) {
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

  /**
   * Tests if graph is eulerian. If not, highlights all vertices of odd degree.
   * 
   * @return true if eulerian.
   */
  public boolean isEulerian() {
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

    algoWrapper = new AlgoWrapper<Collection<DefaultVertex>>(activity, balsam) {

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
    clearAll();

    GraphPath<DefaultVertex, DefaultEdge<DefaultVertex>> gp = DiameterInspector.diameterPath(graph);

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
    clearAll();
    int girth = GirthInspector.girth(graph);
    return girth;
  }

  public void showSpanningTree() {
    KruskalMinimumSpanningTree<DefaultVertex, DefaultEdge<DefaultVertex>> mst = new KruskalMinimumSpanningTree<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    Set<DefaultEdge<DefaultVertex>> spanning = mst.getEdgeSet();
    clearAll();
    markedEdges.addAll(spanning);
    redraw();
  }

  /**
   * Finds if there is a cut vertex, ie if there is a vertex whose removal
   * increases the number of connected components
   * 
   * @return true iff there is a cut vertex
   */
  public boolean showCutVertex() {
    clearAll();
    DefaultVertex v = CutAndBridgeInspector.findCutVertex(graph);
    if (v == null)
      return false;
    highlightedVertices.add(v);
    redraw();
    return true;
  }

  /**
   * Finds if there is a cut vertex, ie if there is a vertex whose removal
   * increases the number of connected components, returns them all.
   * 
   * @return number of cut vertices
   */
  public int showAllCutVertices() {
    clearAll();
    Set<DefaultVertex> cuts = CutAndBridgeInspector.findAllCutVertices(graph);
    highlightedVertices.addAll(cuts);
    redraw();
    return cuts.size();
  }

  /**
   * Finds if there is an edge whose removal increases the number of connected
   * components.
   * 
   * @return true iff there is a bridge
   */
  public boolean showBridge() {
    clearAll();
    DefaultEdge<DefaultVertex> e = CutAndBridgeInspector.findBridge(graph);
    if (e == null) {
      redraw();
      return false;
    }
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
    clearAll();
    Set<DefaultEdge<DefaultVertex>> bridges = CutAndBridgeInspector.findAllBridges(graph);
    markedEdges.addAll(bridges);
    return bridges.size();
  }

  /**
   * Inserts universal vertex, returns its degree, i.e. the size of graph before
   * insertion.
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
      pos = new Coordinate(x / deg + USER_MISS_RADIUS, y / deg + USER_MISS_RADIUS);
    }

    DefaultVertex universal = new DefaultVertex(pos);
    graphWithMemory.addVertex(universal);
    for (DefaultVertex v : graph.vertexSet()) {
      if (v != universal)
        graphWithMemory.addEdge(universal, v);
    }

    redraw();

    return deg;
  }

  public View getView() {
    return view;
  }

  /**
   * Toggles edges between given vertex with memory. If redraw is set, will
   * redraw after operation.
   * 
   * @param v
   *          vertex v
   * @param u
   *          vertex u
   * @return returns the edge if it is added, null if it is removed
   */
  private DefaultEdge<DefaultVertex> toggleEdge(DefaultVertex v, DefaultVertex u, boolean redraw) {
    DefaultEdge<DefaultVertex> edge = null;

    if (graph.containsEdge(v, u)) {
      graphWithMemory.removeEdge(v, u);
    } else {
      edge = graphWithMemory.addEdge(v, u);
    }

    if (redraw)
      redraw();
    return edge;
  }

  /**
   * Toggles edges between given vertex. Redraws as well! And adds to
   * 
   * @param v
   *          vertex v
   * @param u
   *          vertex u
   * @return returns the edge if it is added, null if it is removed
   */
  private DefaultEdge<DefaultVertex> toggleEdge(DefaultVertex v, DefaultVertex u) {
    return toggleEdge(v, u, true);
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

  public void showRegularityDeletionSet() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Collection<DefaultVertex>> algo = new RegularityInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> alg = new AlgoWrapper<Collection<DefaultVertex>>(activity, algo,
        "Regularity Deletion Set") {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        if (result.size() == 0) {
          return "Graph is regular";
        } else {
          clearAll();
          highlightedVertices.addAll(result);
          redraw();
          return "Regularity deletion set of size " + result.size();
        }
      }
    };
    alg.setTitle("Computing regularity deletion set ...");
    alg.execute();
  }

  public void showOddCycleTransversal() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Collection<DefaultVertex>> algo = new OddCycleTransversal<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> alg = new AlgoWrapper<Collection<DefaultVertex>>(activity, algo, "Odd cycle transveral") {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        if (result.size() == 0) {
          return "Graph is bipartite";
        } else {
          clearAll();
          highlightedVertices.addAll(result);
          redraw();
          return "OCT of size " + result.size();
        }
      }
    };
    alg.setTitle("Computing odd cycle transversal ...");
    alg.execute();
  }

  public void showVertexIntegrity() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, VertexIntegrity.VertexIntegritySolution<DefaultVertex>> algo = new VertexIntegrity<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<VertexIntegrity.VertexIntegritySolution<DefaultVertex>> alg = new AlgoWrapper<VertexIntegrity.VertexIntegritySolution<DefaultVertex>>(
        activity, algo, "Vertex integrity") {

      @Override
      protected String resultText(VertexIntegrity.VertexIntegritySolution<DefaultVertex> result) {
        clearAll();

        System.out.println(result);

        highlightedVertices.addAll(result.X);
        redraw();
        return "Vertex integrity is " + result.p;
      }
    };
    alg.setTitle("Computing vertex integrity ...");
    alg.execute();
  }

  public void showFeedbackVertexSet() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Collection<DefaultVertex>> algo = new FeedbackVertexSet<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> alg = new AlgoWrapper<Collection<DefaultVertex>>(activity, algo, "Feedback vertex set") {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        if (result.size() == 0) {
          return "Graph is acyclic";
        } else {
          clearAll();
          highlightedVertices.addAll(result);
          redraw();
          return "FVS of size " + result.size();
        }
      }
    };
    alg.setTitle("Computing feedback vertex set ...");
    alg.execute();
  }

  public void showConnectedFeedbackVertexSet() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Collection<DefaultVertex>> cfvs = new ConnectedFeedbackVertexSet<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> algo = new AlgoWrapper<Collection<DefaultVertex>>(activity, cfvs,
        "Connected feedback vertex set") {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        clearAll();
        String ret = "";
        if (result == null) {
          ret = "Graph was disconnected, no connected feedback vertex set exists.";
        } else if (result.size() == 0) {
          ret = "Graph is acyclic.";
        } else {
          highlightedVertices.addAll(result);
          ret = "Connected FVS of size " + result.size() + ".";
        }
        redraw();
        return ret;
      }
    };
    algo.setTitle("Computing connected feedback vertex set ...");
    algo.execute();
  }

  public void showVertexCover() {
    ExactVertexCover<DefaultVertex, DefaultEdge<DefaultVertex>> algo = new ExactVertexCover<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> wrapper = new AlgoWrapper<Collection<DefaultVertex>>(activity, algo) {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        clearAll();
        String ret = "";
        if (result == null) {
          ret = "Could not compute vertex cover.";
        } else {
          ret = "Vertex cover of size " + result.size();
          highlightedVertices.addAll(result);
        }
        redraw();
        return ret;

      }
    };
    wrapper.setTitle("Computing vertex cover ...");
    wrapper.execute();
  }

  /**
   * Finds connected vertex cover (cvc), highlights it and returns its size
   * (order). If no CVC exists, e.g. there are two connected components
   * containing edges, we return -1.
   * 
   * @return order of cvc, or -1 if none exists
   */
  public void showConnectedVertexCover() {
    ConnectedVertexCover<DefaultVertex, DefaultEdge<DefaultVertex>> cvc = new ConnectedVertexCover<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>> algo = new AlgoWrapper<SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>>(
        activity, cvc) {
      @Override
      protected String resultText(SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> result) {
        clearAll();
        String ret = "";
        if (result == null) {
          ret = "Graph was disconnected, no connected vertex cover exists.";
        } else {
          ret = "Connected vertex cover of size " + result.vertexSet().size();
          highlightGraph(result);
        }
        redraw();
        return ret;
      }
    };
    algo.setTitle("Computing connected vertex cover ...");
    algo.execute();
  }

  public void showMaximumIndependentSet() {
    ExactVertexCover<DefaultVertex, DefaultEdge<DefaultVertex>> algo = new ExactVertexCover<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> wrapper = new AlgoWrapper<Collection<DefaultVertex>>(activity, algo) {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        clearAll();
        String ret = "";
        if (result == null) {
          ret = "Could not compute maximum independent set.";
        } else {
          ret = "Maximum independent set of size " + (graph.vertexSet().size() - result.size());
          clearAll();
          highlightedVertices.addAll(graph.vertexSet());
          highlightedVertices.removeAll(result);
        }
        redraw();
        return ret;

      }
    };
    wrapper.setTitle("Computing independent set ...");
    wrapper.execute();
  }

  public void showMaximumClique() {
    Algorithm<DefaultVertex, DefaultEdge<DefaultVertex>, Collection<DefaultVertex>> algorithm = new MaximalClique<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> algoWrapper = new AlgoWrapper<Collection<DefaultVertex>>(activity, algorithm) {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        clearAll();
        highlightedVertices.addAll(result);
        redraw();
        return "Clique Number is " + result.size();
      }
    };
    algoWrapper.setTitle("Computing maximum clique ...");
    algoWrapper.execute();
  }

  public void showDominatingSet() {
    ExactDominatingSet<DefaultVertex, DefaultEdge<DefaultVertex>> eds = new ExactDominatingSet<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> algo = new AlgoWrapper<Collection<DefaultVertex>>(activity, eds, "Dominating set") {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        clearAll();
        highlightedVertices.addAll(result);
        redraw();
        return "Dominating set of size " + result.size();
      }
    };
    algo.setTitle("Computing dominating set ...");
    algo.execute();
  }

  public void showRedBlueDominatingSet() {
    RedBlueDominatingSet<DefaultVertex, DefaultEdge<DefaultVertex>> eds = new RedBlueDominatingSet<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Collection<DefaultVertex>> algo = new AlgoWrapper<Collection<DefaultVertex>>(activity, eds, "Dominating set") {

      @Override
      protected String resultText(Collection<DefaultVertex> result) {
        clearAll();
        highlightedVertices.addAll(result);
        redraw();
        return "Dominating set of size " + result.size();
      }
    };
    algo.setTitle("Computing dominating set ...");
    algo.execute();
  }

  public boolean showCenterVertex() {
    clearAll();
    redraw();
    DefaultVertex center = CenterInspector.getCenter(graph);
    if (center == null)
      return false;
    highlightedVertices.add(center);
    redraw();
    return true;

  }

  public void computeBandwidth() {
    BandwidthInspector<DefaultVertex, DefaultEdge<DefaultVertex>> bwalgo = new BandwidthInspector<DefaultVertex, DefaultEdge<DefaultVertex>>(
        graph);
    AlgoWrapper<Integer> algo = new AlgoWrapper<Integer>(activity, bwalgo, "Bandwidth") {

      @Override
      protected String resultText(Integer result) {
        clearAll();
        redraw();
        if (result < 0) {
          return "Unable to perform bandwidth computation.  Try with n < 13.";
        }
        return "Bandwidth is " + result;
      }
    };
    algo.setTitle("Computing bandwidth ...");
    algo.execute();
  }

  public void centralize() {
    DefaultVertex center = CenterInspector.getCenter(graph);
    if (center == null)
      return;
    Matrix transformMatrix = view.getTransformMatrix();
    Coordinate moveVector = center.getCoordinate().moveVector(CENTER_COORDINATE);
    transformMatrix.postTranslate(moveVector.getX(), moveVector.getY());
    redraw();
    return;
  }

  public int showClawDeletion() {
    Collection<DefaultEdge<DefaultVertex>> edges = ClawInspector.minimalClawDeletionSet(graph);
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
    Collection<List<DefaultVertex>> cycles = CycleInspector.findAllC4(graph);
    clearAll();
    for (List<DefaultVertex> cycle : cycles) {
      for (int i = 0; i < cycle.size(); i++) {
        DefaultVertex v = cycle.get(i % cycle.size());
        DefaultVertex u = cycle.get((i + 1) % cycle.size());
        highlightedVertices.add(v);
        highlightedVertices.add(u);
        if (graph.containsEdge(v, u)) {
          DefaultEdge<DefaultVertex> e = graph.getEdge(v, u);
          markedEdges.add(e);
        } else {
          System.err.println("Strange, lacks edge for v=" + v + ", u=" + u);
          System.err.println(cycle);
        }
      }
    }
    return cycles.size();
  }

  public String makeInfo() {
    info = GraphInformation.graphInfo(graph);
    return info;
  }

  public String graphInfo() {
    if (graphWithMemory.graphChangedSinceLastCheck()) {
      info = GraphInformation.graphInfo(graph);
    }
    return info;
  }

  public void redraw() {

    if (graph == null) {
      return;
    }

    for (DefaultVertex v : graph.vertexSet()) {
      v.setLabel(""); // todo fix
    }
    for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
      e.setStyle(EdgeStyle.SOLID); // todo fix
    }

    for (DefaultVertex v : graph.vertexSet()) {
      v.setColor(EDGE_DRAW_MODE ? DEFAULT_VERTEX_COLOR : TOUCHED_VERTEX_COLOR);
      if (highlightedVertices.contains(v)) {
        v.setColor(MARKED_VERTEX_COLOR);
      }
    }

    if (!colourMap.isEmpty()) {
      for (DefaultVertex v : colourMap.keySet()) {
        v.setColor(colourMap.get(v));

      }
    } else {
      for (DefaultVertex v : graph.vertexSet()) {
        v.setColor(EDGE_DRAW_MODE ? DEFAULT_VERTEX_COLOR : TOUCHED_VERTEX_COLOR);
        if (highlightedVertices.contains(v)) {
          v.setColor(MARKED_VERTEX_COLOR);
        }
        if (userSelectedVertices.contains(v)) {
          v.setColor(USERSELECTED_VERTEX_COLOR);
        }
      }
    }
    for (DefaultEdge<DefaultVertex> e : graph.edgeSet()) {
      e.setColor(DEFAULT_EDGE_COLOR);
      if (markedEdges.contains(e)) {
        e.setStyle(EdgeStyle.BOLD);
      }
    }

    view.redraw(graphInfo(), graph);
  }

  /**
   * Returns the coordinate the given point/coordinate on the screen represents
   * in the graph
   */
  private Coordinate translateCoordinate(Coordinate screenCoordinate) {

    float[] screenPoint = { screenCoordinate.getX(), screenCoordinate.getY() };
    Matrix invertedTransformMatrix = new Matrix();

    view.getTransformMatrix().invert(invertedTransformMatrix);
    invertedTransformMatrix.mapPoints(screenPoint);

    return new Coordinate(screenPoint[0], screenPoint[1]);
  }

  private class PrivateGestureListener extends SimpleOnGestureListener {
    /** This vertex was touch, e.g. for scrolling and moving purposes */
    private DefaultVertex touchedVertex = null;

    /** This is set to the coordinate of the vertex we started move */
    private Coordinate startCoordinate = null;
    private int previousPointerCount = 0;
    private Coordinate[] previousPointerCoords = null;

    public boolean onDown(MotionEvent e) {
      trashCan(0);
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

    public Coordinate clearCoordinate() {
      Coordinate ret = startCoordinate;
      startCoordinate = null;
      return ret;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      trashCan(0);
      Coordinate sCoordinate = new Coordinate(e2.getX(), e2.getY());
      Coordinate gCoordinate = translateCoordinate(sCoordinate);
      DefaultVertex hit = getClosestVertex(gCoordinate, USER_MISS_RADIUS);

      float dist = (float) Math.round(Math.sqrt((velocityX * velocityX) + (velocityY * velocityY)));

      if (dist < 4000)
        return true;

      if (hit != null) {
        clearAll();
        graphWithMemory.removeVertex(hit);
        touchedVertex = null;
        redraw();
        return true;
      }

      return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      trashCan(0);
      if (EDGE_DRAW_MODE) {

        Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
        Coordinate gCoordinate = translateCoordinate(sCoordinate);
        DefaultVertex hit = getClosestVertex(gCoordinate, USER_MISS_RADIUS);

        if (hit == null) {
          clearAll();
          touchedVertex = null;
          redraw();
          return true;
        }
      } else {

        Coordinate sCoordinate = new Coordinate(e.getX(), e.getY());
        Coordinate gCoordinate = translateCoordinate(sCoordinate);
        DefaultVertex hit = getClosestVertex(gCoordinate, USER_MISS_RADIUS);

        if (hit == null) {
          DefaultVertex newvertex = new DefaultVertex(gCoordinate);
          graphWithMemory.addVertex(newvertex);
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
      trashCan(0);
      vibrator.vibrate(50);
      toggleEdgeDraw();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      switch (e2.getPointerCount()) {
      case 2:
        trashCan(0);
        if (previousPointerCoords == null || previousPointerCount != 2) {
          previousPointerCoords = new Coordinate[2];
          previousPointerCoords[0] = new Coordinate(e2.getX(0), e2.getY(0));
          previousPointerCoords[1] = new Coordinate(e2.getX(1), e2.getY(1));
        } else {
          Coordinate[] newCoords = { new Coordinate(e2.getX(0), e2.getY(0)), new Coordinate(e2.getX(1), e2.getY(1)) };
          Coordinate VectorPrevious = previousPointerCoords[1].subtract(previousPointerCoords[0]);
          Coordinate VectorNew = newCoords[1].subtract(newCoords[0]);
          float diffAngle = VectorNew.angle() - VectorPrevious.angle();
          float scale = VectorNew.length() / VectorPrevious.length();

          // the transformations
          view.getTransformMatrix().postTranslate(-previousPointerCoords[0].getX(), -previousPointerCoords[0].getY());
          view.getTransformMatrix().postRotate(diffAngle);
          view.getTransformMatrix().postScale(scale, scale);
          view.getTransformMatrix().postTranslate(newCoords[0].getX(), newCoords[0].getY());

          previousPointerCoords = newCoords;
        }
        break;
      case 1:
        if (EDGE_DRAW_MODE) {
          Coordinate sCoordinate = new Coordinate(e2.getX(), e2.getY());
          Coordinate gCoordinate = translateCoordinate(sCoordinate);
          DefaultVertex hit = getClosestVertex(gCoordinate, USER_MISS_RADIUS);

          if (hit != null) {
            // System.out.println("HIT " + hit.getId());
            if (touchedVertex != null && touchedVertex != hit) {
              DefaultEdge<DefaultVertex> edge = toggleEdge(hit, touchedVertex);
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
            if (startCoordinate == null) {
              startCoordinate = touchedVertex.getCoordinate();
            }

            trashCan(1);

            Coordinate sCoordinate = new Coordinate(e2.getX(), e2.getY());

            if (view.isOnTrashCan(sCoordinate)) {
              trashCan(2);
              deleteVertex = touchedVertex;
            } else {
              trashCan(1);
              deleteVertex = null;

            }

            Coordinate gCoordinate = translateCoordinate(sCoordinate);
            touchedVertex.setCoordinate(gCoordinate);

          } else {
            trashCan(0);
            if (previousPointerCount == 1)
              view.getTransformMatrix().postTranslate(-distanceX, -distanceY);
          }

        }
        break;
      default: // 3 or more
        trashCan(0);
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
