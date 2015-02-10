package no.uib.ii.algo.st8.interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

/**
 * Generates a clique chain provided graph is interval
 * 
 * @author Pål Grønås Drange
 * 
 */
public class CliqueChain {

  private static ArrayList<HashSet<Integer>> getCliqueChain(HashMap<HashSet<Integer>, HashSet<Integer>> tree, BasicGraph graph,
      List<Integer> peo) {
    if (tree.containsKey(null))
      throw new NullPointerException("Tree contains null: " + tree);
    int n = graph.order();

    ArrayList<HashSet<HashSet<Integer>>> L = new ArrayList<HashSet<HashSet<Integer>>>(n);

    HashSet<HashSet<Integer>> maximalCliques = new HashSet<HashSet<Integer>>(tree.size());
    maximalCliques.addAll(tree.keySet());

    L.add(maximalCliques);
    Stack<Integer> pivots = new Stack<Integer>();
    HashSet<Integer> processed = new HashSet<Integer>(n);
    HashSet<HashSet<Integer>> caligraphicC = new HashSet<HashSet<Integer>>(n);

    // computing clique chain ... :)
    HashSet<HashSet<Integer>> Xc = null;
    while ((Xc = getNonSingleton(L)) != null) {

      // Xc is non-singleton in L

      /*
       * PIVOTS EMPTY? if so, refine Xc according to lex-last clique
       */
      if (pivots.isEmpty()) {
        // let Cl be last clique in Xc processed by Lex-BFS
        HashSet<Integer> Cl = getLastDiscovered(Xc, peo, graph);
        // done getting clique with the greatest number

        if (Cl == null) {
          System.err.println(graph);
          System.err.println("peo: " + peo);

          throw new NullPointerException("Cl was null in getCliqueChain for L = " + L);
        }

        // replace Xc by ...
        int Xci = L.indexOf(Xc);
        Xc.remove(Cl);
        HashSet<HashSet<Integer>> Clset = new HashSet<HashSet<Integer>>();
        Clset.add(Cl);
        L.add(Xci + 1, Clset);
        // done replace Xc by Xc\Cl,Cl in L

        caligraphicC = new HashSet<HashSet<Integer>>();
        caligraphicC.add(Cl);
      }

      /*
       * PIVOTS NON-EMPTY, so we don't use Xc but find Xa and Xb to refine
       */
      else {
        // pick an unprocessed vertex x in pivots (throw away processed ones)
        int x = pivots.pop();
        if (processed.contains(x))
          continue;
        processed.add(x);

        // let C (cal C) be the set of all maximal cliques containing x
        caligraphicC = new HashSet<HashSet<Integer>>();
        for (HashSet<Integer> Cx : maximalCliques) {
          if (Cx.contains(x)) {
            caligraphicC.add(Cx);
          }
        }

        // first containing x
        int xaIndex = 0;
        HashSet<HashSet<Integer>> Xa = null;
        for (int i = 0; i < L.size(); i++) {
          if (SetUtils.containsSetContaining(L.get(i), x)) {
            Xa = L.get(i);
            xaIndex = i;
            break;
          }
        }

        // last containing x
        int xbIndex = L.size() - 1;
        HashSet<HashSet<Integer>> Xb = null;
        for (int i = L.size() - 1; i >= 0; i--) {
          if (SetUtils.containsSetContaining(L.get(i), x)) {
            Xb = L.get(i);
            xbIndex = i;
            break;
          }
        }

        /*
         * DOING B FIRST so the indices for A do not change when I add in L
         */
        HashSet<HashSet<Integer>> XbLeft = new HashSet<HashSet<Integer>>();
        HashSet<HashSet<Integer>> XbRight = new HashSet<HashSet<Integer>>();
        for (HashSet<Integer> set : Xb) {
          if (caligraphicC.contains(set)) {
            XbLeft.add(set); // Xb cap calC
          } else {
            XbRight.add(set); // Xb - calC
          }
        }
        L.add(xbIndex + 1, XbRight);
        L.add(xbIndex + 1, XbLeft);
        L.remove(xbIndex);

        /*
         * DOING A UNLESS A = B
         */
        if (xaIndex != xbIndex) {
          HashSet<HashSet<Integer>> XaLeft = new HashSet<HashSet<Integer>>();
          HashSet<HashSet<Integer>> XaRight = new HashSet<HashSet<Integer>>();
          for (HashSet<Integer> set : Xa) {
            if (caligraphicC.contains(set)) {
              XaRight.add(set); // Xa cap calC
            } else {
              XaLeft.add(set); // Xa - calC
            }
          }
          L.add(xaIndex + 1, XaRight);
          L.add(xaIndex + 1, XaLeft);
          L.remove(xaIndex);
        }

      }

      /*
       * END OF PIVOT/NON-PIVOT WORK
       */

      /*
       * For each tree edge connecting a bag in calC to a bag not in calC
       */
      HashSet<HashSet<Integer>> deleteLinks = new HashSet<HashSet<Integer>>();
      for (Entry<HashSet<Integer>, HashSet<Integer>> link : tree.entrySet()) {
        HashSet<Integer> Ci = link.getKey(); // Ci is child
        HashSet<Integer> Cj = link.getValue();// Cj is parent
        if (Cj == null)
          continue;
        if (caligraphicC.contains(Ci) && !caligraphicC.contains(Cj)) {
          pivots.addAll(SetUtils.intersection(Ci, Cj));
          deleteLinks.add(Ci);
        }
      }
      for (HashSet<Integer> C : deleteLinks) {
        tree.put(C, null);
      }

      // cleaning L
      for (int i = L.size() - 1; i >= 0; i--) {
        if (L.get(i) == null || L.get(i).size() == 0) {
          L.remove(i);
        }
      }
      if (L.contains(null)) {
        System.err.println(L);
        System.exit(1);
      }
    } // end of while

    // now, chain contains only singletons!

    ArrayList<HashSet<Integer>> ret = new ArrayList<HashSet<Integer>>();
    for (int i = 0; i < L.size(); i++) {
      ret.add(L.get(i).iterator().next());
    }
    return ret;
  }

