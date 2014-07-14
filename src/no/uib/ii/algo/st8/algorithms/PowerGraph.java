package no.uib.ii.algo.st8.algorithms;

import java.util.Set;

import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

public class PowerGraph {

	/**
	 * Creates (2nd) power of input graph
	 * 
	 * @param graph
	 *            any
	 * @return new graph reusing vertex objects
	 */
	public static <V, E> SimpleGraph<V, E> constructPowerGraph(
			SimpleGraph<V, E> graph) {
		return constructPowerGraph(graph, 2);
	}

	/**
	 * Creates nth power of input graph
	 * 
	 * @param graph
	 *            any
	 * @param n
	 *            any int > 0?
	 * @return new graph reusing vertex objects
	 */
	public static <V, E> SimpleGraph<V, E> constructPowerGraph(
			SimpleGraph<V, E> graph, int n) {
		// takes a graph and creates the power graph of the given graph
		SimpleGraph<V, E> power = new SimpleGraph<V, E>(graph.getEdgeFactory());
		for (V v : graph.vertexSet()) {
			power.addVertex(v);
		}

		for (V v : graph.vertexSet()) {
			Set<V> neigs = Neighbors.openNNeighborhood(graph, v, n);
			for (V u : neigs) {
				if (!power.containsEdge(v, u)) {
					power.addEdge(v, u);
				}
			}
		}

		return power;
	}

}
