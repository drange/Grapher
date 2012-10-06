package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

public class EulerianInspector {

	/**
	 * Tests if a graph is eulerian (an Euler Graph) by testing that every
	 * vertex has even degree. Does not care about connectedness.
	 * 
	 * @param g
	 *            graph
	 * @return true if eulerian
	 */
	public static <V, E> boolean isEulerian(SimpleGraph<V, E> g) {
		if (g == null)
			throw new NullPointerException("Input was null");

		for (V v : g.vertexSet()) {
			if (g.edgesOf(v).size() % 2 != 0)
				return false;
		}
		return true;
	}

	/**
	 * Tests if a graph is eulerian (an Euler Graph) by testing that every
	 * vertex has even degree. Does not care about connectedness.
	 * 
	 * @param g
	 *            graph
	 * @return true if eulerian
	 */
	public static <V, E> Set<V> getOddDegreeVertices(SimpleGraph<V, E> g) {
		if (g == null)
			throw new NullPointerException("Input was null");

		Set<V> odds = new HashSet<V>(g.vertexSet().size());

		for (V v : g.vertexSet()) {
			if (g.edgesOf(v).size() % 2 != 0)
				odds.add(v);
		}
		return odds;
	}

}
