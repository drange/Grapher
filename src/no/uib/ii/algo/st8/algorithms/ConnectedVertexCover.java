package no.uib.ii.algo.st8.algorithms;

import java.util.Collection;
import java.util.Iterator;

import no.uib.ii.algo.st8.util.InducedSubgraph;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class ConnectedVertexCover {
	public static <V, E> SimpleGraph<V, E> getConnectedVertexCover(
			SimpleGraph<V, E> graph) {

		if (!new ConnectivityInspector<V, E>(graph).isGraphConnected())
			return null;

		Iterator<SimpleGraph<V, E>> it = InducedSubgraph
				.inducedSubgraphIterator(graph);
		SimpleGraph<V, E> cvc = null;

		while (it.hasNext()) {
			cvc = it.next();
			if (!new ConnectivityInspector<V, E>(cvc).isGraphConnected())
				continue;
			if (isVertexCover(graph, cvc.vertexSet()))
				return cvc;
		}
		return null;
	}

	private static <V, E> boolean isVertexCover(SimpleGraph<V, E> graph,
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
