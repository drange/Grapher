package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

public class Neighbors {
	public static <V, E> Set<V> neighborhood(SimpleGraph<V, E> graph, V vertex) {
		Set<V> set = new HashSet<V>(graph.degreeOf(vertex));
		for (E edge : graph.edgesOf(vertex)) {
			set.add(opposite(graph, vertex, edge));
		}
		return set;
	}

	private static <V, E> V opposite(SimpleGraph<V, E> graph, V vertex, E edge) {
		if (graph.getEdgeSource(edge).equals(vertex)) {
			return graph.getEdgeTarget(edge);
		}
		return graph.getEdgeSource(edge);
	}
}
