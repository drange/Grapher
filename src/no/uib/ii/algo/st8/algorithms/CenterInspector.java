package no.uib.ii.algo.st8.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;

public class CenterInspector {

	/**
	 * Calculates center vertex of the graph, this is the vertex with the least
	 * distance to every other vertex in the graph. However, at the moment, we
	 * ignore unreachable vertices, which makes smaller components likely to
	 * have the center vertex.
	 * 
	 * @param graph
	 * @return
	 */
	public static <V, E> V getCenter(UndirectedGraph<V, E> graph) {
		V center = null;
		int minDistance = graph.vertexSet().size();

		FloydWarshallShortestPaths<V, E> paths = new FloydWarshallShortestPaths<V, E>(
				graph);

		// calculates the number of vertices outside of this vertex' component.
		Map<V, Integer> vertexToAntiSize = calculateComponents(graph);

		for (V v : graph.vertexSet()) {
			int currentDistance = 0;
			for (V u : graph.vertexSet()) {
				if (u != v) {
					double distance = paths.shortestDistance(v, u);

					if (distance == Double.POSITIVE_INFINITY) {
						distance = vertexToAntiSize.get(v);
					}

					currentDistance = Math.max(currentDistance, (int) distance);

				}
			}
			if (currentDistance < minDistance) {
				minDistance = currentDistance;
				center = v;
			}
		}
		return center;
	}

	private static <V, E> Map<V, Integer> calculateComponents(
			UndirectedGraph<V, E> graph) {
		// calculates the number of vertices outside of this vertex' component.
		Map<V, Integer> vertexToAntiSize = new HashMap<V, Integer>();
		ConnectivityInspector<V, E> ci = new ConnectivityInspector<V, E>(graph);

		List<Set<V>> ccs = ci.connectedSets();

		// size of largest component
		int largest = 0;

		for (Set<V> cc : ccs) {
			largest = Math.max(largest, cc.size());
		}

		for (int i = 0; i < ccs.size(); i++) {
			int csize = ccs.get(i).size();
			for (V v : ccs.get(i)) {
				vertexToAntiSize.put(v, largest - csize);
			}
		}

		return vertexToAntiSize;
	}
}
