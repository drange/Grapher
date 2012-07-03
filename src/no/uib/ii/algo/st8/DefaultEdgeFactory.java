package no.uib.ii.algo.st8;

import org.jgrapht.EdgeFactory;

public class DefaultEdgeFactory<V> implements EdgeFactory<V, DefaultEdge<V>> {

	public DefaultEdge<V> createEdge(V source, V target) {
		return new DefaultEdge<V>(source, target);
	}
}
