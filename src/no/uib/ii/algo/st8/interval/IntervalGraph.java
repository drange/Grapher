package no.uib.ii.algo.st8.interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * 
 * An interval representation of a graph. Contains a method for generating
 * random interval graphs, and for constructing a SimpleGraph from an interval
 * model.
 * 
 * @author Pål Grønås Drange
 * 
 */
public class IntervalGraph {

  private final HashMap<Integer, Interval> vertices = new HashMap<Integer, Interval>();

  int currentMaxVertexNumber = 0;

  public IntervalGraph() {
  }

  public int order() {
    return vertices.size();
  }

  public ArrayList<Interval> getIntervals() {
    return new ArrayList<Interval>(vertices.values());
  }

  public HashSet<Integer> getNeighborhood(int v) {
    if (!vertices.containsKey(v))
      return null;

    Interval vInt = vertices.get(v);

    HashSet<Integer> nv = new HashSet<Integer>();
    for (Integer other : vertices.keySet()) {
      if (other == v)
        continue;
      if (vertices.get(other).overlaps(vInt)) {
        nv.add(other);
      }
    }

    return nv;
  }

  /**
   * This method takes a clique chain and computes an interval graph. It does
   * not test if the resulting interval graph corresponds to this clique chain.
   * 
   * @param chain
   * @return an interval graph
   */
  public static IntervalGraph fromChain(ArrayList<HashSet<Integer>> chain) {
    IntervalGraph ig = new IntervalGraph();
    for (int i = 0; i < chain.size(); i++) {
      HashSet<Integer> cliq = chain.get(i);
      for (Integer v : cliq) {
        if (ig.vertices.containsKey(v)) {
          ig.vertices.put(v, ig.getInterval(v).shiftRight(2));
        } else {
          ig.vertices.put(v, new Interval(2 * i + 1, 2 * (i + 1)));
          ig.currentMaxVertexNumber = Math.max(ig.currentMaxVertexNumber, v);
        }
      }
    }

    return ig;
  }

  /**
   * Get the interval for vertex v
   * 
   * @param v
   * @return the interval object corresponding to v
   */
  public Interval getInterval(int v) {
    return vertices.get(v);
  }

  /**
   * Get (a copy of) the vertex set of this interval graph
   * 
   * @return
   */
  public HashSet<Integer> getVertices() {
    HashSet<Integer> v = new HashSet<Integer>(vertices.size());
    v.addAll(vertices.keySet());
    return v;
  }

  /**
   * Adds a new vertex with the given endpoints.
   * 
   * @param left
   * @param right
   * @return the id of the new vertex
   */
  public int addInterval(int left, int right) {
    currentMaxVertexNumber++;
    vertices.put(currentMaxVertexNumber, new Interval(left, right));
    return currentMaxVertexNumber;
  }

  public void setInterval(int left, int right, int vertex) {
    currentMaxVertexNumber = Math.max(currentMaxVertexNumber, vertex);
    vertices.put(vertex, new Interval(left, right));
  }

  public boolean isAdjacent(int a, int b) {
    if (!vertices.containsKey(a)) {
      throw new IllegalArgumentException("First argument not a vertex: " + a);
    }
    if (!vertices.containsKey(b)) {
      throw new IllegalArgumentException("Second argument not a vertex: " + b);
    }
    return vertices.get(a).overlaps(vertices.get(b));
  }

  @Override
  public String toString() {
    if (vertices.isEmpty())
      return "Interval graph empty";
    String s = "Interval graph on " + vertices.size() + " vertices: ";
    if (vertices.size() == 1)
      s = s.replace("ices", "ex");
    for (Interval i : vertices.values())
      s += i;
    return s;
  }

  @Override
  public int hashCode() {
    return vertices.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || (!(obj instanceof IntervalGraph)))
      return false;
    IntervalGraph ig = (IntervalGraph) obj;
    return ig.vertices.equals(vertices);
  }

  public BasicGraph getGraph() {
    BasicGraph g = new BasicGraph();
    for (int i : vertices.keySet()) {
      for (int j : vertices.keySet()) {
        if (i != j && vertices.get(i).overlaps(vertices.get(j)))
          g.addEdge(i, j);
      }
    }
    g.setName("From interval representation");
    return g;
  }

  public static IntervalGraph getRandomIntervalGraph(int n) {
    Random r = new Random();
    IntervalGraph ig = new IntervalGraph();
    for (int i = 0; i < n; i++) {
      int left = 1 + r.nextInt(100);
      int right = 100 - r.nextInt(left);
      ig.addInterval(left, right);
    }
    return ig;
  }
  
  public void deleteVertex(int i){
	vertices.remove(i);  
  }
}
