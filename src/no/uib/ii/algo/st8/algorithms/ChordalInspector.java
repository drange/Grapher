package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.interval.SimpleToBasicWrapper;

import org.jgrapht.graph.SimpleGraph;

public class ChordalInspector<V, E> extends Algorithm<V, E, Set<V>> {

  public ChordalInspector(SimpleGraph<V, E> graph) {
    super(graph);
  }

  @Override
  public Set<V> execute() {

    SimpleToBasicWrapper<V, E> wrap = new SimpleToBasicWrapper<V, E>(graph);
    if (wrap.isChordal())
      return null;

    // TODO must return witness
    return new HashSet<V>();
  }

}
