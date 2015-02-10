package no.uib.ii.algo.st8.interval;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * The standard O(n^2) Lex-BFS to clique chain algorithm. Performs n iterations
 * of LBFS+ by Corneil.
 * 
 * Might be O(n^3) since we do stupid things while getting lexicographically
 * largest elements.
 * 
 * @author Pål Grønås Drange
 * 
 */
public class LBFSPlus {

  /**
   * This returns a perfect elimination ordering as per n iterations of LBFS+.
   * The ordering returned is a PEO if and only if g is chordal. Furthermore, if
   * g is an interval graph, this PEO can be used to construct a clique chain.
   * 
   * @param g
   *          a graph
   * @return a perfect elimination ordering iff g is chordal.
   */
  public static List<Integer> getPerfectEliminationOrdering(BasicGraph g) {
    ArrayList<Integer> peo = lexBfsPlus(g);
    Collections.reverse(peo);
    return peo;
  }

  /**
   * Reverse this to get a Perfect Elimination Ordering!
   * 
   * Quadratic time for reals!
   * 
   * @param graph
   * @return
   */
  private static ArrayList<Integer> lexBfsPlus(BasicGraph graph) {
    graph = graph.clone();
    ArrayList<Integer> peo = new ArrayList<Integer>(graph.getVertices());

    for (int i = 0; i < graph.order(); i++)
      peo = lexBfsPlus(graph, peo);

    return peo;
  }

  private static ArrayList<Integer> lexBfsPlus(BasicGraph graph, ArrayList<Integer> peo) {
    int n = graph.order();

    /** label(v) = null if v has been put into pi! */
    HashMap<Integer, ArrayList<Integer>> label = new HashMap<Integer, ArrayList<Integer>>();

    for (Integer v : peo)
      label.put(v, new ArrayList<Integer>());

    ArrayList<Integer> pi = new ArrayList<Integer>();

    for (int i = n; i > 0; i--) {
      int v = lexicographicallyLargest(label, peo);
      if (v == -1) {
        System.err.println(label);
      }
      pi.add(v);
      // System.out.println(v + " → " + (n + 1 - i));
      label.remove(v);
      for (int u : graph.getNeighborhood(v))
        if (label.containsKey(u))
          label.get(u).add(i);
    }

    return pi;
  }

  /**
   * Get a vertex with lexicographic largest label, of which is latest in peo
   */
  private static int lexicographicallyLargest(HashMap<Integer, ArrayList<Integer>> label, ArrayList<Integer> peo) {
    ArrayList<Integer> largestLabel = lexicographicallyLargest(label.values());
    if (largestLabel == null)
      return -1;
    // System.out.println("Largest label: " + largestLabel);
    for (int i = peo.size() - 1; i >= 0; i--) {
      int v = peo.get(i);
      if (largestLabel.equals(label.get(v)))
        return v;
    }

    // this doesn't happen, I think
    return -1;
  }

  private static ArrayList<Integer> lexicographicallyLargest(Collection<ArrayList<Integer>> S) {
    if (S.isEmpty())
      return null;
    ArrayList<Integer> currentMax = S.iterator().next();
    for (ArrayList<Integer> current : S) {
      currentMax = getLargest(current, currentMax);
    }
    return currentMax;
  }

  private static ArrayList<Integer> getLargest(ArrayList<Integer> a, ArrayList<Integer> b) {
    if (a == null)
      return b;
    if (b == null)
      return a;
    for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
      if (a.get(i) > b.get(i)) {
        return a;
      } else if (b.get(i) > a.get(i)) {
        return b;
      }
    }
    return (a.size() > b.size() ? a : b);
  }
}