package no.uib.ii.algo.st8.algorithms;

import static no.uib.ii.algo.st8.algorithms.BipartiteInspector.isBipartite;

import java.util.Collection;
import java.util.HashSet;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.graph.SimpleGraph;

public class OddCycleTransversal<V, E> extends Algorithm<V, E, Collection<V>> {

	public OddCycleTransversal(SimpleGraph<V, E> graph) {
		super(graph);
	}

	@Override
	public Collection<V> execute() {

		if (isBipartite(graph))
			return new HashSet<V>();

		PowersetIterator<V> subsets = new PowersetIterator<V>(graph.vertexSet());

		Collection<V> currentBestOct = graph.vertexSet();

		while (subsets.hasNext()) {
			if (cancelFlag)
				return null;

			Collection<V> oct = subsets.next();
			if (oct.size() >= currentBestOct.size())
				continue;

			progress(oct.size(), graph.vertexSet().size());

			Collection<V> vertices = new HashSet<V>();
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
