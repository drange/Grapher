package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

public class Neighbors {

	public static <V, E> Set<V> closedNNeighborhood(SimpleGraph<V, E> graph,
			V vertex, int n) {
		Set<V> neighbors = new HashSet<V>(graph.degreeOf(vertex));
		neighbors.add(vertex);
		for (int i = 0; i < n; i++) {
			Set<V> newneighbors = new HashSet<V>(graph.vertexSet().size());
			for (V v : neighbors) {
				newneighbors.addAll(openNeighborhood(graph, v));
			}
			neighbors.addAll(newneighbors);
		}
		return neighbors;
	}

	public static <V, E> Set<V> openNNeighborhood(SimpleGraph<V, E> graph,
			V vertex, int n) {
		Set<V> neighbors = new HashSet<V>(graph.degreeOf(vertex));
		neighbors.add(vertex);
		for (int i = 0; i < n; i++) {
			Set<V> newneighbors = new HashSet<V>(graph.vertexSet().size());
			for (V v : neighbors) {
				newneighbors.addAll(openNeighborhood(graph, v));
			}
			neighbors.addAll(newneighbors);
		}
		neighbors.remove(vertex);
		return neighbors;
	}

	public static <V, E> Set<V> openNeighborhood(SimpleGraph<V, E> graph,
			V vertex) {
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
