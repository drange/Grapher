package no.uib.ii.algo.st8.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import no.uib.ii.algo.st8.util.NChooseKIterator;

import org.jgrapht.graph.SimpleGraph;

public class PerfectCodeInspector {

	/**
	 * Has perfect code
	 * 
	 * @param graph
	 * @return set of vertices or null if not perfect code
	 * 
	 */
	public static <V, E> Collection<V> getPerfectCode(SimpleGraph<V, E> graph) {
		for (int i = 1; i <= graph.vertexSet().size(); i++) {
			Collection<V> C = getPerfectCode(graph, i);
			if (C != null)
				return C;
		}
		return null;
	}

	/**
	 * Has perfect code of size at most k
	 * 
	 * @param graph
	 * @param k
	 *            size of code
	 * @return set of vertices of size at most k or null if not perfect code at
	 *         most k
	 */
	public static <V, E> Collection<V> getPerfectCode(SimpleGraph<V, E> graph, int k) {
		// if we have no edges, the set of vertices is a perfect code
		if (graph.vertexSet().size() == 0 || graph.edgeSet().size() == 0)
			return graph.vertexSet();

		int counter = 0;
		HashMap<V, Integer> vertexToInt = new HashMap<V, Integer>();

		for (V v : graph.vertexSet()) {
			vertexToInt.put(v, counter);
			counter++;
		}

		NChooseKIterator<V> iterator = new NChooseKIterator<V>(graph.vertexSet(), k);
		while (iterator.hasNext()) {
			ArrayList<V> C = new ArrayList<V>(iterator.next());
			int[] degrees = new int[counter];

			boolean invalidCode = false;

			for (V c : C) {
				if (invalidCode)
					break;
				for (V v : Neighbors.openNeighborhood(graph, c)) {
					if (invalidCode)
						break;
					int vid = vertexToInt.get(v);
					degrees[vid]++;
					if (degrees[vid] > 1) {
						invalidCode = true; // break out of all for-loops.
						break;
					}
				}
			}

			for (V v : graph.vertexSet()) {
				int vid = vertexToInt.get(v);
				if (C.contains(v)) {
					if (degrees[vid] > 0) {
						invalidCode = true;
						break;
					}
				} else if (degrees[vid] != 1) {
					invalidCode = true;
					break;
				}
			}
			if (!invalidCode) {
				return C;
			}
		}

		return null;
	}
}
