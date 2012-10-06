package no.uib.ii.algo.st8.algorithms;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;

import org.jgrapht.graph.SimpleGraph;

public class OddCycleTransversal {
	public static <V, E> Set<V> findOddCycleTransversal(SimpleGraph<V, E> graph) {
		Iterator<SimpleGraph<V, E>> i = InducedSubgraph
				.inducedSubgraphIteratorLargeToSmall(graph);

		while (i.hasNext()) {
			SimpleGraph<V, E> h = i.next();
			if (BipartiteInspector.isBipartite(h)) {
				Set<V> vertices = new HashSet<V>();
				vertices.addAll(graph.vertexSet());
				vertices.removeAll(h.vertexSet());
				return vertices;
			}
		}
		System.err.println("Should never come here!");
		return new HashSet<V>(0);
	}
}
