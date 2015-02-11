package no.uib.ii.algo.st8.interval;

import java.util.HashMap;
import java.util.HashSet;

import org.jgrapht.graph.SimpleGraph;

public class SimpleToBasicWrapper<V, E> {

  BasicGraph bg = new BasicGraph();
  SimpleGraph<V, E> sg = null;

  HashMap<V, Integer> vertexMap = new HashMap<V, Integer>();
  HashMap<E, Integer> edgeMap = new HashMap<E, Integer>();

  HashMap<Integer, V> vertexMapBack = new HashMap<Integer, V>();
  HashMap<Integer, E> edgeMapBack = new HashMap<Integer, E>();

  public SimpleToBasicWrapper(SimpleGraph<V, E> sg) {
    this.sg = sg;
    for (V v : sg.vertexSet()) {
      int x = bg.addVertex();
      vertexMap.put(v, x);
      vertexMapBack.put(x, v);
    }
    for (E e : sg.edgeSet()) {
      V v = sg.getEdgeSource(e);
      V u = sg.getEdgeTarget(e);
      bg.addEdge(vertexMap.get(v), vertexMap.get(u));
    }
  }

  /**
   * Returns null if none exists
   * 
   * @return
   */
  public IntervalGraph getIntervalGraph() {
    return CliqueChain.getIntervalGraph(bg);
  }

  public HashSet<V> getAT() {
    HashSet<Integer> at = bg.getAT();
    if (at == null)
      return null;

    HashSet<V> ret = new HashSet<V>(3);
    for (Integer i : at) {
      ret.add(vertexMapBack.get(i));
    }
    return ret;
  }

  public boolean isChordal() {
    return bg.isChordal();
  }

  public HashMap<Integer, V> getVertexTranslation() {
    return vertexMapBack;
  }

  public HashMap<Integer, E> getEdgeTranslation() {
    return edgeMapBack;
  }

}
