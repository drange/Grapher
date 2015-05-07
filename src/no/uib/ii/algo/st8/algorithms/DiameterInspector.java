package no.uib.ii.algo.st8.algorithms;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

public class DiameterInspector {

  /**
   * Returns the diameter of the graph, i.e., the maximum shortest distance
   * between any two pairs of vertices in G. Returns -1 in the case that G is
   * disconnected. The latter is to be interpreted as infinite.
   * 
   * @param g
   *          the input graph
   * @return the diameter of g, or -1 if disconnected
   */
  public static <V, E> int diameter(SimpleGraph<V, E> g) {
    GraphPath<V, E> diamPath = diameterPath(g);
    if (diamPath == null)
      return -1;
    return diamPath.getEdgeList().size(); // fixed diameter error: length is
                                          // #edges
  }

  /**
   * Returns any path which is a shortest path and has length the diameter of
   * the graph, i.e., a maximum shortest path between any two pairs of vertices
   * in G. Returns null in the case that G is disconnected.
   * 
   * @param g
   *          the input graph
   * @return the diameter of g, or null if disconnected
   */
  public static <V, E> GraphPath<V, E> diameterPath(SimpleGraph<V, E> g) {

    DijkstraShortestPath<V, E> d;

    ConnectivityInspector<V, E> ci = new ConnectivityInspector<V, E>(g);
    if (!ci.isGraphConnected())
      return null;

    GraphPath<V, E> longestPath = null;
    for (V v : g.vertexSet()) {
      for (V u : g.vertexSet()) {
        if (v != u) {
          d = new DijkstraShortestPath<V, E>(g, v, u);

          GraphPath<V, E> currentPath = d.getPath();

          if (longestPath == null || longestPath.getEdgeList().size() < currentPath.getEdgeList().size()) {
            longestPath = currentPath;
          }
        }
      }
    }
    return longestPath;
  }
}
