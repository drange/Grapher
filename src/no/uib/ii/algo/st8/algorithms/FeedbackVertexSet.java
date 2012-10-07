package no.uib.ii.algo.st8.algorithms;

import static no.uib.ii.algo.st8.algorithms.GirthInspector.isAcyclic;

import java.util.HashSet;
import java.util.Set;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.graph.SimpleGraph;

public class FeedbackVertexSet {

	public static <V, E> Set<V> findExactFeedbackVertexSet(
			SimpleGraph<V, E> graph) {
		if (graph == null)
			throw new NullPointerException("Input graph to FVS was null!");

		if (isAcyclic(graph))
			return new HashSet<V>();

		PowersetIterator<V> subsets = new PowersetIterator<V>(graph.vertexSet());

		Set<V> vertices = new HashSet<V>(graph.vertexSet().size());
		Set<V> fvs = new HashSet<V>(graph.vertexSet().size());

		Set<V> currentBestFvs = graph.vertexSet();

		while (subsets.hasNext()) {
			fvs = subsets.next();

			if (fvs.size() >= currentBestFvs.size())
				continue;

			vertices.clear();
			vertices.addAll(graph.vertexSet());
			vertices.removeAll(fvs);

			SimpleGraph<V, E> h = InducedSubgraph.inducedSubgraphOf(graph,
					vertices);

			System.out.println("Induced subgraph on " + h.vertexSet());
			if (isAcyclic(h))
				currentBestFvs = fvs;
		}
		return currentBestFvs;
	}
}
