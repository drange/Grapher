package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

/**
 * Minimal triangulation algorithm based on
 * "Maximum Cardinality Search for Computing Minimal Triangulations of Graphs*"
 * by Anne Berry, Jean R. S. Blair and Pinar Heggernes.
 * 
 * @author Håvard Haug
 * 
 * @param <V>
 * @param <E>
 */
public class MinimalTriangulation<V, E> extends Algorithm<V, E, Set<E>> {

  public Map<V,Integer> peoMap;
  public List<V> peoOrder;
  public MinimalTriangulation(SimpleGraph<V, E> graph) {
    super(graph);
    // TODO Auto-generated constructor stub
  }

  public Set<E> execute() {
    return minimalFill();
  }

  public Set<E> minimalFill() {
    peoMap = new HashMap<V,Integer>();
    peoOrder = new ArrayList<V>();
    // SimpleGraph<V, E> filled = new SimpleGraph<V, E>(graph.getEdgeFactory());
    Set<E> FillSet = new HashSet<E>();
    Map<V, Integer> w = new HashMap<V, Integer>();
    for (V v : graph.vertexSet()) {
      w.put(v, 0);
    }
    int n = graph.vertexSet().size();
    Map<V, Integer> numbered = new HashMap<V, Integer>();
    List<V> unNumbered = new ArrayList<V>();
    for (V v : graph.vertexSet()) {
      unNumbered.add(v);
    }
    Map<V, HashSet<V>> adjList = new HashMap<V, HashSet<V>>();
    for (int i = n; i > 0; i--) {
      V maxV = unNumbered.get(0);
      for (V v : unNumbered) {
        if (w.get(v) > w.get(maxV))
          maxV = v;
      }
      MinimumBottleneckPaths<V, E> minBot = new MinimumBottleneckPaths<V, E>(graph);
      Set<V> S = minBot.mbp(unNumbered, maxV, w);
      adjList.put(maxV, new HashSet<V>(S));
      for (V v : S) {
        w.put(v, w.get(v) + 1);
      }
      numbered.put(maxV, i);
      unNumbered.remove(maxV);
      peoMap.put(maxV, i);
      peoOrder.add(maxV);
    }
    for (V v : adjList.keySet()) {
      for (V u : adjList.get(v)) {
        if (!graph.containsEdge(v, u))
          FillSet.add(graph.addEdge(v, u));
      }
    }
    List<V> revPeo = new ArrayList<V>();
    for (int i = peoOrder.size()-1; i >= 0 ; i--) {
          revPeo.add(peoOrder.get(i));
    }
    peoOrder = revPeo;
    return FillSet;

  }

}
