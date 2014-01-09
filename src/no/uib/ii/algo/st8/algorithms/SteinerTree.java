package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * @author Johan Alexander Nordstrand Rusvik
 */
public class SteinerTree<V, E> extends Algorithm<V, E, Collection<E>> {
	
	// The subset of vertices which is the goal to connect with as few edges as possible
	private Set<V> terminals;

	// The 3D dynamic programming table
	private int[][][] dp;
	
	// This integer generates a unique key for the dp table above which
	// is mapped to a set of edges connecting the current disjunkt set
	// including the current vertex on the edge budget, if such a connection exists.
	private int dpKey;
	
	// Mapping a set of edges corresponding to an index in the dp table,
	// to an unique integer
	private Map<Integer, Set<E>> intToSubST;
	
	// A list containing the disjunkt sets, from smallest to largest
	private List<Set<V>> disjunktSets;

	// A list containing the vertices
	private List<V> vertices;

	// Mapping each disjunkt set to an unique integer and vice versa
	private Map<Integer, Set<V>> intFindDisjunktSet;
	private Map<Set<V>, Integer> disjunktSetFindInt;

	// Mapping each vertice to an unique integer and vice versa
	private Map<Integer, V> intFindVertex;
	private Map<V, Integer> vertexFindInt;

	public SteinerTree(SimpleGraph<V, E> graph, Set<V> terminals) {
		super(graph);
		this.terminals = terminals;
	}

	@Override
	public Collection<E> execute() {
		if(terminals.size() == 1)
			return new HashSet<E>();
		init();
		fillTable();
		for (int k = 0; k <= graphEdgeSize(); k++) {
			for (int i = 0; i < vertices.size(); i++) {
				int edgeSetKey = dp[dp.length - 1][i][k];
				if (edgeSetKey == 0)
					continue;
				Set<E> steinerTree = intToSubST.get(edgeSetKey);
				System.out.println("Steiner Tree size: " + steinerTree.size());
				return steinerTree;
			}
		}
		return null;
	}

	private void fillTable() {

		// calculating k=1, consider only cases where the disjunkt sets have size 1 or 2
		for (int i = 0; i < disjunktSets.size(); i++) {
			if(disjunktSets.get(i).size() == 1) {
				for(V vertex : disjunktSets.get(i)) { // there is only one vertex --> O(1)
					for(int j = 0; j < vertices.size(); j++) {
						for(E edge : graph.edgesOf(vertex)) {
							if(opposite(graph, vertex, edge).equals(vertices.get(j))) {
								Set<E> edgeSet = new HashSet<E>();
								edgeSet.add(edge);
								storeEdgeSet(edgeSet, i, j, 1);
							}
						}
					}
				}
			}
			else if(disjunktSets.get(i).size() == 2) {
				Iterator<V> vertexIterator = disjunktSets.get(i).iterator();
				V v1 = vertexIterator.next();
				V v2 = vertexIterator.next();
				for(int j = 0; j < vertices.size(); j++) {
					V currVertex = vertices.get(j);
					if(currVertex.equals(v1)) {
						for(E edge : graph.edgesOf(currVertex)) {
							if(opposite(graph, currVertex, edge).equals(v2)) {
								Set<E> edgeSet = new HashSet<E>();
								edgeSet.add(edge);
								storeEdgeSet(edgeSet, i, j, 1);
							}
						}
					}
					else if(currVertex.equals(v2)) {
						for(E edge : graph.edgesOf(currVertex)) {
							if(opposite(graph, currVertex, edge).equals(v1)) {
								Set<E> edgeSet = new HashSet<E>();
								edgeSet.add(edge);
								storeEdgeSet(edgeSet, i, j, 1);
							}
						}
					}
				}
			}
		}

		for (int k = 2; k <= graphEdgeSize(); k++) {
			for (int i = 0; i < disjunktSets.size(); i++) {
				for (int j = 0; j < vertices.size(); j++) {
					boolean trySecond = true;
					// 1st
					for (E edge : graph.edgesOf(vertices.get(j))) {
						int edgeSetKey = dp[i][vertexFindInt.get(opposite(graph, vertices.get(j), edge))][k - 1];
						if (edgeSetKey == 0)
							continue;
						Set<E> prevIndex = intToSubST.get(edgeSetKey);
						Set<E> newIndex = new HashSet<E>();
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
						Set<V> t1 = collectionToSet(partitionsIterator.next());
						Set<V> t2 = collectionToSet(partitionsIterator.currentSecondPart());
						for (int l = k; l >= 0; l--) {
							int t1Key = dp[disjunktSetFindInt.get(t1)][j][l];
							if (t1Key == 0)
								continue;
							int t2Key = dp[disjunktSetFindInt.get(t2)][j][k - l];
							if (t2Key == 0)
								continue;
							Set<E> newIndex = new HashSet<E>();
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
		}
	}
	
	private <V, E> V opposite(SimpleGraph<V, E> graph, V vertex, E edge) {
		if (graph.getEdgeSource(edge).equals(vertex)) {
			return graph.getEdgeTarget(edge);
		}
		return graph.getEdgeSource(edge);
	}
	
	private void storeEdgeSet(Set<E> edgeSet, int disjunktSetIndex, int vertexIndex, int edgeBudget) {
		dp[disjunktSetIndex][vertexIndex][edgeBudget] = dpKey;
		intToSubST.put(dpKey++, edgeSet);
	}
	
	private <T> Set<T> collectionToSet(Collection<T> collection) {
		Set<T> newSet = new HashSet<T>();
		newSet.addAll(collection);
		return newSet;
	}

	private void init() {
		int numDisSets = PowersetIterator.twoPower(terminals.size());
		
		dp = new int[numDisSets][graphSize()][graphEdgeSize() + 1];
		dpKey = 1;
		
		intToSubST = new HashMap<Integer, Set<E>>();

		disjunktSets = new ArrayList<Set<V>>(numDisSets);
		intFindDisjunktSet = new HashMap<Integer, Set<V>>();
		disjunktSetFindInt = new HashMap<Set<V>, Integer>();
		PowersetIterator<V> dts = new PowersetIterator<V>(terminals);
		int disjunktSetsKey = 0;
		while (dts.hasNext()) {
			Set<V> disjunktSet = collectionToSet(dts.next());
			disjunktSets.add(disjunktSet);
			intFindDisjunktSet.put(disjunktSetsKey, disjunktSet);
			disjunktSetFindInt.put(disjunktSet, disjunktSetsKey++);
		}

		vertices = new ArrayList<V>(graphSize());
		intFindVertex = new HashMap<Integer, V>();
		vertexFindInt = new HashMap<V, Integer>();
		int verticesKey = 0;
		for (V v : graph.vertexSet()) {
			vertices.add(v);
			intFindVertex.put(verticesKey, v);
			vertexFindInt.put(v, verticesKey++);
		}
	}
}