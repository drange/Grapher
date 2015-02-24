package no.uib.ii.algo.st8.interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import no.uib.ii.algo.st8.model.DefaultEdge;
import no.uib.ii.algo.st8.model.DefaultVertex;
import no.uib.ii.algo.st8.util.Coordinate;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

public class BasicGraph {

  private final Collection<Integer> vertices = new HashSet<Integer>();
  private final HashMap<Integer, HashSet<Integer>> neighborhoods = new HashMap<Integer, HashSet<Integer>>();

  private String name = "";

  public BasicGraph() {
  }

  /**
   * simple as that. quartic time!
   * 
   * @return
   */
  public boolean isInterval() {
    return isChordal() && isATFree();
  }

  public IntervalGraph getIntervalRepresentation() {
    return CliqueChain.getIntervalGraph(this);
  }

  public boolean isChordal(List<Integer> peo) {
    if (peo == null)
      return false;

    BasicGraph graph = clone();

    for (int i = 0; i < peo.size(); i++) {
      Integer v = peo.get(i);
      if (!graph.isSimplicial(v)) {
        // System.out.println(v + " not simplicial in peo");
        return false;
      }
      graph.removeVertex(v);
    }
    return true;
  }

  public String getName() {
    return name;
  }

  public int addUniversalVertex() {
    int u = addVertex();
    for (Integer i : vertices) {
      if (i != u)
        addEdge(i, u);
    }
    return u;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BasicGraph randomizeLabels() {
    int n = order();
    ArrayList<Integer> nVertices = new ArrayList<Integer>(n);
    BasicGraph g = new BasicGraph();
    for (int i = 1; i <= n; i++) {
      g.addVertex(i);
      nVertices.add(i);
    }
    Collections.shuffle(nVertices);
    ArrayList<Integer> orig = new ArrayList<Integer>(vertices);
    HashMap<Integer, Integer> phi = new HashMap<Integer, Integer>(n);
    for (int i = 0; i < n; i++) {
      phi.put(orig.get(i), nVertices.get(i));
    }
    for (Edge e : getEdges()) {
      g.addEdge(phi.get(e.a), phi.get(e.b));
    }
    g.setName(getName() + " (randomized)");
    return g;
  }

  public HashSet<Integer> getVertices() {
    HashSet<Integer> s = new HashSet<Integer>();
    for (Integer v : vertices) {
      s.add(v);
    }
    return s;
  }

  public boolean isSimplicial(int v) {
    boolean ret = isClique(getNeighborhood(v));
    // System.out.println("SimpleGraph.isSimplicial " + v + ": " + ret);
    return ret;
  }

  public Collection<Edge> getEdges() {
    HashSet<Edge> s = new HashSet<Edge>();
    for (Entry<Integer, HashSet<Integer>> neigh : neighborhoods.entrySet()) {
      int v = neigh.getKey();
      for (Integer nv : neigh.getValue()) {
        s.add(new Edge(v, nv));
      }
    }
    return s;
  }

  public boolean removeVertex(Integer v) {
    if (!vertices.contains(v))
      return false;

    HashSet<Integer> neigh = neighborhoods.get(v);
    for (Integer nv : neigh) {
      neighborhoods.get(nv).remove(v);
    }

    vertices.remove(v);
    neighborhoods.remove(v);

    return true;
  }

  public boolean removeVertices(Collection<Integer> delete) {
    boolean ret = false;
    while (!delete.isEmpty()) {
      int v = delete.iterator().next();
      if (vertices.contains(v)) {
        ret = true;
        removeVertex(v);
      }
      delete.remove(v);
    }
    return ret;
  }

  public int order() {
    return vertices.size();
  }

  private int firstFreeIndex() {
    int ffi = 1;
    for (Integer i : vertices) {
      ffi = Math.max(ffi, i + 1);
    }
    return ffi;
  }

  public int addVertex() {
    int n = firstFreeIndex();

    addVertex(n);

    return n;
  }

  /**
   * Does nothing (but return false) if v is in vertices
   * 
   * @param v
   * @return
   */
  public boolean addVertex(int v) {
    if (!vertices.contains(v)) {
      vertices.add(v);
      neighborhoods.put(v, new HashSet<Integer>(100));
    }
    return false;
  }

  public void addEdge(int a, int b) {
    if (a == b)
      throw new IllegalArgumentException("Not allowed to add self-loop on " + a);

    // this reduced the running time drastically
    if (vertices.contains(a) && neighborhoods.get(a).contains(b))
      return;

    if (!vertices.contains(a)) {
      vertices.add(a);
      HashSet<Integer> an = new HashSet<Integer>();
      an.add(b);
      neighborhoods.put(a, an);
    } else {
      neighborhoods.get(a).add(b);
    }

    if (!vertices.contains(b)) {
      vertices.add(b);
      HashSet<Integer> bn = new HashSet<Integer>();
      bn.add(a);
      neighborhoods.put(b, bn);
    } else {
      neighborhoods.get(b).add(a);
    }
  }

  public static BasicGraph disjointUnion(BasicGraph g1, BasicGraph g2) {
    BasicGraph g = g1.clone();
    int max = 0;
    for (Integer v : g1.vertices) {
      max = Math.max(max, v);
    }
    for (Edge e : g2.getEdges()) {
      g.addEdge(e.a + max, e.b + max);
    }

    g.setName(g1.name + " \\cup " + g2.name);

    return g;
  }

  public static BasicGraph join(BasicGraph g1, BasicGraph g2) {
    BasicGraph g = g1.clone();
    int offset = g1.order();
    for (Integer v : g1.vertices) {
      offset = Math.max(offset, v);
    }
    offset += 1;
    for (Edge e : g2.getEdges()) {
      g.addEdge(e.a + offset, e.b + offset);
    }
    for (Integer v : g1.vertices) {
      for (Integer u : g2.vertices) {
        g.addEdge(v, u + offset);
      }
    }

    g.setName(g1.name + " \\join " + g2.name);

    return g;
  }

  public BasicGraph complement() {
    BasicGraph c = new BasicGraph();
    for (Integer v : vertices)
      c.addVertex(v);

    for (Integer i : vertices) {
      for (Integer j : vertices) {
        if ((i < j) && !isAdjacent(i, j)) {
          c.addEdge(i, j);
        }
      }
    }
    if (name.indexOf("co-") == 0) {
      c.name = name.substring(3);
    } else {
      c.name = "co-" + name;
    }
    return c;
  }

  @Override
  public int hashCode() {
    return neighborhoods.hashCode();
  }

  public boolean isAdjacent(int a, int b) {
    return neighborhoods.get(a).contains(b);
  }

  /**
   * Returns true if and only if s is a clique and for no vertex v not in s, s
   * union {v} is a clique.
   * 
   * @param s
   * @return true if s is a maximal clique
   */
  public boolean isMaximalClique(Collection<Integer> s) {
    if (!isClique(s))
      return false;

    HashSet<Integer> nonMembers = new HashSet<Integer>(order());
    for (Integer v : vertices) {
      if (!s.contains(v))
        nonMembers.add(v);
    }

    for (Integer v : nonMembers) {
      HashSet<Integer> potential = new HashSet<Integer>(s.size() + 1);
      potential.addAll(s);
      potential.add(v);
      if (isClique(potential))
        return false;
    }

    return true;
  }

  public boolean isClique(Collection<Integer> s) {
    if (s == null)
      throw new NullPointerException("Clique input was null");
    if (s.size() <= 1)
      return true;
    if (s.size() == 2) {
      Iterator<Integer> it = s.iterator();
      int a = it.next();
      int b = it.next();
      return isAdjacent(a, b);
    }

    Integer[] x = s.toArray(new Integer[s.size()]);
    for (int i = 0; i < x.length; i++) {
      for (int j = i + 1; j < x.length; j++) {
        if (!isAdjacent(x[i], x[j])) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Runs in O(deg(v)) time
   * 
   * @param v
   * @return
   */
  public HashSet<Integer> getNeighborhood(int v) {
    HashSet<Integer> n = new HashSet<Integer>();
    for (Integer i : neighborhoods.get(v)) {
      n.add(i);
    }
    return n;
  }

  /**
   * Runs in O(deg(v)) time
   * 
   * @param v
   * @return
   */
  public HashSet<Integer> getClosedNeighborhood(int v) {
    HashSet<Integer> n = getNeighborhood(v);
    n.add(v);
    return n;
  }

  public BasicGraph clone() {
    BasicGraph gc = new BasicGraph();
    for (Integer v : vertices)
      gc.addVertex(v);

    for (Integer v : neighborhoods.keySet()) {
      for (Integer nv : neighborhoods.get(v))
        gc.addEdge(v, nv);
    }

    gc.name = name;
    return gc;
  }

  @Override
  public String toString() {
    String s = name == "" ? "" : ("[" + name + "] ");
    ArrayList<Edge> lst = new ArrayList<Edge>(getEdges());
    Collections.sort(lst);
    s += vertices.size() + " vertices on edge set " + lst.toString();
    return s;
  }

  @Override
  public boolean equals(Object obj) {
    return obj == null ? false : toString().equals(obj.toString());
  }

  /**
   * RETURNS LENGTH OF SHORTEST PATH OR -1 IF DISCONNECTED
   * 
   */
  public int shortestPath(int a, int b) {
    if (!vertices.contains(a) || !vertices.contains(b))
      throw new IllegalArgumentException("Not vertices " + a + "," + b);
    if (a == b)
      return 0;
    if (isAdjacent(a, b))
      return 1;

    HashMap<Integer, Integer> dist = new HashMap<Integer, Integer>(order());
    Queue<Integer> q = new LinkedList<Integer>();
    q.add(a);
    dist.put(a, 0);
    while (!q.isEmpty()) {
      a = q.poll();
      for (Integer v : getNeighborhood(a)) {
        if (!dist.containsKey(v)) {
          q.add(v);
          dist.put(v, dist.get(a) + 1);
          if (v == b)
            return dist.get(v);
        }
      }
    }
    return -1;
  }

  /**
   * An edgeless graph is disconnected
   * 
   * @return
   */
  public boolean isConnected() {
    if (vertices.size() <= 1 || getEdges().size() == 0)
      return false;
    int s = vertices.iterator().next();
    for (Integer v : vertices) {
      if (shortestPath(s, v) < 0)
        return false;
    }
    return true;
  }

  /**
   * Returns true if and only if the graph is AT-free, that is, if for every
   * triple a,b,c, the test isAT(a,b,c) fails.
   * 
   * Runs in n^3(n+m) time
   * 
   * @return true if graph is AT-free
   */
  public boolean isATFree() {
    return getAT() == null;
  }

  public HashSet<Integer> getAT() {
    ArrayList<Integer> V = new ArrayList<Integer>(vertices);

    for (int i = 0; i < order(); i++) {
      for (int j = i + 1; j < order(); j++) {
        for (int k = j + 1; k < order(); k++) {
          int a = V.get(i);
          int b = V.get(j);
          int c = V.get(k);
          if (isAT(a, b, c)) {
            // System.out.println("AT: [" + a + ", " + b + ", " + c
            // + "]");

            HashSet<Integer> s = new HashSet<Integer>(3);
            s.add(a);
            s.add(b);
            s.add(c);
            return s;

          }
        }
      }
    }
    return null;
  }

  public boolean isChordal() {
    BasicGraph g = clone();
    while (!g.vertices.isEmpty()) {
      int del = -1;
      for (int i : g.vertices) {
        if (g.isSimplicial(i)) {
          del = i;
          break;
        }
      }
      if (del == -1)
        return false;
      if (!g.removeVertex(del)) {
        System.out.println("Could not delete vertex " + del + " from " + g);
      }
    }
    return true;
  }

  public boolean isAT(int a, int b, int c) {
    if (a == b || b == c || a == c)
      throw new IllegalArgumentException("Need three disctinct vertices for AT test");
    return (hasPathExceptNeigh(a, b, c) && hasPathExceptNeigh(a, c, b) && hasPathExceptNeigh(b, c, a));
  }

  /**
   * Checks if there is an a-b-path in G-N[c]
   * 
   * @param a
   *          source
   * @param b
   *          sink
   * @param c
   *          the neighborhood to avoid
   * @return true if a-b-path in G-N[c]
   */
  private boolean hasPathExceptNeigh(int a, int b, int c) {
    if (isAdjacent(a, c) || isAdjacent(b, c))
      return false;
    if (isAdjacent(a, b))
      return true;

    BasicGraph gMinusNc = clone();
    gMinusNc.removeVertices(getNeighborhood(c));
    return gMinusNc.shortestPath(a, b) >= 0;
  }

  static class Edge implements Comparable<Edge> {
    private final int a;
    private final int b;

    public Edge(int a, int b) {
      if (a < b) {
        this.a = a;
        this.b = b;
      } else {
        this.a = b;
        this.b = a;
      }
    }

    public boolean isIncident(int x) {
      return x == this.a || x == this.b;
    }

    public int getOther(int x) {
      if (x == this.a) {
        return this.b;
      } else {
        return this.a;
      }
    }

    @Override
    public int compareTo(Edge o) {
      if (a < o.a) {
        return -1;
      } else if (a == o.a) {
        if (b < o.b) {
          return -1;
        } else {
          return b == o.b ? 0 : 1;
        }
      } else {
        return 1;
      }
    }

    @Override
    public int hashCode() {
      return (a * 97) + b;
    }

    public boolean equals(Object o) {
      if (o == null || (!(o instanceof Edge)))
        return false;
      Edge e = (Edge) o;
      return a == e.a && b == e.b;
    }

    public String toString() {
      return a + "-" + b;
    }
  }

  public SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> getSimpleGraph(
      EdgeFactory<DefaultVertex, DefaultEdge<DefaultVertex>> edgeFactory) {
    int n = vertices.size();
    SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>> sg = new SimpleGraph<DefaultVertex, DefaultEdge<DefaultVertex>>(
        edgeFactory);
    HashMap<Integer, DefaultVertex> map = new HashMap<Integer, DefaultVertex>();
    for (Integer i : vertices) {
      int x = i % ((int) Math.sqrt(n) + 1);
      int y = i - x;
      DefaultVertex vi = new DefaultVertex(new Coordinate(x, y));
      sg.addVertex(vi);
      map.put(i, vi);
    }
    for (Integer a : vertices) {
      for (Integer b : neighborhoods.get(a)) {
        if (!sg.containsEdge(map.get(a), map.get(b))) {
          sg.addEdge(map.get(a), map.get(b));
        }
      }
    }

    return sg;
  }
}
