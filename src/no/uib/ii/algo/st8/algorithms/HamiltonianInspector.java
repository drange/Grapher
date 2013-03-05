package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import no.uib.ii.algo.st8.util.PermutationIterator;
import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.graph.SimpleGraph;

public class HamiltonianInspector {
	public static <V, E> GraphPath<V, E> getHamiltonianPath(
			SimpleGraph<V, E> graph) {

		boolean con = new ConnectivityInspector<V, E>(graph).isGraphConnected();
		if (!con)
			return null;

		int n = graph.vertexSet().size();
		int npow = (int) Math.pow(2, n);

		Map<Integer, V> idToVertex = new HashMap<Integer, V>(n);
		Map<V, Integer> vertexToId = new HashMap<V, Integer>(n);

		Map<Collection<V>, Integer> collectionToId = new HashMap<Collection<V>, Integer>(
				npow);
		Map<Integer, Collection<V>> idToCollection = new HashMap<Integer, Collection<V>>(
				npow);

		PowersetIterator<V> pi = new PowersetIterator<V>(graph.vertexSet());
		int counter = 0;
		while (pi.hasNext()) {
			Collection<V> set = pi.next();
			collectionToId.put(set, counter);
			idToCollection.put(counter, set);

			counter++;
		}

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

		// for (int x = 0; x < dp.length; x++) {
		// for (int y = 0; y < dp[x].length; y++) {
		// int out = dp[x][y] ? 1 : 0;
		// System.out.print(out);
		// }
		// System.out.println();
		// }
		// System.out.println("\n======\n\n");

		int pathEnds = -1;
		for (int i = 0; i < n; i++) {
			if (dp[i][collectionToId.get(graph.vertexSet())]) {
				pathEnds = i;
				break;
			}
		}

		if (pathEnds < 0) {
			// System.out.println("No hamiltonian path");
			return null;
		}

		// System.out.println("We found hamiltonian path ending in " + pathEnds
		// + " = " + idToVertex.get(pathEnds));

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

	public static <V, E> GraphPath<V, E> bruteForceHamiltonianPath(
			SimpleGraph<V, E> graph) {
		if (graph == null)
			throw new NullPointerException("Input graph was null.");
		if (graph.vertexSet().size() < 2)
			return null;

		boolean con = new ConnectivityInspector<V, E>(graph).isGraphConnected();
		if (!con)
			return null;

		PermutationIterator<V> pit = new PermutationIterator<V>(
				graph.vertexSet());
		while (pit.hasNext()) {
			ArrayList<V> pi = pit.next();
			boolean isPath = true;
			for (int i = 1; i < pi.size(); i++) {
				if (!graph.containsEdge(pi.get(i - 1), pi.get(i))) {
					isPath = false;
					break;
				}
			}
			if (isPath) {
				ArrayList<E> edges = new ArrayList<E>(pi.size());
				for (int i = 1; i < pi.size(); i++) {
					edges.add(graph.getEdge(pi.get(i - 1), pi.get(i)));
				}
				GraphPath<V, E> path = new GraphPathImpl<V, E>(graph,
						pi.get(0), pi.get(pi.size() - 1), edges, 0);
				return path;
			}
		}
		return null;
	}
}
