package no.uib.ii.algo.st8.interval;
/**
 * 
 * A simple small set of graph classes used to test interval-graphs.
 * 
 * @author Pål Grønås Drange
 * 
 */

public final class GraphClasses {
  public static BasicGraph unit() {
    BasicGraph g = new BasicGraph();
    g.addVertex();
    return g;
  }

  public static BasicGraph empty() {
    BasicGraph g = new BasicGraph();
    return g;
  }

  public static BasicGraph edge() {
    BasicGraph g = new BasicGraph();
    g.addEdge(1, 2);
    return g;
  }

  public static BasicGraph cycle(int n) {
    if (n == 0)
      return empty();
    if (n == 1)
      return unit();
    if (n == 2)
      return edge();

    BasicGraph c = new BasicGraph();
    for (int i = 1; i < n; i++) {
      c.addEdge(i, i + 1);
    }
    c.addEdge(1, n);
    c.setName("C_" + n);
    return c;
  }

  public static BasicGraph antihabib() {
    // [1-3, 1-6, 2-3, 3-4, 3-5, 3-6, 3-8, 4-8, 5-8, 6-7, 6-8]

    BasicGraph g = new BasicGraph();

    g.addEdge(1, 3);
    g.addEdge(1, 6);
    g.addEdge(2, 3);
    g.addEdge(4, 3);
    g.addEdge(5, 3);
    g.addEdge(6, 3);
    g.addEdge(8, 3);
    g.addEdge(4, 8);
    g.addEdge(5, 8);
    g.addEdge(6, 7);
    g.addEdge(6, 8);

    g.setName("Habib counter-example");
    return g;
  }

  public static BasicGraph cricket() {
    BasicGraph g = GraphClasses.complete(3);
    g.addEdge(1, 4);
    g.addEdge(1, 5);
    g.setName("Cricket");
    return g;
  }

  public static BasicGraph longcricket() {
    BasicGraph longcricket = GraphClasses.cricket();

    longcricket.addEdge(1, 2);
    longcricket.addEdge(1, 3);
    longcricket.addEdge(2, 3);
    longcricket.addEdge(4, 3);
    longcricket.addEdge(4, 6);
    longcricket.addEdge(5, 3);
    longcricket.addEdge(5, 7);

    longcricket.setName("Longcricket");
    return longcricket;
  }

  public static BasicGraph longhorn() {
    BasicGraph longhorn = new BasicGraph();

    longhorn.addEdge(1, 2);
    longhorn.addEdge(3, 2);
    longhorn.addEdge(3, 4);
    longhorn.addEdge(3, 7);

    longhorn.addEdge(7, 4);
    longhorn.addEdge(5, 4);
    longhorn.addEdge(5, 6);

    longhorn.setName("Longhorn");
    return longhorn;
  }

  public static BasicGraph habib() {
    BasicGraph g = new BasicGraph();
    g.setName("Habib");
    g.addEdge(2, 8);
    g.addEdge(5, 8);
    g.addEdge(4, 8);
    g.addEdge(3, 8);
    g.addEdge(7, 8);
    g.addEdge(6, 8);
    g.addEdge(6, 1);
    g.addEdge(4, 7);
    g.addEdge(6, 7);
    g.addEdge(7, 5);
    g.addEdge(3, 6);
    return g;
  }

  public static BasicGraph pgdChordal() {
    BasicGraph g = new BasicGraph();
    g.addEdge(1, 2);
    g.addEdge(2, 3);
    g.addEdge(3, 4);
    g.addEdge(4, 1);
    g.addEdge(4, 2); // chord which makes diamond

    g.addEdge(1, 5);
    g.addEdge(5, 6);
    g.addEdge(6, 7);
    g.addEdge(7, 8);
    g.addEdge(8, 6);

    g.addEdge(3, 9);
    g.addEdge(10, 9);

    g.addEdge(1, 11);
    g.addEdge(4, 11);
    g.addEdge(12, 11);
    g.addEdge(12, 13);

    g.addEdge(2, 14);
    g.addEdge(15, 14);
    g.addEdge(2, 16);
    g.addEdge(2, 17);
    g.addEdge(11, 18);
    return g;
  }

  public static BasicGraph wheel(int n) {
    if (n <= 3) {
      return GraphClasses.cycle(n);
    }
    BasicGraph c = cycle(n - 1);
    c.addUniversalVertex();
    c.setName("W_" + n);
    return c;
  }

  public static BasicGraph path(int n) {

    if (n == 0)
      return empty();
    if (n == 1)
      return unit();
    if (n == 2)
      return edge();

    BasicGraph c = new BasicGraph();
    for (int i = 1; i < n; i++) {
      c.addEdge(i, i + 1);
    }
    c.setName("P_" + n);
    return c;
  }

  public static BasicGraph fan(int n) {
    BasicGraph c = path(n - 1);
    c.addUniversalVertex();
    c.setName(n + "-fan");
    return c;
  }

  public static BasicGraph independent(int n) {
    BasicGraph c = new BasicGraph();
    for (int i = 1; i <= n; i++) {
      c.addVertex();
    }
    c.setName("I_" + n);
    return c;
  }

  public static BasicGraph star(int n) {

    if (n == 0)
      return empty();
    if (n == 1)
      return unit();
    if (n == 2)
      return edge();

    BasicGraph c = new BasicGraph();
    for (int i = 2; i <= n; i++) {
      c.addEdge(1, i);
    }
    c.setName("S_" + n);
    return c;
  }

  public static BasicGraph complete(int n) {

    if (n == 0)
      return empty();
    if (n == 1)
      return unit();
    if (n == 2)
      return edge();

    BasicGraph c = new BasicGraph();
    for (int i = 1; i <= n; i++) {
      for (int j = i + 1; j <= n; j++) {
        c.addEdge(i, j);
      }
    }
    c.setName("K_" + n);
    return c;
  }

  public static BasicGraph biclique(int a, int b) {
    BasicGraph c = new BasicGraph();
    for (int i = 1; i <= a; i++) {
      for (int j = 1; j <= b; j++) {
        c.addEdge(i, j);
      }
    }
    c.setName("K_{" + a + "," + b + "}");
    return c;
  }

  public static BasicGraph petersen() {
    BasicGraph g = new BasicGraph();
    g.addEdge(1, 2);
    g.addEdge(2, 3);
    g.addEdge(3, 4);
    g.addEdge(4, 5);
    g.addEdge(5, 6);
    g.addEdge(6, 7);
    g.addEdge(7, 8);
    g.addEdge(7, 9);
    g.addEdge(9, 10);
    g.addEdge(2, 6);
    g.addEdge(3, 9);
    g.addEdge(1, 8);
    g.addEdge(1, 10);
    g.addEdge(8, 4);

    g.setName("Petersen");
    return g;
  }

}
