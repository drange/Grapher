package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

public class ThresholdInspector<V, E> extends Algorithm<V, E, Set<V>> {
  private final ArrayList<V> lst = new ArrayList<V>();

  public ThresholdInspector(SimpleGraph<V, E> graph) {
    super(graph);
    lst.addAll(graph.vertexSet());
  }

  private boolean isEdge(int a, int b) {
    V va = lst.get(a);
    V vb = lst.get(b);
    return graph.getEdge(va, vb) != null;
  }

  private boolean isC4(int a, int b, int c, int d) {
    boolean c4 = isEdge(a, b) && isEdge(b, c) && isEdge(c, d) && isEdge(d, a);
    return c4 && !isEdge(a, c) && !isEdge(b, d);
  }

  private boolean isP4(int a, int b, int c, int d) {
    boolean p4 = isEdge(a, b) && isEdge(b, c) && isEdge(c, d);
    return p4 && !isEdge(a, c) && !isEdge(b, d) && !isEdge(d, a);
  }

  private boolean is2K2(int a, int b, int c, int d) {
    boolean tk2 = isEdge(a, b) && isEdge(c, d);
    return tk2 && !isEdge(a, c) && !isEdge(a, d) && !isEdge(b, c) && !isEdge(b, d);
  }

  /**
   * Returns null if aborted, an empty set if threshold and a set of four
   * vertices if not threshold.
   */
  @Override
  public Set<V> execute() {
    if (graph.vertexSet().size() <= 3)
      return new HashSet<V>();

    for (int a = 0; a < lst.size() - 3; a++) {
      for (int b = 0; b < lst.size() - 2; b++) {
        for (int c = 0; c < lst.size() - 1; c++) {
          for (int d = 0; d < lst.size(); d++) {

            if (a == b || a == c || a == d || b == c || b == d || c == d)
              continue;

            if (isC4(a, b, c, d)) {
              return obstruction(a, b, c, d);
            }
            if (isP4(a, b, c, d)) {
              return obstruction(a, b, c, d);
            }
            if (is2K2(a, b, c, d)) {
              return obstruction(a, b, c, d);
            }
            if (cancelFlag)
              return null;
          }
        }
      }
    }

    return new HashSet<V>();
  }

  public Set<V> obstruction(int a, int b, int c, int d) {
    V va = lst.get(a);
    V vb = lst.get(b);
    V vc = lst.get(c);
    V vd = lst.get(d);
    HashSet<V> set = new HashSet<V>();
    set.add(va);
    set.add(vb);
    set.add(vc);
    set.add(vd);
    return set;
  }

}
