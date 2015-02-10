package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import no.uib.ii.algo.st8.util.Neighbors;

import org.jgrapht.graph.SimpleGraph;

import android.util.Pair;

/**
 * 
 * @author markussd
 * 
 */
public class FlowInspector {

	/**
	 * Given a graph, a source and a target vertex the function computes the
	 * flow and returns the size of the flow and a set of edges demonstrating
	 * the flow in the graph.
	 * 
	 * @param graph
	 *            A simple graph
	 * @param source
	 *            The source
	 * @param target
	 *            The target
	 * @return The flow from source to target and a set of edges demonstrating
	 *         the flow
	 */
	public static <V, E> Pair<Integer, Collection<E>> findFlow(
			SimpleGraph<V, E> graph, V source, V target) {
		Set<Pair<V, V>> flowEdges = new HashSet<Pair<V, V>>();

		// Compute flow
		int flow = 0;
		while (flowIncreasingPath(graph, flowEdges, source, target))
			++flow;

		// Retrieve edges with flow
		Set<E> edges = new HashSet<E>();
		for (Pair<V, V> p : flowEdges)
			edges.add(graph.getEdge(p.first, p.second));

		return new Pair<Integer, Collection<E>>(flow, edges);
	}

	/**
	 * Finds a flow increasing path in the graph from source to target avoiding
	 * the directed edges in flowEdges.
	 * 
	 * @param graph
	 *            The graph
	 * @param flowEdges
	 *            The directed edges that should not be used in the flow
	 *            increasing path
	 * @param source
	 *            The source
	 * @param target
	 *            The target
	 * @return True if a flow increasing path exist, false otherwise.
	 */
	private static <V, E> boolean flowIncreasingPath(SimpleGraph<V, E> graph,
			Set<Pair<V, V>> flowEdges, V source, V target) {
		Map<V, V> prev = new HashMap<V, V>();
		Queue<V> next = new LinkedList<V>();

		// Initialise search
		next.add(source);
		prev.put(source, null);

		// Search the graph
		while (!next.isEmpty()) {
			V v = next.poll();

			if (v == target)
				break;

			// Look at the neighbourhood
			for (V nghbr : Neighbors.openNeighborhood(graph, v)) {
				// If the edge is not in flowEdges and the neighbour is not
				// already visited we want to search the neighbour
				if (!flowEdges.contains(new Pair<V, V>(v, nghbr))
						&& !prev.containsKey(nghbr)) {
					next.add(nghbr);
					prev.put(nghbr, v);
				}
			}
		}

		// No path found
		if (!prev.containsKey(target))
			return false;

		// Updates flowEdges according to the path found
		V v = target;
		while (v != source) {
			flowEdges.add(new Pair<V, V>(prev.get(v), v));
			flowEdges.add(new Pair<V, V>(v, prev.get(v)));
			v = prev.get(v);
		}

		return true;
	}
}