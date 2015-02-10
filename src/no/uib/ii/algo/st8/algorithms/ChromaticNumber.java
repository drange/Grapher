package no.uib.ii.algo.st8.algorithms;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.util.NChooseKIterator;
import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

/**
 * (in comments ISet = independent set) Class that contains methods for
 * calculating a graphs chromatic number. There is also a method for dividing a
 * graph into k ISets each of separate colour that together cover the graph.
 * Methods are based on "Inclusion-Exclusion based algorithms for graph
 * colouring" by Andreas Björklund and Thore Husfeldt.
 * 
 * @author Håvard Haug
 * 
 */
public class ChromaticNumber<V, E> extends Algorithm<V, E, Integer> {

  public ChromaticNumber(SimpleGraph<V, E> graph) {
    super(graph);
    // TODO Auto-generated constructor stub
  }

  private BigInteger[] fib;

  public Integer execute() {
    return getChromaticNumber(graph);
  }

  /**
   * Method that computes the chromatic number of a graph
   * 
   * @param graph
   * @return chromatic number
   */
  public <V, E> int getChromaticNumber(SimpleGraph<V, E> graph) {
    int n = graph.vertexSet().size();

    setProgressGoal(n + 1);
    // no vertices can be coloured by 0 colours
    if (graph.vertexSet().isEmpty()) {
      return 0;
    }

    // an ISet can be coloured by the same colour
    if (graph.edgeSet().isEmpty()) {
      return 1;
    }

    // check for bipartition by other algorithm as it
    // is much faster
    if (BipartiteInspector.isBipartite(graph)) {
      return 2;
    }

    // calculate lookup table for Fibonacci
    // series for use in small degree ISet counter

    fib = new BigInteger[n];
    fib[0] = new BigInteger("1");
    fib[1] = new BigInteger("2");
    for (int i = 2; i < n; i++) {
      fib[i] = fib[i - 1].add(fib[i - 2]);
    }

    BigInteger[] sums = chromatic(graph);

    /*
     * find the chromatic number by binary search reducing the total runtime.
     * locates smallest k for which a colouring is possible.
     */
    int upper = n;
    int lower = 3;
    while (upper > lower) {
      int mid = (upper + lower) / 2;
      if (sums[mid].compareTo(BigInteger.ZERO) == 1) {
        upper = mid;
      } else {
        lower = mid + 1;
      }
    }
    increaseProgress();
    return upper;
  }

  /**
   * Number of ways to cover graph with k ISets inclusion-exclusion formula from
   * previously mentioned paper.
   * 
   * @param graph
   *          to be covered
   * @param k
   *          number of ISets
   * @return Number of ways to cover graph with k ISets
   */
  private <V, E> BigInteger[] chromatic(SimpleGraph<V, E> graph) {
    int n = graph.vertexSet().size();
    BigInteger sums[] = new BigInteger[n + 1];
    for (int i = 0; i <= n; i++) {
      sums[i] = new BigInteger("0");
    }
    BigInteger neg = new BigInteger("-1");
    for (int i = 0; i <= n; i++) {
      NChooseKIterator<V> nk;
      nk = new NChooseKIterator<V>(graph.vertexSet(), i);
      while (nk.hasNext()) {
        Collection<V> currSet = nk.next();
        SimpleGraph<V, E> currGraph;
        currGraph = (SimpleGraph<V, E>) graph.clone();
        currGraph.removeAllVertices(currSet);
        BigInteger nrISets = countISets(currGraph);
        // Remove empty set from count
        // as not legal ISet according to paper
        nrISets = nrISets.add(neg);
        for (int j = 0; j <= n; j++) {
          sums[j] = sums[j].add(neg.pow(currSet.size()).multiply(nrISets.pow(j)));
        }

      }
      increaseProgress();
    }
    return sums;
  }

