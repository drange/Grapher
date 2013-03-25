package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.graph.SimpleGraph;

import android.util.SparseArray;

public class HamiltonianPathInspector<V, E> extends
		Algorithm<V, E, GraphPath<V, E>> {

	public HamiltonianPathInspector(SimpleGraph<V, E> graph) {
		super(graph);
	}

	private boolean isPotentiallyYesInstance() {

		if (!new ConnectivityInspector<V, E>(graph).isGraphConnected())
			return false;

		Set<V> cuts = CutAndBridgeInspector.findAllCutVertices(graph);
		Set<V> vertices = new HashSet<V>();
		vertices.addAll(graph.vertexSet());

		for (V v : cuts) {
			vertices.remove(v);
			SimpleGraph<V, E> g = InducedSubgraph.inducedSubgraphOf(graph,
					vertices);
			if ((new ConnectivityInspector<V, E>(g)).connectedSets().size() > 2)
				return false;
			vertices.add(v);
		}
		return true;
	}

	public GraphPath<V, E> execute() {
		if (!isPotentiallyYesInstance())
			return null;

		int n = graphSize();
		int npow = (int) Math.pow(2, n);

		SparseArray<V> idToVertex = new SparseArray<V>(n);
		Map<V, Integer> vertexToId = new HashMap<V, Integer>(n);

		Map<Collection<V>, Integer> collectionToId = new HashMap<Collection<V>, Integer>(
				npow);
		SparseArray<Collection<V>> idToCollection = new SparseArray<Collection<V>>(
				npow);

		PowersetIterator<V> pi = new PowersetIterator<V>(graph.vertexSet());
		int counter = 0;

		progress(0, PowersetIterator.twoPower(graphSize()));

		while (pi.hasNext()) {
			Collection<V> set = pi.next();
			collectionToId.put(set, counter);
			idToCollection.put(counter, set);

			counter++;
		}
		if (cancelFlag)
			return null;
		progress(1, counter);

		counter = 0;
		for (V v : graph.vertexSet()) {
			idToVertex.put(counter, v);
			vertexToId.put(v, counter);

			counter++;
		}

		/*
		 * Standard Hamiltonian path algorithm in 2^n time by dynamic
		 * programming. The table dp[v][S] is true if v is in S and there is a
		 * path going through the entire S and ending in v. Recursively we test
		 * whether dp[u][S-v] is true for some u in S.
		 */

		// base cases, let ( id(v) , id({v}) ) := true
		boolean[][] dp = new boolean[n][npow];
		for (int i = 0; i < n; i++) {
			V v = idToVertex.get(i);
			HashSet<V> s = new HashSet<V>(1);
			s.add(v);
			int setId = collectionToId.get(s);
			dp[vertexToId.get(v)][setId] = true;
		}

		// s is the id for a subset currentSet
		for (int s = 0; s < npow; s++) {
			if (cancelFlag)
				return null;
			progress(s, npow);

			// currentSet is the set corresponding to 's'
			Collection<V> currentSet = idToCollection.get(s);

			for (int v = 0; v < n; v++) {
				// considering dp[currentVertex][currentSet] =? true
				V currentVertex = idToVertex.get(v);

				if (!currentSet.contains(currentVertex))
					continue;

				Set<V> newSet = new HashSet<V>(currentSet.size());
				newSet.addAll(currentSet);
				newSet.remove(currentVertex);

				for (V newVertex : newSet) {
					if (dp[vertexToId.get(newVertex)][collectionToId
							.get(newSet)]) {
						// There is a path in newSet ending in newVertex,
						// is there an edge between newVertex an currentVertex?

						if (graph.containsEdge(currentVertex, newVertex)) {
							// vu is an edge
							dp[vertexToId.get(currentVertex)][collectionToId
									.get(currentSet)] = true;

							break;
						}
					}
				}
			}
		}

		int pathEnds = -1;
		for (int i = 0; i < n; i++) {
			if (dp[i][collectionToId.get(graph.vertexSet())]) {
				pathEnds = i;
				break;
			}
		}

		if (pathEnds < 0) {
			return null;
		}

		// we need to reconstruct path from dp table!
		ArrayList<V> hamPath = new ArrayList<V>(n);

		ArrayList<E> edgeList = new ArrayList<E>(n);

		V currentVertex = idToVertex.get(pathEnds);
		Collection<V> currentSet = new HashSet<V>(n);
		currentSet.addAll(graph.vertexSet());

		hamPath.add(currentVertex);

		// going backwards from pathEnds
		for (int i = 0; i < n - 1; i++) {

			currentSet.remove(currentVertex);
			int currentSetId = collectionToId.get(currentSet);

			for (int newVertexId = 0; newVertexId < n; newVertexId++) {
				if (dp[newVertexId][currentSetId]) {
					V newVertex = idToVertex.get(newVertexId);
					graph.getEdge(currentVertex, newVertex);

					// if it doesn't contain this edge, a different newVertex is
					// witness (next in path)
					if (!graph.containsEdge(currentVertex, newVertex)) {
						continue;
					}

					currentVertex = newVertex;

					hamPath.add(currentVertex);
					break;
				}
			}
		}

		for (int i = 1; i < hamPath.size(); i++) {
			V v = hamPath.get(i - 1);
			V u = hamPath.get(i);
			E e = graph.getEdge(v, u);
			if (e == null) {
				e = graph.getEdge(u, v);
				System.err.println("Edge was null but should be none null: "
						+ e);
				System.err.println("current = " + v);
				System.err.println("new     = " + u);
			}
			edgeList.add(e);
		}

		GraphPath<V, E> path = new GraphPathImpl<V, E>(graph, hamPath.get(0),
				hamPath.get(hamPath.size() - 1), edgeList, 0);

		return path;
	}
}
