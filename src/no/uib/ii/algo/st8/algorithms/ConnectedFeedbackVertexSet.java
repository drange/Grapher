package no.uib.ii.algo.st8.algorithms;

import static no.uib.ii.algo.st8.algorithms.GirthInspector.isAcyclic;

import java.util.Collection;
import java.util.HashSet;

import no.uib.ii.algo.st8.util.InducedSubgraph;
import no.uib.ii.algo.st8.util.PowersetIterator;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

/**
 * Returns on execute() null if disconnected, else a connected feedback vertex
 * set.
 * 
 * @author drange
 * 
 * @param <V>
 *            vertex set
 * @param <E>
 *            edge set
 */
public class ConnectedFeedbackVertexSet<V, E> extends
		Algorithm<V, E, Collection<V>> {

	public ConnectedFeedbackVertexSet(SimpleGraph<V, E> graph) {
		super(graph);
	}

	public Collection<V> execute() {
		if (!new ConnectivityInspector<V, E>(graph).isGraphConnected())
			return null;

		if (isAcyclic(graph))
			return new HashSet<V>();

		PowersetIterator<V> subsets = new PowersetIterator<V>(graph.vertexSet());

		Collection<V> vertices = new HashSet<V>(graph.vertexSet().size());
		Collection<V> fvs = new HashSet<V>(graph.vertexSet().size());

		Collection<V> currentBestFvs = graph.vertexSet();

		while (subsets.hasNext()) {
			fvs = subsets.next();

			if (fvs.size() >= currentBestFvs.size())
				continue;

			progress(fvs.size(), graph.vertexSet().size());
			if (cancelFlag)
				return null;

			vertices.clear();
			vertices.addAll(graph.vertexSet());
			vertices.removeAll(fvs);
			SimpleGraph<V, E> hfvs = InducedSubgraph.inducedSubgraphOf(graph,
					fvs);
			if (!new ConnectivityInspector<V, E>(hfvs).isGraphConnected())
				continue;

			vertices.clear();
			vertices.addAll(graph.vertexSet());
			vertices.removeAll(fvs);

			SimpleGraph<V, E> h = InducedSubgraph.inducedSubgraphOf(graph,
					vertices);

			if (isAcyclic(h))
				currentBestFvs = fvs;
		}
		return currentBestFvs;
	}

}
