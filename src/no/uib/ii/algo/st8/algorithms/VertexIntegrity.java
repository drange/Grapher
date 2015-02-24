package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import no.uib.ii.algo.st8.algorithms.VertexIntegrity.VertexIntegritySolution;
import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

public class VertexIntegrity<V, E> extends Algorithm<V, E, VertexIntegritySolution<V>> {

  public VertexIntegrity(SimpleGraph<V, E> graph) {
    super(graph);
    setProgressGoal(graph.vertexSet().size());
  }

  @Override
  public VertexIntegritySolution<V> execute() {
    if (graph.edgeSet().isEmpty()) {
      return VertexIntegritySolution.make(new HashSet<V>(), 1);
    }
    setProgressGoal(graph.vertexSet().size());

    for (int i = 1; i < graph.vertexSet().size(); i++) {
      if (cancelFlag)
        return null;

      setCurrentProgress(i);
      progress(i, graph.vertexSet().size());

      System.out.println("Vertex integrity iteration " + i + " of " + graph.vertexSet().size());

      Collection<V> X = vertexIntegrity(i, new HashSet<V>(i + 1));
      if (X != null) {
        // System.out.println("Found X with p " + i + ": " + X);
        return VertexIntegritySolution.make(X, i);
      }
    }
    new IllegalStateException("Should not come here for " + graph).printStackTrace();
    return null;
  }

  /**
   * Can we delete X s.t. max component of G-X has size at most p-|X|?
   * 
   * @return null if no, X if yes
   */
  public HashSet<V> vertexIntegrity(int p, HashSet<V> X) {
    if (cancelFlag)
      return null;

    if (p - X.size() < 0) {
      return null;
    }

    // System.out.println("vertexIntegrity " + p + " " + X);

    HashSet<V> large = findLargeComponent(p - X.size(), X);

    if (large == null) {
      // could not find any large components!
      // System.out.println("No large components (" + p + "), so return " + X);
      return X;
    } else {
      // found large, branch on each of the p+1 vertices
      for (V v : large) {
        if (X.contains(v)) {
          new IllegalStateException(X.toString() + " contains " + v).printStackTrace();
        }
        X.add(v);
        HashSet<V> OPT = vertexIntegrity(p, X);
        if (OPT != null) {
          OPT.add(v);
          OPT.addAll(X);
          // System.out.println("\n\n\t\t\tFound solution " + OPT);
          return OPT;
        } else {
          X.remove(v);
        }
      }
    }
    return null;
  }

  /**
   * Finds and returns p+1 vertices which induce a connected graph, or null.
   */
  private HashSet<V> findLargeComponent(int p, HashSet<V> deleted) {
    // System.out.print("findLargeComponent " + p + " " + deleted);

    if (p < 0)
      return new HashSet<V>();
    if (p == 0) {
      if (graph.vertexSet().size() > deleted.size()) {
        HashSet<V> ret = new HashSet<V>();
        HashSet<V> allVertices = new HashSet<V>(graph.vertexSet());
        allVertices.removeAll(deleted);
        ret.add(allVertices.iterator().next());
        // System.out.println(" return " + ret);
        return ret;
      } else {
        // System.out.println(" return " + null);
        return null;

      }
    }

    HashSet<V> visited = new HashSet<V>();
    LinkedList<V> queue = new LinkedList<V>();
    ArrayList<V> allVertices = new ArrayList<V>(graph.vertexSet());
    allVertices.removeAll(deleted);

    while (!allVertices.isEmpty()) {
      // System.out.println("\tfindLargeComponent a" + allVertices);

      HashSet<V> currentComponent = new HashSet<V>(p + 1);

      // starting a new bfs
      queue.add(allVertices.iterator().next());
      while (!queue.isEmpty()) {
        // System.out.println("\t\tfindLargeComponent q" + queue + " d" +
        // deleted);

        if (cancelFlag) {
          // System.out.println(" return " + null + " (canceled)");

          return null;
        }

        V v = queue.remove();
        visited.add(v);
        currentComponent.add(v);
        allVertices.remove(v);

        Set<V> nv = Neighbors.openNeighborhood(graph, v);
        nv.removeAll(deleted);
        // System.out.println("\t\tN(" + v + ") = " + nv + " ... we deleted " +
        // deleted);
        if (currentComponent.size() + nv.size() > p + 1) {
          while (currentComponent.size() <= p + 1 && !nv.isEmpty()) {
            V x = nv.iterator().next();
            currentComponent.add(x);
            nv.remove(x);
          }
        }
        for (V x : nv) {
          if (!visited.contains(x))
            queue.add(x);
        }
      }
      if (currentComponent.size() > p) {
        // System.out.println("\t\t\tFound large component: " +
        // currentComponent);
        // System.out.println(" return " + currentComponent);
        return currentComponent;
      }
      allVertices.removeAll(visited);
    }
    // System.out.println("ending findLargeComponent without success " + p);
    // System.out.println("d" + deleted);
    // System.out.println("a" + allVertices);
    // System.out.println("q" + queue);
    // System.out.println(" return " + null);
    return null;
  }

  public static class VertexIntegritySolution<U> {
    public Collection<U> X;
    public int p;

    protected static <U> VertexIntegritySolution<U> make(Collection<U> u, int p) {
      VertexIntegritySolution<U> vis = new VertexIntegritySolution<U>();
      vis.X = u;
      vis.p = p;
      return vis;
    }

    @Override
    public String toString() {
      return "VI solution: " + p + " → " + X;
    }
  }

}
