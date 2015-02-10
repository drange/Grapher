package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

import android.util.SparseArray;

/**
 * @author Johan Alexander Nordstrand Rusvik
 */
public class SteinerTree<V, E> extends Algorithm<V, E, Collection<E>> {

	// The subset of vertices which is the goal to connect with as few edges as
	// possible
	private Collection<V> terminals;

	// The 3D dynamic programming table
	private int[][][] dp;

	// This integer generates a unique key for the dp table above which
	// is mapped to a set of edges connecting the current disjunkt set
	// including the current vertex on the edge budget, if such a connection
	// exists.
	private int dpKey;

	// Mapping a set of edges corresponding to an index in the dp table,
	// to an unique integer
	private SparseArray<Collection<E>> intToSubST;

	// A list containing the disjunkt sets, from smallest to largest
	private List<Collection<V>> disjunktSets;

	// A list containing the vertices
	private List<V> vertices;

	// Mapping each disjunkt set to an unique integer and vice versa
	private SparseArray<Collection<V>> intFindDisjunktSet;
	private Map<Collection<V>, Integer> disjunktSetFindInt;

	// Mapping each vertice to an unique integer and vice versa
	private SparseArray<V> intFindVertex;
	private Map<V, Integer> vertexFindInt;

	public SteinerTree(SimpleGraph<V, E> graph, Collection<V> terminals) {
		super(graph);
		if (terminals == null)
			throw new NullPointerException("Terminal set cannot be null as input to Steiner Tree.");
		this.terminals = terminals;
		setProgressGoal((int) Math.pow(2, terminals.size()) + 5);
	}

	@Override
	public Collection<E> execute() {
		if (terminals.size() == 1)

			return new HashSet<E>();
		increaseProgress();

		if (!areTerminalsConnected(graph, terminals))
			return null;
		increaseProgress();

		init();
		if (cancelFlag)
			return null;
		increaseProgress();

		fillTable();
		if (cancelFlag)
			return null;
		increaseProgress();

		for (int k = 0; k <= graphEdgeSize(); k++) {
			if (cancelFlag)
				return null;

			for (int i = 0; i < vertices.size(); i++) {
				int edgeSetKey = dp[dp.length - 1][i][k];
				if (edgeSetKey == 0)
					continue;
				Collection<E> steinerTree = intToSubST.get(edgeSetKey);
				System.out.println("Steiner Tree size: " + steinerTree.size());
				return steinerTree;
			}
		}
		return null;
	}

	private void fillTable() {

		// calculating k=1, consider only cases where the disjunkt sets have
		// size 1 or 2
		for (int i = 0; i < disjunktSets.size(); i++) {
			if (disjunktSets.get(i).size() == 1) {
				for (V vertex : disjunktSets.get(i)) { // there is only one
														// vertex --> O(1)
					for (int j = 0; j < vertices.size(); j++) {
						for (E edge : graph.edgesOf(vertex)) {
							if (opposite(graph, vertex, edge).equals(vertices.get(j))) {
								Collection<E> edgeSet = new HashSet<E>();
								edgeSet.add(edge);
								storeEdgeSet(edgeSet, i, j, 1);
							}
						}
					}
				}
			} else if (disjunktSets.get(i).size() == 2) {
				Iterator<V> vertexIterator = disjunktSets.get(i).iterator();
				V v1 = vertexIterator.next();
				V v2 = vertexIterator.next();
				for (int j = 0; j < vertices.size(); j++) {
					V currVertex = vertices.get(j);
					if (currVertex.equals(v1)) {
						for (E edge : graph.edgesOf(currVertex)) {
							if (opposite(graph, currVertex, edge).equals(v2)) {
								Collection<E> edgeSet = new HashSet<E>();
								edgeSet.add(edge);
								storeEdgeSet(edgeSet, i, j, 1);
							}
						}
					} else if (currVertex.equals(v2)) {
						for (E edge : graph.edgesOf(currVertex)) {
							if (opposite(graph, currVertex, edge).equals(v1)) {
								Collection<E> edgeSet = new HashSet<E>();
								edgeSet.add(edge);
								storeEdgeSet(edgeSet, i, j, 1);
							}
						}
					}
				}
			}
			if (cancelFlag)
				return;
			increaseProgress();
		}

		setProgressGoal(2 * (int) Math.pow(2, terminals.size()));
		for (int k = 2; k <= graphEdgeSize(); k++) {
			for (int i = 0; i < disjunktSets.size(); i++) {
				for (int j = 0; j < vertices.size(); j++) {
					boolean trySecond = true;
					// 1st
					for (E edge : graph.edgesOf(vertices.get(j))) {
						int edgeSetKey = dp[i][vertexFindInt.get(opposite(graph, vertices.get(j), edge))][k - 1];
						if (edgeSetKey == 0)
							continue;
						Collection<E> prevIndex = intToSubST.get(edgeSetKey);
						Collection<E> newIndex = new HashSet<E>();
						newIndex.addAll(prevIndex);
						newIndex.add(edge);
						storeEdgeSet(newIndex, i, j, k);
						trySecond = false;
						break;
					}

					if (!trySecond) {
						continue;
					}
					// 2nd
					boolean breakOut = false;
					TwoPartitionsIterator<V> partitionsIterator = new TwoPartitionsIterator<V>(disjunktSets.get(i));
					while (partitionsIterator.hasNext()) {
						Collection<V> t1 = partitionsIterator.next();
						Collection<V> t2 = partitionsIterator.currentSecondPart();
						for (int l = k; l >= 0; l--) {
							int t1Key = dp[disjunktSetFindInt.get(t1)][j][l];
							if (t1Key == 0)
								continue;
							int t2Key = dp[disjunktSetFindInt.get(t2)][j][k - l];
							if (t2Key == 0)
								continue;
							Collection<E> newIndex = new HashSet<E>();
							newIndex.addAll(intToSubST.get(t1Key));
							newIndex.addAll(intToSubST.get(t2Key));

							storeEdgeSet(newIndex, i, j, k);
							breakOut = true;
							break;
						}
						if (breakOut)
							break;
					}
				}
			}
			if (cancelFlag)
				return;
			increaseProgress();
		}

	}

