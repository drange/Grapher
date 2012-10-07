package no.uib.ii.algo.st8.algorithms;

import static no.uib.ii.algo.st8.algorithms.BipartiteInspector.isBipartite;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.graph.SimpleGraph;

public class OddCycleTransversal {
	public static <V, E> Set<V> findOddCycleTransversal(SimpleGraph<V, E> graph) {
		if (graph == null)
			throw new NullPointerException("Input graph to OCT was null!");

		if (isBipartite(graph))
			return new HashSet<V>();

		PowersetIterator<V> subsets = new PowersetIterator<V>(graph.vertexSet());

		Set<V> currentBestOct = graph.vertexSet();

		while (subsets.hasNext()) {
			Set<V> oct = subsets.next();
			if (oct.size() >= currentBestOct.size())
				continue;

			Set<V> vertices = new HashSet<V>();
			vertices.addAll(graph.vertexSet());
			vertices.removeAll(oct);

			SimpleGraph<V, E> h = InducedSubgraph.inducedSubgraphOf(graph,
					vertices);

			if (isBipartite(h)) {
				currentBestOct = oct;
			}
		}
		return currentBestOct;
	}
}
