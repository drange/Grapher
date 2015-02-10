package no.uib.ii.algo.st8.interval;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

/**
 * 
 * Lots of tests for the graph structure we use and for interval, chordal,
 * simpliciality, peo and AT-free-ness
 * 
 * @author Pål Grønås Drange
 * 
 */
public class UnitTest {

  public static boolean PRINT = false;

  public static void runmanytests(int tests, int size) throws Exception {
    System.out.print("Testing " + tests + " graphs of size " + size + "\t... ");
    long maxTimeMs = 0;
    long minTimeMs = 1000 * 1000 * 1000;
    long totalTimeMs = 0;
    for (int i = 0; i < tests; i++) {
      IntervalGraph ig = IntervalGraph.getRandomIntervalGraph(size);
      ig.addInterval(0, 102);
      BasicGraph sg = ig.getGraph();
      if (sg.order() != (size + 1))
        throw new Exception("Size = " + size + " vs order = " + sg.order());

      long start = System.nanoTime();
      CliqueChain.getIntervalGraph(sg);
      long stop = System.nanoTime();
      long nano = (stop - start) / (1000 * 1000);

      totalTimeMs += nano;
      maxTimeMs = Math.max(maxTimeMs, nano);
      minTimeMs = Math.min(minTimeMs, nano);

    }
    long avg = totalTimeMs / tests;
    System.out.println("min: " + minTimeMs + "   \tmax: " + maxTimeMs + "  \t avg: " + avg);
  }

  public static void main(String[] args) throws Exception {
    // int amount = 100;
    // for (int i = 1; i <= amount; i++) {
    // runmanytests(3, 10 * (i + 1));
    // }

    // while (true)
    pain(args);

  }

