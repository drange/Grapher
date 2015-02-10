package no.uib.ii.algo.st8.interval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

/**
 * 
 * @author Pål Grønås Drange
 * 
 */
public class CliqueTree {

  private final HashMap<Integer, HashSet<Integer>> bags = new HashMap<Integer, HashSet<Integer>>();
  private HashMap<Integer, Integer> uplinks = new HashMap<Integer, Integer>();
  private HashMap<Integer, HashSet<Integer>> backlinks = new HashMap<Integer, HashSet<Integer>>();

  private List<Integer> peo = null;

  private HashMap<HashSet<Integer>, Integer> parentBags = new HashMap<HashSet<Integer>, Integer>();

  private final BasicGraph graph;

  private HashMap<Integer, HashSet<Integer>> RN;
  private HashMap<Integer, Integer> parent;

  private HashMap<Integer, HashSet<Integer>> downlinks = null;

  private void constructBacklinks() {
    backlinks.clear();
    for (Entry<Integer, Integer> e : uplinks.entrySet()) {
      if (!backlinks.containsKey(e.getValue())) {
        backlinks.put(e.getValue(), new HashSet<Integer>());
      }
      backlinks.get(e.getValue()).add(e.getKey());
    }
  }

  public CliqueTree(BasicGraph g) {
    this.graph = g.clone();
  }

  /**
   * Computes a clique tree of G provided that G is chordal and peo is a perfect
   * elimination ordering of G.
   * 
   * @param G
   * @param peo
   */
  private void computeCliqueTree() {
    HashMap<Integer, Integer> T = chordalityTree();
    // let T be some tree defined earlier

    // let r be the root of T ...
    int r = findRoot(T);

    downlinks = new HashMap<Integer, HashSet<Integer>>();

    for (Entry<Integer, Integer> e : T.entrySet()) {
      if (!downlinks.containsKey(e.getValue()))
        downlinks.put(e.getValue(), new HashSet<Integer>());
      downlinks.get(e.getValue()).add(e.getKey());
    }

    for (Integer x : graph.getVertices()) {
      if (!parent.containsKey(x))
        continue; // root
      int px = parent.get(x);
      HashSet<Integer> rnMinusParent = SetUtils.setMinus(RN.get(x), px);

      if (downlinks.containsKey(x)) {
        // not root
        if (rnMinusParent.equals(RN.get(px)))
          continue;
      }
      // else is leaf || is diff from parent's RN

      HashSet<Integer> bx = new HashSet<Integer>();
      bx.add(x);
      bx.addAll(RN.get(x));
      bags.put(x, bx);
    }

    //
    // HERE STARTS for each vertex in postorder except root:
    //
    for (Integer x : downlinks.get(r))
      postorder(x);

    // deal with root!

    uplinks = T;
    constructBacklinks();
  }

  /**
   * Returns the clique tree, with _one_ root which points to null
   * 
   * @return the clique tree of the graph
   */
  public HashMap<HashSet<Integer>, HashSet<Integer>> getCliqueTree() {

    // in case it hasn't been computed yet, we need to compute the clique tree
    if (downlinks == null) {
      computeCliqueTree();
    }

    HashMap<HashSet<Integer>, HashSet<Integer>> tree = new HashMap<HashSet<Integer>, HashSet<Integer>>();
    // parentBags
    for (HashSet<Integer> clik : parentBags.keySet()) {
      tree.put(clik, bags.get(parentBags.get(clik)));
    }

    HashSet<Integer> root = null;
    for (Entry<HashSet<Integer>, HashSet<Integer>> link : tree.entrySet()) {
      if (link.getKey().equals(link.getValue())) {
        if (root != null)
          throw new IllegalStateException("Two roots: " + root + " and " + link.getKey());
        root = link.getKey();
      }
    }
    tree.put(root, null);
    return tree;
  }

  public List<Integer> getPeo() {
    if (peo == null) {
      computeCliqueTree();
    }
    return peo;
  }

  private void postorder(int x) {
    if (downlinks.containsKey(x))
      for (Integer cx : downlinks.get(x))
        postorder(cx);

    int px = parent.get(x);
    HashSet<Integer> rnMinusParent = SetUtils.setMinus(RN.get(x), px);
    HashSet<Integer> rnpx = RN.get(px);

    if (rnMinusParent.equals(rnpx)) {
      HashSet<Integer> C = new HashSet<Integer>();
      C.add(x);
      bags.put(px, bags.get(x)); // should be pointer
    } else {
      // what to do when Bag(px) = null?
      if (bags.get(px) == null) {
        bags.put(px, SetUtils.union(rnpx, px));
      }
    }

    // I moved this out of equal/nonequal test!
    parentBags.put(bags.get(x), px);
  }

  private int findRoot(HashMap<Integer, Integer> T) {
    if (T.isEmpty())
      throw new IllegalArgumentException("Empty tree");

    int root = T.entrySet().iterator().next().getValue();
    while (T.containsKey(root)) {
      root = T.get(root);
    }
    return root;
  }

  /**
   * Algorithm 3 in Habib et al
   * 
   * @param g
   *          a graph
   * @return constructs a peo and returns a directed parent-tree corresponding
   *         to a chordality tree
   */
  private HashMap<Integer, Integer> chordalityTree() {
    peo = LBFSPlus.getPerfectEliminationOrdering(graph);

    RN = new HashMap<Integer, HashSet<Integer>>(graph.order());
    parent = new HashMap<Integer, Integer>(graph.order());

    for (int i = 0; i < peo.size(); i++) {
      int x = peo.get(i);
      HashSet<Integer> nx = graph.getNeighborhood(x);

      for (int j = 0; j < i; j++) {
        int y = peo.get(j);
        nx.remove(y);
      }
      RN.put(x, nx);

      for (int j = i + 1; j < peo.size(); j++) {
        int y = peo.get(j);
        if (nx.contains(y)) {
          parent.put(x, y);
          break;
        }
      }
    }

    return parent;
  }
}