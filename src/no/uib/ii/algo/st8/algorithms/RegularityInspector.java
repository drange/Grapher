package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;

import org.jgrapht.graph.SimpleGraph;

public class RegularityInspector {
	public static <V, E> boolean isRegular(SimpleGraph<V, E> g) {
		if (g == null)
			throw new NullPointerException("Input was null");
		if (g.vertexSet().size() == 0)
			return true;
		int deg = g.degreeOf(g.vertexSet().iterator().next());
		for (V v : g.vertexSet()) {
			if (g.degreeOf(v) != deg) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns -1 if non-regular, otherwise degree of all vertices.
	 * 
	 * @param g
	 * @return degree or -1 if non-regular
	 */
	public static <V, E> int getRegularity(SimpleGraph<V, E> g) {
		if (g == null)
			throw new NullPointerException("Input was null");
		if (g.vertexSet().size() == 0)
			return 0;
		int deg = g.degreeOf(g.vertexSet().iterator().next());
		for (V v : g.vertexSet()) {
			if (g.degreeOf(v) != deg) {
				return -1;
			}
		}
		return deg;
	}

	public static <V, E> Set<V> regularDeletionSet(SimpleGraph<V, E> graph) {
		
		Iterator<SimpleGraph<V, E>> i = InducedSubgraph
				.inducedSubgraphIteratorLargeToSmall(graph);

		while (i.hasNext()) {
			SimpleGraph<V, E> h = i.next();
			if (isRegular(h)) {
				Set<V> vertices = new HashSet<V>();
				vertices.addAll(graph.vertexSet());
				vertices.removeAll(h.vertexSet());
				return vertices;
			}
		}
		System.err.println("Should never come here!");
		return new HashSet<V>(0);

	}

	/**
	 * Returns a deletion set for obtaining a degree-regular graph. Returns null
	 * if and only if there is no induced subgraph with given regularity degree.
	 * 
	 * @param graph
	 * @param degree
	 * @return
	 */
	public static <V, E> Set<V> regularDeletionSet(SimpleGraph<V, E> graph,
			int degree) {
		Iterator<SimpleGraph<V, E>> i = InducedSubgraph
				.inducedSubgraphIteratorLargeToSmall(graph);

		while (i.hasNext()) {
			SimpleGraph<V, E> h = i.next();
			if (getRegularity(h) == degree) {
				Set<V> vertices = new HashSet<V>();
				vertices.addAll(graph.vertexSet());
				vertices.removeAll(h.vertexSet());
				return vertices;
			}
		}

		return null;
	}

}