  public static void pain(String[] args) throws Exception {

    int upperbound = 19 + 1; // any graphs of 19 or more vertices should not
    // behave differently

    // paths
    unitTestChordal(GraphClasses.path(5), true);
    unitTestChordal(GraphClasses.path(5), true);
    unitTestChordal(GraphClasses.path(5), true);
    unitTestChordal(GraphClasses.path(5), true);

    // paths
    for (int i = 0; i < upperbound; i++) {
      unitTestChordal(GraphClasses.path(i), true);
      unitTestChordal(GraphClasses.path(i).complement(), i <= 4);
    }

    // fan
    for (int i = 0; i < upperbound; i++) {
      unitTestChordal(GraphClasses.fan(i), true);
      unitTestChordal(GraphClasses.fan(i).complement(), i <= 5);
    }

    // stars
    for (int i = 0; i < upperbound; i++) {
      unitTestChordal(GraphClasses.star(i), true);
      unitTestChordal(GraphClasses.star(i).complement(), true);
    }

    // complete
    for (int i = 0; i < upperbound; i++) {
      unitTestChordal(GraphClasses.complete(i), true);
      unitTestChordal(GraphClasses.complete(i).complement(), true);
    }

    // bicomplete
    for (int i = 0; i < upperbound; i++) {
      for (int j = i; j < 1; j++) {
        int a = j;
        int b = upperbound - a;
        unitTestChordal(GraphClasses.biclique(a, b), (a < 2 || b < 2));
        unitTestChordal(GraphClasses.biclique(a, b), true);
      }
    }

    // independent
    for (int i = 0; i < upperbound; i++) {
      unitTestChordal(GraphClasses.independent(i), true);
      unitTestChordal(GraphClasses.independent(i).complement(), true);
    }

    unitTestChordal(GraphClasses.petersen(), false);
    unitTestChordal(GraphClasses.petersen().complement(), false);

    // cycles
    for (int i = 0; i < upperbound; i++) {
      unitTestChordal(GraphClasses.cycle(i), i <= 3);
      unitTestChordal(GraphClasses.cycle(i).complement(), i <= 4);
    }

    // wheel
    for (int i = 0; i < upperbound; i++) {
      unitTestChordal(GraphClasses.wheel(i), i <= 4);
      unitTestChordal(GraphClasses.wheel(i).complement(), i <= 5);
    }

    Random r = new Random();
    int len = 10 + r.nextInt(10);
    BasicGraph path = GraphClasses.path(len);
    BasicGraph cliq = GraphClasses.complete(len);
    BasicGraph cycl = GraphClasses.cycle(len);
    BasicGraph inde = GraphClasses.independent(len);

    unitTestChordal(BasicGraph.disjointUnion(path, cliq), true);
    unitTestChordal(BasicGraph.disjointUnion(cycl, cliq), false);
    unitTestChordal(BasicGraph.disjointUnion(cycl, path), false);
    unitTestChordal(BasicGraph.disjointUnion(path, path), true);
    unitTestChordal(BasicGraph.disjointUnion(cycl, cycl), false);
    unitTestChordal(BasicGraph.disjointUnion(cliq, cliq), true);

    unitTestChordal(BasicGraph.join(path, cliq), true);
    unitTestChordal(BasicGraph.join(cycl, cliq), false);
    unitTestChordal(BasicGraph.join(cycl, path), false);
    unitTestChordal(BasicGraph.join(path, path), false);
    unitTestChordal(BasicGraph.join(cycl, cycl), false);
    unitTestChordal(BasicGraph.join(cliq, cliq), true);

    unitTestChordal(BasicGraph.join(path, inde), false);
    unitTestChordal(BasicGraph.join(cycl, inde), false);

    unitTestChordal(GraphClasses.habib(), true);
    unitTestChordal(GraphClasses.cricket(), true);
    unitTestChordal(GraphClasses.longcricket(), true);
    unitTestChordal(GraphClasses.longhorn(), true);

    unitTestChordal(GraphClasses.antihabib(), true);

    for (int i = 0; i < 1000; i++) {

      unitTestChordal(GraphClasses.habib().randomizeLabels(), true);
      unitTestChordal(GraphClasses.cricket().randomizeLabels(), true);
      unitTestChordal(GraphClasses.longcricket().randomizeLabels(), true);
      unitTestChordal(GraphClasses.longhorn().randomizeLabels(), true);

    }

    System.out.println("chordality ok");

    // interval
    unitTestIntervalIffChordalAndAtFree(GraphClasses.habib());
    unitTestIntervalIffChordalAndAtFree(path);
    unitTestIntervalIffChordalAndAtFree(cliq);
    unitTestIntervalIffChordalAndAtFree(cycl);

    System.out.println("interval ok");

    // shortest path

    BasicGraph pp = GraphClasses.path(upperbound);
    BasicGraph pc = GraphClasses.complete(upperbound);
    BasicGraph pu = BasicGraph.disjointUnion(pp, pc);

    for (int i = 1; i < upperbound; i++) {
      for (int j = 1; j < upperbound; j++) {
        unitTestDistance(pp, i, j, Math.abs(i - j));
        unitTestDistance(pc, i, j, (i == j ? 0 : 1));
        unitTestDistance(pu, i, upperbound + j, -1);
      }
    }

    System.out.println("shortest path ok");

    //
    // AT-FREE
    //

    unitTestATFree(GraphClasses.habib(), true);

    // path
    for (int i = 0; i < upperbound; i++) {
      unitTestATFree(GraphClasses.path(i), true);
      unitTestATFree(BasicGraph.disjointUnion(GraphClasses.path(i), GraphClasses.path(i)), true);
    }

    // clique
    for (int i = 0; i < upperbound; i++) {
      unitTestATFree(GraphClasses.complete(i), true);
    }

    // cycles
    for (int i = 0; i < upperbound; i++) {
      unitTestATFree(GraphClasses.cycle(i), i <= 5);
    }

    // wheel
    for (int i = 0; i < upperbound; i++) {
      unitTestATFree(GraphClasses.wheel(i), i <= 6);
    }
    System.out.println("AT free ok");

    System.out.println("All tests ok");
  }

  public static void unitTestDistance(BasicGraph g, int s, int t, int dist) throws Exception {
    int shortestPath = g.shortestPath(s, t);
    if (shortestPath != dist) {
      if (shortestPath == -1)
        throw new Exception("(" + s + ", " + t + ") in " + g.getName() + " was disconnected, should have distance " + dist);
      if (dist == -1)
        throw new Exception("(" + s + ", " + t + ") in " + g.getName() + " had distance " + shortestPath
            + " but should be disconnected");
      throw new Exception("(" + s + ", " + t + ") in " + g.getName() + " had distance " + shortestPath + " but should have "
          + dist);
    }
  }

