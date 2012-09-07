package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleGraph;

public class BipartiteInspector {

	/**
	 * Returns bipartition, one of the partitions
	 * 
	 * @param graph
	 * @return bipartition, null if not bipartite
	 */
	public static <V, E> Set<V> getBipartition(SimpleGraph<V, E> graph) {
		if (graph.vertexSet().size() == 0)
			return new HashSet<V>();

		Set<V> a = new HashSet<V>(graph.vertexSet().size());
		Set<V> b = new HashSet<V>(graph.vertexSet().size());

		Set<V> notProcessed = new HashSet<V>();
		notProcessed.addAll(graph.vertexSet());

		Set<V> fringe = new HashSet<V>(graph.vertexSet().size());

		while (!notProcessed.isEmpty()) {
			V current = notProcessed.iterator().next();
			fringe.add(current);
			notProcessed.remove(current);

			// current is first in its connected component to be processed
			a.add(current);

			while (!fringe.isEmpty()) {
				current = fringe.iterator().next();
				fringe.remove(current);
				notProcessed.remove(current);

				if (a.contains(current)) {
					for (E e : graph.edgesOf(current)) {
						V u = opposite(graph, e, current);
						if (a.contains(u))
							return null;
						b.add(u);
						if (notProcessed.contains(u)) {
							fringe.add(u);
						}
					}
				} else if (b.contains(current)) {
					for (E e : graph.edgesOf(current)) {
						V u = opposite(graph, e, current);
						if (b.contains(u))
							return null;
						a.add(u);
						if (notProcessed.contains(u)) {
							fringe.add(u);
						}
					}
				}
			}
		}
		return a;
	}

	/**
	 * Returns true iff graph is bipartite
	 * 
	 * @param graph
	 * @return true if input is bipartite
	 */
	public static <V, E> boolean isBipartite(SimpleGraph<V, E> graph) {
		return getBipartition(graph) != null;
	}

	private static <V, E> V opposite(SimpleGraph<V, E> g, E e, V v) {
		if (g.getEdgeSource(e) == v)
			return g.getEdgeTarget(e);
		return g.getEdgeSource(e);
	}
}
