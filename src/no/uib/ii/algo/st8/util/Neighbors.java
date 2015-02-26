package no.uib.ii.algo.st8.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

public class Neighbors {

  public static <V, E> Set<V> closedNNeighborhood(SimpleGraph<V, E> graph, V vertex, int n) {
    Set<V> neighbors = new HashSet<V>(graph.degreeOf(vertex));
    neighbors.add(vertex);
    for (int i = 0; i < n; i++) {
      Set<V> newneighbors = new HashSet<V>(graph.vertexSet().size());
      for (V v : neighbors) {
        newneighbors.addAll(openNeighborhood(graph, v));
      }
      neighbors.addAll(newneighbors);
    }
    return neighbors;
  }

  public static <V, E> Set<V> openNNeighborhood(SimpleGraph<V, E> graph, V vertex, int n) {
    Set<V> neighbors = new HashSet<V>(graph.degreeOf(vertex));
    neighbors.add(vertex);
    for (int i = 0; i < n; i++) {
      Set<V> newneighbors = new HashSet<V>(graph.vertexSet().size());
      for (V v : neighbors) {
        newneighbors.addAll(openNeighborhood(graph, v));
      }
      neighbors.addAll(newneighbors);
    }
    neighbors.remove(vertex);
    return neighbors;
  }

  public static <V, E> Set<V> openNeighborhood(SimpleGraph<V, E> graph, V vertex) {
    Set<V> set = new HashSet<V>(graph.degreeOf(vertex));
    for (E edge : graph.edgesOf(vertex)) {
      set.add(opposite(graph, vertex, edge));
    }
    return set;
  }

  public static <V, E> V getNeighbor(SimpleGraph<V, E> graph, V vertex) {
    for (E edge : graph.edgesOf(vertex)) {
      return (opposite(graph, vertex, edge));
    }
    return null;
  }

  public static <V, E> Collection<V> openNeighborhood(SimpleGraph<V, E> graph, Collection<V> vertices) {
    Set<V> set = new HashSet<V>(graph.vertexSet().size());
    for (V vertex : vertices) {
      for (E edge : graph.edgesOf(vertex)) {
        V neighbor = opposite(graph, vertex, edge);
        if (!vertices.contains(neighbor)) {
          set.add(neighbor);
        }
      }
    }
    return set;
  }

  public static <V, E> Collection<V> closedNeighborhood(SimpleGraph<V, E> graph, Collection<V> vertices) {
    Set<V> set = new HashSet<V>(graph.vertexSet().size());
    for (V vertex : vertices) {
      set.add(vertex);
      for (E edge : graph.edgesOf(vertex)) {
        V neighbor = opposite(graph, vertex, edge);
        set.add(neighbor);
      }
    }
    return set;
  }

  public static <V, E> Collection<V> closedNeighborhood(SimpleGraph<V, E> graph, V vertex) {
    Set<V> set = new HashSet<V>(graph.vertexSet().size());
    set.add(vertex);
    for (E edge : graph.edgesOf(vertex)) {
      V neighbor = opposite(graph, vertex, edge);
      set.add(neighbor);
    }
    return set;
  }

  public static <V, E> V opposite(SimpleGraph<V, E> graph, V vertex, E edge) {
    if (graph.getEdgeSource(edge).equals(vertex)) {
      return graph.getEdgeTarget(edge);
    }
    return graph.getEdgeSource(edge);
  }

  public static <V, E> boolean isIncident(SimpleGraph<V, E> graph, V vertex, E edge) {
    return vertex.equals(graph.getEdgeSource(edge)) || vertex.equals(graph.getEdgeTarget(edge));
  }

  public static <V, E> boolean isIncidentEdge(SimpleGraph<V, E> graph, E edge1, E edge2) {
    V v11 = graph.getEdgeSource(edge1);
    V v12 = graph.getEdgeTarget(edge1);
    return isIncident(graph, v11, edge2) || isIncident(graph, v12, edge2);
  }

  /**
   * 
   * Ordered descending degree.
   */
  public static <V, E> List<V> orderedOpenNeighborhood(final SimpleGraph<V, E> graph, V vertex, final boolean descending) {
    Collection<V> neighborhood = openNeighborhood(graph, vertex);
    ArrayList<V> list = new ArrayList<V>(neighborhood.size());
    list.addAll(neighborhood);
    Collections.sort(list, new Comparator<V>() {
      public int compare(V arg0, V arg1) {
        if (descending)
          return graph.degreeOf(arg0) - graph.degreeOf(arg1);
        else
          return graph.degreeOf(arg1) - graph.degreeOf(arg0);
      };
    });
    return list;
  }

  public static <V, E> List<V> sortByDegree(final SimpleGraph<V, E> graph, Collection<V> vertices, final boolean ascending) {
    ArrayList<V> lst = new ArrayList<V>(vertices);
    Collections.sort(lst, new Comparator<V>() {
      @Override
      public int compare(final V a, final V b) {
        int degA = graph.degreeOf(a);
        int degB = graph.degreeOf(b);
        if (degA == degB)
          return 0;
        int ret = 1;
        if (degA < degB) // ascending
          ret = -1;
        return ascending ? ret : -ret;
      }
    });
    return lst;
  }
}