  public static void unitTestATFree(BasicGraph g, boolean isAtFree) throws Exception {
    boolean wasAtFree = g.isATFree();
    if (wasAtFree != isAtFree) {
      throw new Exception(g.getName() + (wasAtFree ? " was AT-free" : " was not AT-free"));
    }
  }

  public static void unitTestCliqueTree(BasicGraph g, HashMap<HashSet<Integer>, HashSet<Integer>> cliqueTree) throws Exception {
    HashSet<Integer> root = null;
    HashSet<Integer> fullVertexSet = new HashSet<Integer>();
    for (Entry<HashSet<Integer>, HashSet<Integer>> link : cliqueTree.entrySet()) {
      HashSet<Integer> child = link.getKey();
      HashSet<Integer> parent = link.getValue();

      // child bag
      if (child == null) {
        throw new Exception("Child in tree was null for link " + link);
      } else {
        fullVertexSet.addAll(child);
        if (!g.isMaximalClique(child)) {
          throw new Exception("Child was not max clique: " + child);
        }
      }

      // parent bag
      if (parent == null) {
        // found unique root
        if (root != null)
          throw new Exception("Root was set to " + root + " but root is also " + child);
        root = child;
      } else {
        fullVertexSet.addAll(parent);
        if (!g.isMaximalClique(parent)) {
          throw new Exception("Parent was not max clique: " + parent);
        }
      }
    }
    if (!fullVertexSet.equals(g.getVertices()))
      throw new Exception("Vertex set not equal to max cliques set: " + fullVertexSet + " vs " + g.getVertices());
  }

  public static void unitTestIntervalIffChordalAndAtFree(BasicGraph g) throws Exception {
    boolean isInterval = g.isInterval();

    IntervalGraph ig = CliqueChain.getIntervalGraph(g);
    if (ig == null && isInterval) {
      throw new Exception("Interval error: " + g.getName() + " did not get interval representation, but is interval: " + g);
    }
    if (ig != null && !isInterval) {
      throw new Exception("Interval error: " + g.getName() + " got interval representation, but is not interval: " + g);
    }
    if (ig != null) {
      unitTestIntervalEquivalence(g, ig);
      if (PRINT)
        System.out.println(g.getName() + ig);
    }

    // else ig is null and g is not interval which is correct
  }

  /**
   * Throws an exception if there is a v in g s.t. , N_g(v) != N_ig(v) and the
   * other way around
   * 
   * @param g
   * @param ig
   */
  public static void unitTestIntervalEquivalence(BasicGraph g, IntervalGraph ig) throws Exception {
    if (ig == null) {
      System.err.println("\n\n\nError for " + g.getName());
      System.err.println(g.isChordal() ? "chordal" : "not chordal");
      System.err.println(g.isATFree() ? "AT-free" : "has AT");
      throw new Exception("Interval graph was null for graph " + g);
    }

    for (Integer v : g.getVertices()) {
      if (!g.getNeighborhood(v).equals(ig.getNeighborhood(v))) {
        String s = "Simple graph error: Different neighborhood for " + v;
        s += " --- N_g (v) = " + g.getNeighborhood(v);
        s += " --- N_ig(v) = " + ig.getNeighborhood(v);
        throw new Exception(s);
      }
    }

    for (Integer v : ig.getVertices()) {
      if (!ig.getNeighborhood(v).equals(g.getNeighborhood(v))) {
        String s = "Interval error: Different neighborhood for " + v;
        s += " --- N_ig(v) = " + ig.getNeighborhood(v);
        s += " --- N_g (v) = " + g.getNeighborhood(v);
        throw new Exception(s);
      }
    }
  }

  public static void unitTestChordal(BasicGraph g, boolean chordal) throws Exception {
    g = g.randomizeLabels();
    boolean wasChordal = g.isChordal();
    if (wasChordal != chordal) {
      throw new Exception(g.getName() + (wasChordal ? " was chordal. " : " was not chordal. " + g));
    }

    wasChordal = g.isChordal();
    if (wasChordal != chordal) {
      throw new Exception(g.getName()
          + (wasChordal ? " was chordal according to own simplical deletion. " : " was not chordal. " + g));
    }

    if (g.isConnected())
      unitTestIntervalIffChordalAndAtFree(g);
  }
}