	private V opposite(SimpleGraph<V, E> graph, V vertex, E edge) {
		if (graph.getEdgeSource(edge).equals(vertex)) {
			return graph.getEdgeTarget(edge);
		}
		return graph.getEdgeSource(edge);
	}

	private void storeEdgeSet(Collection<E> edgeSet, int disjunktSetIndex, int vertexIndex, int edgeBudget) {
		dp[disjunktSetIndex][vertexIndex][edgeBudget] = dpKey;
		intToSubST.put(dpKey++, edgeSet);
	}

	private void init() {

		int numDisSets = PowersetIterator.twoPower(terminals.size());

		dp = new int[numDisSets][graphSize()][graphEdgeSize() + 1];
		dpKey = 1;

		intToSubST = new SparseArray<Collection<E>>();

		disjunktSets = new ArrayList<Collection<V>>(numDisSets);
		intFindDisjunktSet = new SparseArray<Collection<V>>();
		disjunktSetFindInt = new HashMap<Collection<V>, Integer>();
		PowersetIterator<V> dts = new PowersetIterator<V>(terminals);
		int disjunktSetsKey = 0;
		while (dts.hasNext()) {
			if (cancelFlag)
				return;

			Collection<V> disjunktSet = dts.next();
			disjunktSets.add(disjunktSet);
			intFindDisjunktSet.put(disjunktSetsKey, disjunktSet);
			disjunktSetFindInt.put(disjunktSet, disjunktSetsKey++);
		}
		increaseProgress();

		vertices = new ArrayList<V>(graphSize());
		intFindVertex = new SparseArray<V>();
		vertexFindInt = new HashMap<V, Integer>();
		int verticesKey = 0;
		for (V v : graph.vertexSet()) {
			vertices.add(v);
			intFindVertex.put(verticesKey, v);
			vertexFindInt.put(v, verticesKey++);
		}

	}

	/**
	 * Returns true if and only if all terminals given in input is connected in
	 * given graph.
	 * 
	 * @param graph
	 *            A graph where terminals live
	 * @param terminals
	 *            set of vertices to check connectedness of
	 * @return true iff there exists a steiner tree of terminals
	 * @author pgd
	 */
	public static <V, E> boolean areTerminalsConnected(SimpleGraph<V, E> graph, Collection<V> terminals) {
		ConnectivityInspector<V, E> connInsp = new ConnectivityInspector<V, E>(graph);
		if (!connInsp.isGraphConnected()) {
			for (V t1 : terminals) {
				for (V t2 : terminals) {
					if (t1 != t2) {
						if (!connInsp.pathExists(t1, t2)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
}
