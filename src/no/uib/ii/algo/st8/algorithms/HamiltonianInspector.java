package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

		Map<Integer, V> intToVertex = new HashMap<Integer, V>(n);
		Map<V, Integer> vertexToInt = new HashMap<V, Integer>(n);

		Map<Collection<V>, Integer> collectionToInt = new HashMap<Collection<V>, Integer>(
				npow);
		Map<Integer, Collection<V>> intToCollection = new HashMap<Integer, Collection<V>>(
				npow);

		PowersetIterator<V> pi = new PowersetIterator<V>(graph.vertexSet());
		int counter = 0;
		while (pi.hasNext()) {
			Collection<V> set = pi.next();
			collectionToInt.put(set, counter);
			intToCollection.put(counter, set);
			counter++;
		}

		List<V> vertices = new ArrayList<V>(n);

		counter = 0;
		for (V v : graph.vertexSet()) {
			intToVertex.put(counter, v);
			vertexToInt.put(v, counter);
			vertices.add(v);
			counter++;
		}

		/*
		 * Standard Hamiltonian path algorithm in 2^n time by dynamic
		 * programming. The table dp[v][S] is true if v is in S and there is a
		 * path going through the entire S and ending in v. Recursively we test
		 * whether dp[u][S-v] is true for some u in S.
		 */

		// base cases, set (n, {n}) to true
		boolean[][] dp = new boolean[n][npow];
		for (int i = 0; i < n; i++) {
			V v = intToVertex.get(i);
			HashSet<V> s = new HashSet<V>(1);
			s.add(v);
			int key = collectionToInt.get(s);
			dp[i][key] = true;
		}

		for (int v = 0; v < n; v++) {
			V vertex = intToVertex.get(v);

			// s is the characteristic vector for a subset currentSet
			for (int s = 0; s < npow; s++) {
				// currentSet is the set corresponding to 's'
				Collection<V> currentSet = intToCollection.get(s);
				if (!currentSet.contains(vertex))
					continue;

				// System.out.println("v = " + v);
				// System.out.println("S = " + currentSet);
				for (V u : currentSet) {
					Set<V> Sprime = new HashSet<V>(currentSet.size());
					Sprime.addAll(currentSet);
					Sprime.remove(vertex);
					// System.out.println("\ttesting S' = " + Sprime);

					if (dp[vertexToInt.get(u)][collectionToInt.get(Sprime)]) {
						// okey, there is a path in Sprime ending in u, need
						// only that there is an edge between u and v

						if (graph.containsEdge(intToVertex.get(v), u)) {
							// vu is an edge

							dp[vertexToInt.get(vertex)][collectionToInt
									.get(currentSet)] = true;

							continue;
						}
					}
				}
			}
		}

		// for (int x = 0; x < dp.length; x++) {
		// for (int y = 0; y < dp[x].length; y++) {
		// int out = dp[x][y] ? 1 : 0;
		// System.out.print(out + " ");
		// }
		// System.out.println();
		// }
		// System.out.println("\n======\n\n");

		if (!dp[n - 1][collectionToInt.get(graph.vertexSet())]) {
			// System.out.println("No hamiltonian path");
			return null;
		}

		// we need to reconstruct path from dp table!
		ArrayList<V> hamPath = new ArrayList<V>(n);

		ArrayList<E> edgeList = new ArrayList<E>(n);

		V currentVertex = intToVertex.get(n - 1);
		Collection<V> currentSet = new HashSet<V>(n);
		currentSet.addAll(graph.vertexSet());

		hamPath.add(currentVertex);

		for (int i = 0; i < n - 1; i++) {
			currentSet.remove(currentVertex);
			int currentSetId = collectionToInt.get(currentSet);
			for (int newVertexId = 0; newVertexId < n; newVertexId++) {
				if (dp[newVertexId][currentSetId]) {
					V newVertex = intToVertex.get(newVertexId);
					edgeList.add(graph.getEdge(currentVertex, newVertex));
					currentVertex = newVertex;
					hamPath.add(currentVertex);
					break;
				}
			}
		}

		// System.out.println("Found hamiltonian path");
		// System.out.println("\t" + edgeList);

		GraphPath<V, E> path = new GraphPathImpl<V, E>(graph, hamPath.get(0),
				hamPath.get(hamPath.size() - 1), edgeList, 0);

		return path;
	}
}
