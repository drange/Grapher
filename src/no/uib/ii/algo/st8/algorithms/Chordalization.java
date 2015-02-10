package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

/**
 * @author Simen Lilleeng
 * 
 */

public class Chordalization<V, E> extends Algorithm<V, E, Set<E>> {

	public Chordalization(SimpleGraph<V, E> graph) {
		super(graph);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Minimum fill in on the graph. (Adds the minimum amount of edges such that
	 * the graph becomes chordal).
	 * 
	 * "Fixed-parameter tractability of graph modification problems for hereditary properties"
	 * - Cai, Leizhen 1996
	 * 
	 * @param graph
	 * @return The set of added edges.
	 */
	public Set<E> fillIn() {
		int i = 1;
		HashMap<V, Integer> vertexToInt = new HashMap<V, Integer>();
		ArrayList<V> intToVertex = new ArrayList<V>();
		intToVertex.add(null);
		for (V v : graph.vertexSet()) {
			vertexToInt.put(v, i);
			intToVertex.add(v);
			i++;
		}

		Set<E> newEdges;
		for (int j = 0; j < Integer.MAX_VALUE; j++) {
			if (cancelFlag)
				return null;
			System.out.println("fillIn() Trying j = " + j);
			progress(j, graphSize() * 3);

			// DOING FOR k = j....
			if ((newEdges = chordalize(graph, j, vertexToInt, intToVertex)) != null) {
				return newEdges;
			}
		}
		return null;
	}

	/**
	 * Adds up to k-edges to the graph such that the graph becomes chordal.
	 * 
	 * @param graph
	 * @param k
	 *            Number of edges allowed to add, this affects runtime ~O*(4^k)
	 * @return Set of up to k edges that makes the graph chordal. Null if graph
	 *         is already chordal, or if the graph can't be made chordal by
	 *         adding up to k edges.
	 */
	public Set<E> chordalize(SimpleGraph<V, E> graph, int k, HashMap<V, Integer> vertexToInt, ArrayList<V> intToVertex) {
		SimpleGraph<V, E> filled = branching(graph, k, vertexToInt, intToVertex);
		if (filled == null) {
			return null;
		}
		Set<E> highlight = new HashSet<E>();
		Set<E> allEdges = filled.edgeSet();
		for (E e : allEdges) {
			if (cancelFlag)
				return null;
			if (!graph.containsEdge(e)) {
				graph.addEdge(filled.getEdgeSource(e), filled.getEdgeTarget(e));
				highlight.add(graph.getEdge(filled.getEdgeSource(e), filled.getEdgeTarget(e)));
			}
		}
		return highlight;
	}

	/**
	 * Branches on all possible minimal fill-ins of the graph.
	 * 
	 * @param graph
	 * @param k
	 *            Number of edges left to add
	 * @param vertexToInt
	 *            HashMap to translate vertices into integers, practical for
	 *            handling list references.
	 * @param intToVertex
	 *            ArrayList filled with vertices according to their index.
	 * @return A graph, if there is a chordal graph down the branch. Null if
	 *         none is found.
	 */
	public SimpleGraph<V, E> branching(SimpleGraph<V, E> graph, int k, HashMap<V, Integer> vertexToInt, ArrayList<V> intToVertex) {
		int[][] alpha = maximumCardinalitySearch(graph, vertexToInt);
		V u = testForZeroFillIn(graph, alpha, vertexToInt, intToVertex);
		if (u == null) {
			return graph;
		}
		ArrayList<V> maxViol = maxViolatingTriple(u, alpha, graph, vertexToInt, intToVertex);
		ArrayList<E> cycle = findUnchordedCycle(maxViol, graph, vertexToInt, intToVertex);
		if (cycle.size() > k + 3) {
			return null;
		}
		ArrayList<ArrayList<Boolean>> binaryTrees = new ArrayList<ArrayList<Boolean>>();
		ArrayList<Boolean> x = new ArrayList<Boolean>();
		binaryTreeGenerator(x, cycle.size() - 2, 0, 0, binaryTrees);
		for (int i = 0; i < binaryTrees.size(); i++) {
			if (cancelFlag)
				return null;

			SimpleGraph<V, E> clone = (SimpleGraph<V, E>) graph.clone();
			ArrayList<E> cyClone = (ArrayList<E>) cycle.clone();
			fillCycle(clone, cyClone, binaryTrees.get(i));
			SimpleGraph<V, E> newClone;
			if ((newClone = branching(clone, k - cycle.size() + 3, vertexToInt, intToVertex)) != null) {
				return newClone;
			}
		}
		return null;
	}

	/**
	 * Fills an unchorded cycle with edges. There is a bijection between minimum
	 * fill-ins of a cycle of size n and full binary trees with n-2 internal
	 * nodes.
	 * 
	 * @param graph
	 * @param cycle
	 *            An unchorded cycle that was found in the graph
	 * @param binaryTree
	 *            A binaryTree representation that corrolates to an unique fill
	 *            of the cycle.
	 */
	private void fillCycle(SimpleGraph<V, E> graph, ArrayList<E> cycle, ArrayList<Boolean> binaryTree) {
		boolean lastZero = false;
		int zeroCount = 0;
		while (cycle.size() > 3) {
			if (cancelFlag)
				return;
			zeroCount = 0;
			lastZero = false;
			for (int i = 1; i < binaryTree.size(); i++) {
				if (!binaryTree.get(i)) {
					if (lastZero) {
						lastZero = false;
						binaryTree.remove(i - 2);
						binaryTree.remove(i - 2);
						E fst = cycle.get(zeroCount);
						E snd = cycle.get(zeroCount + 1);
						if (graph.getEdgeSource(fst).equals(graph.getEdgeSource(snd))) {
							graph.addEdge(graph.getEdgeTarget(fst), graph.getEdgeTarget(snd));
							cycle.set(zeroCount, graph.getEdge(graph.getEdgeTarget(fst), graph.getEdgeTarget(snd)));
						} else if (graph.getEdgeSource(fst).equals(graph.getEdgeTarget(snd))) {
							graph.addEdge(graph.getEdgeTarget(fst), graph.getEdgeSource(snd));
							cycle.set(zeroCount, graph.getEdge(graph.getEdgeTarget(fst), graph.getEdgeSource(snd)));
						} else if (graph.getEdgeTarget(fst).equals(graph.getEdgeSource(snd))) {
							graph.addEdge(graph.getEdgeSource(fst), graph.getEdgeTarget(snd));
							cycle.set(zeroCount, graph.getEdge(graph.getEdgeSource(fst), graph.getEdgeTarget(snd)));
						} else {
							graph.addEdge(graph.getEdgeSource(fst), graph.getEdgeSource(snd));
							cycle.set(zeroCount, graph.getEdge(graph.getEdgeSource(fst), graph.getEdgeSource(snd)));
						}
						cycle.remove(zeroCount + 1);
					} else {
						lastZero = true;
					}
					zeroCount++;
				} else {
					lastZero = false;
				}
			}
		}
	}

	/**
	 * Generates all possible binary-trees with n internal nodes.
	 * 
	 * @param x
	 *            The current representation of a binary-tree.
	 * @param n
	 *            Number of internal nodes.
	 * @param ones
	 *            Count of number of internal nodes used so far.
	 * @param zeros
	 *            Count of number of external nodes used so far.
	 * @param binaryTrees
	 *            List of all generated binaryTrees with n internal nodes.
	 */
	private void binaryTreeGenerator(ArrayList<Boolean> x, int n, int ones, int zeros, ArrayList<ArrayList<Boolean>> binaryTrees) {
		if (x.size() > 2 * n) {
			return;
		}
		if (x.size() == 2 * n) {
			x.add(false);
			binaryTrees.add(x);
			return;
		}
		if (ones == n) {
			x.add(false);
			binaryTreeGenerator(x, n, ones, zeros + 1, binaryTrees);
			return;
		}
		if (zeros >= ones) {
			x.add(true);
			binaryTreeGenerator(x, n, ones + 1, zeros, binaryTrees);
			return;
		}
		if (ones > zeros) {
			ArrayList<Boolean> y = (ArrayList<Boolean>) x.clone();
			x.add(true);
			binaryTreeGenerator(x, n, ones + 1, zeros, binaryTrees);
			y.add(false);
			binaryTreeGenerator(y, n, ones, zeros + 1, binaryTrees);
		}

	}

	/**
	 * Finds an unchorded cycle in the graph
	 * 
	 * "Addendum: Simple linear-time algorithms to test chordality of graphs, test acyclicity of hypergraphs, and selectively reduce acyclic hypergraphs"
	 * - Tarjan, Robert E and Yannakakis, Mihalis 1985
	 * 
	 * @param maxViol
	 *            List of the three vertices which makes the max violating
	 *            triple
	 * @param graph
	 * @param vertexToInt
	 * @param intToVertex
	 * @return Unchorded cycle in graph
	 */
	public ArrayList<E> findUnchordedCycle(ArrayList<V> maxViol, SimpleGraph<V, E> graph, HashMap<V, Integer> vertexToInt,
			ArrayList<V> intToVertex) {
		V x;
		V y = null;
		ArrayList<E> cycle = new ArrayList<E>();
		boolean found = false;
		int[] backtrack = new int[intToVertex.size()];
		SimpleGraph<V, E> clone = (SimpleGraph<V, E>) graph.clone();
		clone.removeEdge(maxViol.get(0), maxViol.get(1));
		clone.removeEdge(maxViol.get(0), maxViol.get(2));
		Set<E> edges = new HashSet<E>();
		edges.addAll(clone.edgesOf(maxViol.get(0)));
		for (E e : edges) {
			V s = clone.getEdgeSource(e);
			V t = clone.getEdgeTarget(e);
			if (s.equals(maxViol.get(0))) {
				clone.removeVertex(t);
			} else {

				clone.removeVertex(s);
			}
		}
		Queue<Integer> q = new LinkedList<Integer>();
		q.add(vertexToInt.get(maxViol.get(1)));
		backtrack[vertexToInt.get(maxViol.get(1))] = vertexToInt.get(maxViol.get(1));
		cycle.add(graph.getEdge(maxViol.get(2), maxViol.get(0)));
		cycle.add(graph.getEdge(maxViol.get(1), maxViol.get(0)));
		while (!q.isEmpty() && !found) {
			x = intToVertex.get(q.poll());
			for (E e : clone.edgesOf(x)) {
				y = clone.getEdgeSource(e);
				if (y.equals(x)) {
					y = clone.getEdgeTarget(e);
				}
				if (backtrack[vertexToInt.get(y)] == 0) {
					backtrack[vertexToInt.get(y)] = vertexToInt.get(x);
					q.add(vertexToInt.get(y));
				}
				if (y.equals(maxViol.get(2))) {
					found = true;
					break;
				}
			}
		}
		int b = vertexToInt.get(maxViol.get(2));
		x = maxViol.get(2);
		while (b != vertexToInt.get(maxViol.get(1))) {
			b = backtrack[b];
			y = intToVertex.get(b);
			cycle.add(graph.getEdge(x, y));
			x = y;
		}
		return cycle;
	}

	/**
	 * Finds the max violating triple in the graph.
	 * 
	 * "Addendum: Simple linear-time algorithms to test chordality of graphs, test acyclicity of hypergraphs, and selectively reduce acyclic hypergraphs"
	 * - Tarjan, Robert E and Yannakakis, Mihalis 1985
	 * 
	 * @param u
	 *            First vertex of the max violating triple
	 * @param alpha
	 *            The alpha-values made in the graph
	 * @param graph
	 * @param vertexToInt
	 * @param intToVertex
	 * @return The three vertices of the max violating triple
	 */
	public ArrayList<V> maxViolatingTriple(V u, int[][] alpha, SimpleGraph<V, E> graph, HashMap<V, Integer> vertexToInt,
			ArrayList<V> intToVertex) {
		ArrayList<Integer> neighAlpha = new ArrayList<Integer>();
		V x;
		V y;
		V v = null;
		V w = null;
		ArrayList<V> triple = new ArrayList<V>();
		for (E e : graph.edgesOf(u)) {
			x = graph.getEdgeSource(e);
			if (x == u) {
				x = graph.getEdgeTarget(e);
			}
			neighAlpha.add(alpha[0][vertexToInt.get(x)]);
		}
		Collections.sort(neighAlpha);
		for (int i = 0; i < neighAlpha.size() - 1; i++) {
			x = intToVertex.get(alpha[1][neighAlpha.get(i)]);
			for (int j = i + 1; j < neighAlpha.size(); j++) {
				y = intToVertex.get(alpha[1][neighAlpha.get(j)]);
				if (!(graph.containsEdge(x, y) || graph.containsEdge(y, x))) {
					v = x;
					w = y;
				}
			}
		}
		triple.add(u);
		triple.add(v);
		triple.add(w);

		return triple;
	}

	/**
	 * Does a maximum cardinality search on the graph
	 * 
	 * "Simple linear-time algorithms to test chordality of graphs, test acyclicity of hypergraphs, and selectively reduce acyclic hypergraphs"
	 * - Tarjan, Robert E and Yannakakis, Mihalis 1984
	 * 
	 * @param graph
	 * @param vertexToInt
	 * @return Alpha-values and its reverse
	 */
	public int[][] maximumCardinalitySearch(SimpleGraph<V, E> graph, HashMap<V, Integer> vertexToInt) {
		int i, j;
		int n = graph.vertexSet().size();
		int[] size = new int[n + 1];
		int[][] alpha = new int[2][n + 1]; // alpha[0][] corresponds to alpha,
											// alpha[1][] corresponds to
											// alphaInverse
		ArrayList<Set<V>> set = new ArrayList<Set<V>>();
		i = 0;
		for (i = 0; i < n; i++) {
			set.add(new HashSet<V>());
		}
		for (V v : graph.vertexSet()) {
			set.get(0).add(v);
		}
		i = n;
		j = 0;
		while (i > 0) {
			V v = set.get(j).iterator().next();
			int vIndex = vertexToInt.get(v);
			set.get(j).remove(v);
			alpha[0][vIndex] = i;
			alpha[1][i] = vIndex;
			size[vIndex] = -1;
			for (E e : graph.edgesOf(v)) {
				V u = graph.getEdgeSource(e);
				V w = graph.getEdgeTarget(e);
				if (v.equals(w)) {
					w = u;
				}
				int wIndex = vertexToInt.get(w);
				if (size[wIndex] >= 0) {
					set.get(size[wIndex]).remove(w);
					size[wIndex]++;
					set.get(size[wIndex]).add(w);
				}
			}

			i--;
			j++;
			while (j >= 0 && set.get(j).isEmpty()) {
				j = j - 1;
			}
		}
		return alpha;
	}

	/**
	 * Checks if the graph is chordal.
	 * 
	 * "Simple linear-time algorithms to test chordality of graphs, test acyclicity of hypergraphs, and selectively reduce acyclic hypergraphs"
	 * - Tarjan, Robert E and Yannakakis, Mihalis 1984
	 * 
	 * @param graph
	 * @param alpha
	 *            Alpha-values found by maximum cardinality search.
	 * @param vertexToInt
	 * @param intToVertex
	 * @return The first vertex of the max violating triple if the graph is not
	 *         chordal. Null if the graph is chordal.
	 */
	public V testForZeroFillIn(SimpleGraph<V, E> graph, int[][] alpha, HashMap<V, Integer> vertexToInt, ArrayList<V> intToVertex) {
		int w;
		V v;
		int n = graph.vertexSet().size();
		int[] f = new int[n + 1];
		int[] index = new int[n + 1];
		for (int i = 1; i <= n; i++) {
			w = alpha[1][i];
			f[w] = w;
			index[w] = i;
			for (E e : graph.edgesOf(intToVertex.get(w))) {
				if ((alpha[0][vertexToInt.get(graph.getEdgeTarget(e))] < i)
						|| (alpha[0][vertexToInt.get(graph.getEdgeSource(e))] < i)) {
					v = graph.getEdgeSource(e);
					if (vertexToInt.get(v) == w) {
						v = graph.getEdgeTarget(e);
					}
					index[vertexToInt.get(v)] = i;
					if (f[vertexToInt.get(v)] == vertexToInt.get(v)) {
						f[vertexToInt.get(v)] = w;
					}
				}
			}
			for (E e : graph.edgesOf(intToVertex.get(w))) {
				if ((alpha[0][vertexToInt.get(graph.getEdgeTarget(e))] < i)
						|| (alpha[0][vertexToInt.get(graph.getEdgeSource(e))] < i)) {
					v = graph.getEdgeSource(e);
					if (vertexToInt.get(v) == w) {
						v = graph.getEdgeTarget(e);
					}
					if (index[f[vertexToInt.get(v)]] < i) {
						return v;
					}
				}
			}

		}
		return null;
	}

	@Override
	public Set<E> execute() {
		return fillIn();
	}
}