  /**
   * Counts the number of ISets in the graph by branching on vertices of degree
   * over 2, and using a faster method once no over 2 degree vertices remain.
   * 
   * @param graph
   * @return number of ISets in the graph
   */
  private <V, E> BigInteger countISets(SimpleGraph<V, E> newgraph) {
    SimpleGraph<V, E> graph;
    graph = (SimpleGraph<V, E>) newgraph.clone();
    V v = null;
    Set<V> tmpvset = graph.vertexSet();
    for (V tmpv : tmpvset) {
      if (graph.degreeOf(tmpv) > 2) {
        v = tmpv;
        break;
      }
    }
    if (v == null) {
      return countSmallDegISets(graph);
    } else {
      Collection<V> openNeighbourhood;
      openNeighbourhood = Neighbors.openNeighborhood(graph, v);
      graph.removeVertex(v);
      BigInteger ISetWithoutV = countISets(graph);
      graph.removeAllVertices(openNeighbourhood);
      BigInteger ISetWithV = countISets(graph);
      return ISetWithoutV.add(ISetWithV);
    }
  }

  /**
   * Counts the number of ISets that can be formed by a graph of vertices when
   * all vertices have degree <= 2. this is done by finding the number of ISets
   * that each connected component of the graph can form, then multiplying these
   * values.
   * 
   * @param newgraph
   *          graph with no > 2 degree vertices
   * @return number of ISets that can be formed within the graph
   */
  private <V, E> BigInteger countSmallDegISets(SimpleGraph<V, E> newgraph) {
    SimpleGraph<V, E> graph;
    graph = (SimpleGraph<V, E>) newgraph.clone();
    Set<V> remaining = new HashSet<V>(graph.vertexSet());
    BigInteger isets = new BigInteger("1");
    // Discover degree 0 vertices
    for (V v : remaining) {
      if (graph.degreeOf(v) == 0) {
        isets = isets.multiply(fib[1]);
        graph.removeVertex(v);
      }
    }
    remaining = new HashSet<V>(graph.vertexSet());
    // Discover paths
    Set<V> used = new HashSet<V>();
    for (V v : remaining) {
      if (used.contains(v)) {
        continue;
      }
      if (graph.degreeOf(v) == 1) {
        int counter = 1;
        V currV = v;
        V preV = v;
        used.add(currV);
        Set<E> edgeSet = new HashSet<E>(graph.edgesOf(currV));
        do {
          counter++;
          for (E tmpe : edgeSet) {
            V v1 = Neighbors.opposite(graph, currV, tmpe);
            if (!v1.equals(preV)) {
              preV = currV;
              currV = v1;
              break;
            }
          }
          edgeSet = new HashSet<E>(graph.edgesOf(currV));
          edgeSet.remove(graph.getEdge(currV, preV));
          used.add(currV);
        } while (!edgeSet.isEmpty());
        isets = isets.multiply(fib[counter]);
      }
    }
    // Remove all vertices in paths
    for (V v : used) {
      graph.removeVertex(v);
    }
    remaining = new HashSet<V>(graph.vertexSet());
    used = new HashSet<V>();

    // Discover cycles
    for (V tmpv : remaining) {
      if (used.contains(tmpv)) {
        continue;
      }
      int counter = 0;
      V currV = tmpv;
      V preV = tmpv;
      used.add(currV);
      Set<E> edgeSet = new HashSet<E>(graph.edgesOf(currV));
      while (!(counter > 0 && currV.equals(tmpv))) {
        counter++;
        for (E tmpe : edgeSet) {
          V v1 = Neighbors.opposite(graph, currV, tmpe);
          if (!v1.equals(preV)) {
            preV = currV;
            currV = v1;
            break;
          }
        }
        edgeSet = new HashSet<E>(graph.edgesOf(currV));
        edgeSet.remove(graph.getEdge(currV, preV));
        used.add(currV);
      }
      isets = isets.multiply(fib[counter - 1].add(fib[counter - 3]));
    }
    return isets;

  }

}