package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.Iterator;

import no.uib.ii.algo.st8.util.InducedSubgraph;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

/**
 * Returns on execute() null if disconnected, else a connected vertex cover.
 * 
 * @author drange
 * 
 * @param <V>
 *            vertex set
 * @param <E>
 *            edge set
 */
public class ConnectedVertexCover<V, E> extends
		Algorithm<V, E, SimpleGraph<V, E>> {

	public ConnectedVertexCover(SimpleGraph<V, E> graph) {
		super(graph);
	}

	public SimpleGraph<V, E> execute() {
		if (!new ConnectivityInspector<V, E>(graph).isGraphConnected())
			return null;

		Iterator<SimpleGraph<V, E>> it = InducedSubgraph
				.inducedSubgraphIterator(graph);
		SimpleGraph<V, E> cvc = null;

		while (it.hasNext()) {
			cvc = it.next();
			progress(cvc.vertexSet().size(), graph.vertexSet().size());
			if (cancelFlag)
				return null;
			if (!new ConnectivityInspector<V, E>(cvc).isGraphConnected())
				continue;
			if (isVertexCover(graph, cvc.vertexSet()))
				return cvc;
		}
		return null;
	}

	private boolean isVertexCover(SimpleGraph<V, E> graph,
			Collection<V> vertices) {
		for (E e : graph.edgeSet()) {
			V v = graph.getEdgeSource(e);
			V u = graph.getEdgeTarget(e);
			if (vertices.contains(v) || vertices.contains(u))
				continue;
			else
				return false;
		}
		return true;
	}

}