  /**
   * Tests for a given chain of cliques that every vertex appears consecutively
   * 
   * @param chain
   * @param g
   * @return
   */
  private static boolean isIntervalChain(ArrayList<HashSet<Integer>> chain, BasicGraph g) {
    // mode 0 is before, mode 1 is in mode 2 is after
    for (Integer v : g.getVertices()) {
      int mode = 0; // before
      for (int i = 0; i < chain.size(); i++) {
        if (chain.get(i).contains(v)) {
          if (mode == 2) {
            // System.out.println(v + " appears non-consecutively");
            return false;
          } else {
            mode = 1;
          }
        } else {
          // v is _no longer_ in a clique
          if (mode != 0)
            mode = 2;
        }
      }
    }
    return true;
  }

  private static HashSet<Integer> getLastDiscovered(Collection<HashSet<Integer>> cliques, List<Integer> peo, BasicGraph graph) {
    if (cliques.isEmpty())
      return null;

    for (int i = 0; i < peo.size(); i++) {
      HashSet<Integer> rnv = rightNeighborhood(peo.get(i), peo, graph);
      if (cliques.contains(rnv))
        return rnv;
    }
    return null;
  }

  private static HashSet<Integer> rightNeighborhood(int vertex, List<Integer> peo, BasicGraph graph) {
    HashSet<Integer> rnv = graph.getClosedNeighborhood(vertex);
    for (int i = 0; peo.get(i) != vertex; i++) {
      rnv.remove(peo.get(i));
    }
    return rnv;
  }

  /**
   * Returns a non-singleton, i.e. a set with size > 1 if such exists, else null
   * 
   * @param lst
   * @return
   */
  private static HashSet<HashSet<Integer>> getNonSingleton(ArrayList<HashSet<HashSet<Integer>>> lst) {
    for (HashSet<HashSet<Integer>> c : lst) {
      if (c.size() > 1) {
        return c;
      }
    }
    return null;
  }

  /**
   * Returns the interval representation for g if g is interval, otherwise it
   * returns null!
   * 
   * @param g
   * @return
   */
  public static IntervalGraph getIntervalGraph(BasicGraph g) {
    if (g.order() == 0)
      return new IntervalGraph();
    if (g.order() == 1) {
      IntervalGraph ig = new IntervalGraph();
      ig.setInterval(1, 2, g.getVertices().iterator().next());
    }

    CliqueTree ct = new CliqueTree(g);

    ArrayList<HashSet<Integer>> chain = getCliqueChain(ct.getCliqueTree(), g, ct.getPeo());

    if (!isIntervalChain(chain, g))
      return null;

    IntervalGraph ig = IntervalGraph.fromChain(chain);

    return ig;
  }
}